package com.epiroc.wifiaware.net

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.epiroc.wifiaware.utility.WifiAwareUtility
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class Subscriber(
    ctx: Context,
    nanSession: WifiAwareSession,
    cManager: ConnectivityManager,
    srvcName: String,
    uuid: String
) {

    data class DeviceConnection(val deviceIdentifier: String, val timestamp: Long)

    private lateinit var subNetwork : Network
    private val serviceUUID = uuid
    private val serviceName = srvcName
    private val context = ctx
    private val subscribeMessageLiveData: MutableState<String> = mutableStateOf("")
    private var connectivityManager = cManager
    private var wifiAwareSession = nanSession
    private var currentSubSession: DiscoverySession? = null
    private lateinit var currentNetworkCapabilities: NetworkCapabilities
    private val utility: WifiAwareUtility = WifiAwareUtility
    private lateinit var networkCallbackSub: ConnectivityManager.NetworkCallback

    private val uuidMessageLiveData: MutableState<String> = mutableStateOf("")


    fun subscribeToWifiAwareSessions() {

        Log.d("1Wifi","SUBSCRIBE: subscribeToWifiAwareSessions called")

        if (wifiAwareSession == null) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("subscribe_message", "SUBSCRIBE: Wifi Aware session is not available")
            }
            Log.d("1Wifi","SUBSCRIBE: Wifi Aware session is not available")
            return
        }

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        val handler = Handler(Looper.getMainLooper()) // Use the main looper.

        val discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                Log.d("1Wifi", "SUBSCRIBE: Subscription started.")
                currentSubSession = session

            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                Log.d("1Wifi", "SUBSCRIBE: Service discovered from peer: $peerHandle")
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                if (peerHandle != null)   {
                    Log.d("1Wifi", "SUBSCRIBE: We Connected to $peerHandle In the sub")
                    Thread.sleep(100)
                    subscribeMessageLiveData.value = "SUBSCRIBE: Connected to  $peerHandle                                                      THESE ARE THE DETAILS:($peerHandle: ${serviceSpecificInfo.toString()} ${matchFilter.toString()})"
                    Log.d("1Wifi", "SUBSCRIBE: we are sending a message now")
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            currentSubSession?.sendMessage(
                                peerHandle,
                                0, // Message type (0 for unsolicited)
                                serviceUUID.toByteArray(Charsets.UTF_8)
                            )
                        }
                    }, 1000) // Delay in milliseconds
                }else{
                    Log.e("1Wifi", "SUBSCRIBE: Peerhandle is null")
                }
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle incoming data here.
                subscribeMessageLiveData.value = "SUBSCRIBE: GOT A MESSAGE FROM THE FOLLOWING UUID " + String(message, Charsets.UTF_8)

                Log.d("1Wifi", "SUBSCRIBE: Message received from peer: $peerHandle")
                if(shouldConnectToDevice(String(message, Charsets.UTF_8))){
                    utility.add(utility.createDeviceConnection(String(message, Charsets.UTF_8),System.currentTimeMillis()))
                    establishConnection(peerHandle)

                }else{
                    Log.e("1Wifi", "SUBSCRIBE: Device has already been discovered "+String(message, Charsets.UTF_8))
                }
            }
        }

        // Subscribe to WiFi Aware sessions.
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permissions here if needed.
        }
        wifiAwareSession!!.subscribe(subscribeConfig, discoverySessionCallback, handler)
    }

    fun shouldConnectToDevice(deviceIdentifier: String): Boolean {

        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis: Long = 5 * 60 * 1000

        val deviceConnection = utility.findDevice(deviceIdentifier)

        if (deviceConnection != null) {
            val timeSinceLastConnection = currentTime - deviceConnection.timestamp
            if (timeSinceLastConnection < fiveMinutesInMillis) {
                Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] was connected ${timeSinceLastConnection / 1000} seconds ago. Not connecting again.")
                return false
            } else {
                // Update the timestamp for this device since we're going to reconnect.
                Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] was connected more than 5 minutes ago. Updating timestamp and reconnecting.")
                utility.remove(deviceConnection)
                return true
            }
        } else {
            // Device is not in the list. Add it and allow the connection.
            Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] is not in the list. Adding it and allowing connection.")
            return true
        }
    }

    private fun establishConnection(peerHandle: PeerHandle) {
        Log.d("1Wifi", "SUBSCRIBE: Attempting to establish connection with peer: $peerHandle")
        val networkSpecifier = currentSubSession?.let {
            WifiAwareNetworkSpecifier.Builder(it, peerHandle)
                .setPskPassphrase("somePassword")
                .build()
        }
        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        networkCallbackSub = object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                //Thread.sleep(100)
                Log.d("1Wifi", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port
                var socket: Socket? = null
                try {
                    socket = network.socketFactory.createSocket() // Don't pass the address and port here.
                    socket.connect(InetSocketAddress(peerIpv6, peerPort), 5000)
                } catch (e: Exception) {
                    Log.e("1Wifi", "SUBSCRIBE: ERROR SOCKET COULD NOT BE MADE! ${e.message} THIS IS THE currentNetworkCapabilities: (${currentNetworkCapabilities.toString()})")
                    wifiAwareSession!!.close()
                    return
                }

                if (socket != null) {
                    handleDataExchange(peerHandle, socket)
                }
                //port += 1
                socket?.close()
                //currentNetworkCapabilities = null
            }


            override fun onAvailable(network: Network) {
                Log.d("1Wifi", "SUBSCRIBE: Network available for peer: $peerHandle")
                subNetwork = network
            }

            override fun onLost(network: Network) {
                subscribeMessageLiveData.value = "SUBSCRIBE: Network lost for peer: $peerHandle It will now start a new sub session in 3 min"
                Log.d("1Wifi", "SUBSCRIBE: Network lost for peer: $peerHandle")
                Log.d("1Wifi", "SUBSCRIBE: SUBSCRIBE CONNECTION LOST")

                // Close the SubscribeDiscoverySession
                currentSubSession?.close()
                currentSubSession = null
                wifiAwareSession?.close()
            }
        }
        // Request the network and handle connection in the callback as shown above.
        connectivityManager?.requestNetwork(myNetworkRequest, networkCallbackSub)
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket) {
        Log.d("1Wifi", "SUBSCRIBE: Attempting to send information to: $peerHandle")

        socket.getOutputStream().use { outputStream ->
            PrintWriter(OutputStreamWriter(outputStream)).apply {
                for (i in 1..100) {
                    println("HELLO, server we are subscriber! count: $i")
                    flush()
                }
                socket.shutdownOutput()
                Log.d("1Wifi", "SUBSCRIBE: All information sent we are done")
            }
        }
        Log.d("DONEEE", "subscriberDone = true")
        //subscriberDone = true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
            }
        }, 1000) // Delay in milliseconds

    }

    fun getSubscribeMessageLiveData(): MutableState<String> {
        return if (::subscribeMessageLiveData != null) subscribeMessageLiveData else mutableStateOf("")
    }

    fun getUUIDMessageLiveData(): MutableState<String>{
        return if (::uuidMessageLiveData != null) uuidMessageLiveData else mutableStateOf("")
    }


}