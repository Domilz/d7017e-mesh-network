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
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.Timer
import java.util.TimerTask

class Publisher(
    wakeLock : WakeLock,
    ctx: Context,
    nanSession: WifiAwareSession,
    client: Client,
    srvcName: String?,
    uuid: String
) {
    private var serviceUUID = uuid
    private val wakeLock = wakeLock
    private var context = ctx
    private var currentPubSession: DiscoverySession? = null
    private val utility: WifiAwareUtility = WifiAwareUtility
    private val serviceName = srvcName
    private val wifiAwareSession = nanSession
    private lateinit var currentNetwork : Network

    private var client = client
    private var serverSocket: ServerSocket? = null
    private lateinit var networkCallbackPub: ConnectivityManager.NetworkCallback
    private var clientSocket: Socket? = null
    private val messagesReceived: MutableList<String> = mutableListOf()



    fun publishUsingWifiAware() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        Log.d("1Wifi", "PUBLISH: Attempting to start publishUsingWifiAware.")
        if (wifiAwareSession != null) {
            Log.d("1Wifi", "PUBLISH: ServiceName is set to $serviceName.")
            val config = PublishConfig.Builder()
                .setServiceName(serviceName!!)
                .build()
            val handler = Handler(Looper.getMainLooper())
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("1Wifi","PUBLISH: NO PREM FOR PUB")

            } else {
                Log.d("1Wifi","PUBLISH: WE HAVE PREM TO PUBLISH")
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        Log.d("1Wifi", "PUBLISH: Publish started")
                        currentPubSession = session
                    }
                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        if(message.toString() == "onLost"){
                            networkCallbackPub.onLost(currentNetwork)
                        }else{
                            Log.d("1Wifi", "PUBLISH: Message received from peer in publisher $peerHandle")
                            //connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                            CoroutineScope(Dispatchers.IO).launch {
                                createNetwork(peerHandle, wifiAwareSession, context)
                            }
                            //publishMessageLiveData.value = "PUBLISH: MessageReceived from $peerHandle message: ${message.decodeToString()}"
                            // Respond to the sender (Device A) if needed.
                            //val byteArrayToSend = "tag_id:\"PUBLISH\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"
                            Log.d("1Wifi", "PUBLISH: sending message now via publisher to $peerHandle")

                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    currentPubSession?.sendMessage(
                                        peerHandle,
                                        0, // Message type (0 for unsolicited)
                                        serviceUUID.toByteArray(Charsets.UTF_8)
                                    )
                                }
                            }, 2000) // Delay in milliseconds*/
                        }
                    }
                }, handler)
            }
        } else {
            Log.d("1Wifi", "PUBLISH: Wifi Aware session is not available.")
        }
    }
    fun createNetwork(peerHandle : PeerHandle, wifiAwareSession : WifiAwareSession, context : Context){
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }

        this.context = context
        val connectivityManager = ConnectivityManagerHelper.getManager(context)
        //if (serverSocket == null || serverSocket!!.isClosed) {
        //}
        serverSocket = ServerSocket(0)

        var port = if (serverSocket!!.localPort != -1) serverSocket!!.localPort else 1337

        var networkSpecifier = WifiAwareNetworkSpecifier.Builder(currentPubSession!!, peerHandle)
            .setPskPassphrase("somePassword")
            .setPort(port)
            .build()
        var myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()


        Log.d("NETWORKWIFI","PUBLISH: All necessary wifiaware network things created now awaiting callback on port $port and this is port from local ${serverSocket!!.localPort}")
        networkCallbackPub = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                currentNetwork = network
                try {
                    Log.d("NETWORKWIFI","PUBLISH: onAvailable")
                    serverSocket?.soTimeout = 1000
                    serverSocket?.reuseAddress = true

                    try {
                        Log.d("NETWORKWIFI","PUBLISH: TESTAR ATT ACCA SOCKET")
                        clientSocket = serverSocket?.accept()
                    } catch (e: Exception) {
                        Log.d("NETWORKWIFI","PUBLISH: DET GICK INTE, FÖRSÖKER IGEN")
                        serverSocket?.close()
                        try {

                            if (serverSocket == null || serverSocket!!.isClosed) {
                                serverSocket = ServerSocket(0)
                                serverSocket?.soTimeout = 1000
                                serverSocket?.reuseAddress = true
                                clientSocket = serverSocket?.accept()
                            }
                        } catch (e: Exception) {
                            Log.d("NETWORKWIFI","PUBLISH: SKET SIG IGEN")
                            serverSocket?.close()
                            clientSocket?.close()
                            return
                        }

                    }
                    Log.d("NETWORKWIFI","PUBLISH: DET GICK BRA")
                    handleClient(clientSocket,connectivityManager)
                    clientSocket!!.close()
                    val onLostMessage = "onLost".toByteArray(Charsets.UTF_8)
                    currentPubSession?.sendMessage(peerHandle, 0, onLostMessage)
                    Log.d("1Wifi", "PUBLISH: Accepting client $network")
                } catch (e: Exception) {
                    Log.e("1Wifi", "PUBLISH: ERROR Exception while accepting client", e)
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI","PUBLISH: onCapabilitiesChanged")
                Log.d("1Wifi", "PUBLISH: onCapabilitiesChanged incoming: ${networkCapabilities.transportInfo}")
            }

            override fun onLost(network: Network) {
                Log.d("1Wifi", "PUBLISH: Connection lost: $network")
                currentPubSession?.close()
                currentPubSession = null

                closeServerSocket()
                connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
                Log.e("1Wifi", "PUBLISH: EVERYTHING IN PUBLISH IS NOW CLOSED RESETTING PUBLISHER")
                publishUsingWifiAware()
            }
        }

        connectivityManager.requestNetwork(myNetworkRequest, networkCallbackPub);
    }

    private fun handleClient(clientSocket: Socket?,connectivityManager : ConnectivityManager ) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }

        Log.d("1Wifi", "PUBLISH: handleClient started.")
        client.insertSingleMockedReading("publish")
        var sdfsdf = client.state
        clientSocket!!.getInputStream().use { inputStream ->

            val dataInputStream = DataInputStream(inputStream)

            try {
                val size = dataInputStream.readInt()
                if (size > 0) {
                    val messageBytes = ByteArray(size)
                    dataInputStream.readFully(messageBytes)
                    try{
                        client.insert(messageBytes)
                    }catch (e: Exception){
                        Log.d("1Wifi", e.message.toString())
                    }

                    Log.d("INFOFROMCLIENT", "Received protobuf message: ${client.getReadableOfSingleState(messageBytes)}")
                } else {
                    Log.d("INFOFROMCLIENT", "End of stream reached or the connection")
                    //return
                }
            } catch (e: EOFException) {
                // End of stream has been reached or the connection was closed
                Log.d("INFOFROMCLIENT", "End of stream reached or the connection was closed.")
            } catch (e: IOException) {
                // Handle I/O error
                Log.e("INFOFROMCLIENT", "I/O error: ${e.message}")
            }
        }
        Log.d("1Wifi", "PUBLISH: All information received we are done $messagesReceived")
        Log.d("DONEEE", "publisherDone = true")
        Log.d("1Wifi", "${client.getReadableOfSingleState(sdfsdf)}" )
        Log.d("1Wifi", "${client.getReadableOfSingleState(client.state)}" )

        utility.saveToFile(context,client.state)
       // connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
    }

    fun closeServerSocket() {
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
        }
    }

    fun getCurrent(): DiscoverySession? {
        return currentPubSession
    }
}