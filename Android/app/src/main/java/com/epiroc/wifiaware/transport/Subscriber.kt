package com.epiroc.wifiaware.transport

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
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.transport.network.ConnectivityManagerHelper
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tag.Client
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class Subscriber(
    wakeLock : WakeLock,
    ctx: Context,
    nanSession: WifiAwareSession,
    client: Client,
    srvcName: String,
    uuid: String
) {

    private val serviceUUID = uuid
    private val wakeLock = wakeLock
    private val serviceName = srvcName
    private val context = ctx
    private val client = client
    private val utility: WifiAwareUtility = WifiAwareUtility

    private var wifiAwareSession = nanSession
    private var currentSubSession: DiscoverySession? = null

    private lateinit var networkCallbackSub: ConnectivityManager.NetworkCallback
    private lateinit var  subNetwork : Network
    private lateinit var clientSocket: Socket



    fun subscribeToWifiAwareSessions() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        val handler = Handler(Looper.getMainLooper()) // Use the main looper.
        Log.d("1Wifi","SUBSCRIBE: subscribeToWifiAwareSessions called")

        if (wifiAwareSession == null) {
            Log.d("1Wifi","SUBSCRIBE: Wifi Aware session is not available")
            return
        }

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        var discoverySessionCallback = object : DiscoverySessionCallback() {
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
                    Log.d("1Wifi", "SUBSCRIBE: we are sending a message now")
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            currentSubSession?.sendMessage(
                                peerHandle,
                                0, // Message type (0 for unsolicited)
                                serviceUUID.toByteArray(Charsets.UTF_8)
                            )
                        }
                    }, 0) // Delay in milliseconds
                }else{
                    Log.e("1Wifi", "SUBSCRIBE: Peerhandle is null")
                }
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                if(message.toString() == "onLost"){
                    networkCallbackSub.onLost(subNetwork)
                } else {
                    Log.d("1Wifi", "SUBSCRIBE: Message received from peer: $peerHandle")
                    if(shouldConnectToDevice(String(message, Charsets.UTF_8))){
                        utility.add(utility.createDeviceConnection(String(message, Charsets.UTF_8),System.currentTimeMillis()))
                        CoroutineScope(Dispatchers.IO).launch {
                            createNetwork(peerHandle,wifiAwareSession,context)
                        }


                    }else{
                        Log.e("1Wifi", "SUBSCRIBE: Device has already been discovered "+String(message, Charsets.UTF_8))
                    }
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
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis: Long = 5 * 60 * 1000
        val deviceConnection = utility.findDevice(deviceIdentifier)

        return if (deviceConnection != null) {
            val timeSinceLastConnection = currentTime - deviceConnection.timestamp
            if (timeSinceLastConnection < fiveMinutesInMillis) {
                Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] was connected ${timeSinceLastConnection / 1000} seconds ago. Not connecting again.")
                false
            } else {
                Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] was connected more than 5 minutes ago. Updating timestamp and reconnecting.")
                utility.remove(deviceConnection)
                true
            }
        } else {
            Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] is not in the list. Adding it and allowing connection.")
            true
        }
    }

    fun createNetwork(peerHandle : PeerHandle, wifiAwareSession : WifiAwareSession, context : Context) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        val connectivityManager = ConnectivityManagerHelper.getManager(context)

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
        Log.d("NETWORKWIFI","SUBSCRIBER: All necessary wifiaware network things created now awaiting callback")
        networkCallbackSub = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI","SUBSCRIBER: onCapabilitiesChanged")
                Log.d("1Wifi", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port

                Log.d("1Wifi", "peerport is: $peerPort, aware info: $peerAwareInfo")

                try {
                    clientSocket = network.socketFactory.createSocket() // Don't pass the address and port here.
                    clientSocket.reuseAddress = true
                    clientSocket.connect(InetSocketAddress(peerIpv6, peerPort), 1000)
                    Log.d("1Wifi","port for clientsocket: ${clientSocket.port}")
                    handleDataExchange(peerHandle, clientSocket,connectivityManager)


                } catch (e: Exception) {
                    Log.e("1Wifi", "SUBSCRIBE: ERROR SOCKET COULD NOT BE MADE! ${e.message}")
                    onLost(network)
                }

                //clientSocket?.close()
            }

            override fun onAvailable(network: Network) {
                Log.d("NETWORKWIFI","SUBSCRIBER: onCapabilitiesChanged")
                Log.d("1Wifi", "SUBSCRIBE: Network available for peer: $peerHandle")
                subNetwork = network
            }

            override fun onLost(network: Network) {
                Log.d("NETWORKWIFI","SUBSCRIBER: onLost")
                Log.d("1Wifi", "SUBSCRIBE: Network lost for peer: $peerHandle")
                Log.d("1Wifi", "SUBSCRIBE: SUBSCRIBE CONNECTION LOST")


                currentSubSession?.close()
                currentSubSession = null
                closeClientSocket()
                connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
                Log.e("1Wifi", "SUBSCRIBE: EVERYTHING IN SUBSCRIBER CLOSED AND WE ARE NOW RESETTING THE SUBSCRIBER ")
                subscribeToWifiAwareSessions()

            }
        }
        // Request the network and handle connection in the callback as shown above.
        connectivityManager?.requestNetwork(myNetworkRequest, networkCallbackSub)
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket,connectivityManager : ConnectivityManager) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        Log.d("1Wifi", "SUBSCRIBE: Attempting to send information to: $peerHandle")
        client.insertSingleMockedReading("Client")
        val state = client.state

        socket.getOutputStream().use { outputStream ->

            val size = state.size
            outputStream.write(ByteBuffer.allocate(4).putInt(size).array())

            // Now write the actual protobuf message bytes
            try{
                outputStream.write(state)
                outputStream.flush()
                socket.shutdownOutput()
                //outputStream.close()
            }catch (e: Exception){
                Log.e("1Wifi", "SUBSCRIBE: ERROR HERE!!!!!!!!!!!")
            }
            outputStream.close()

            Log.d("1Wifi", "SUBSCRIBE: All information sent we are done")

        }

        Log.d("DONEEE", "subscriberDone = true")
        val onLostMessage = "onLost".toByteArray(Charsets.UTF_8)
        currentSubSession?.sendMessage(peerHandle,0,onLostMessage)
        networkCallbackSub.onLost(subNetwork)
        //subscriberDone = true
        //connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)


    }

    fun closeClientSocket() {
        try {
            clientSocket?.close()
        } catch (e: IOException) {
            Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
        }
    }

    fun getCurrent(): DiscoverySession? {
        return currentSubSession
    }


}