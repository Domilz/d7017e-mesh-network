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
import android.util.Log
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.lib.Config
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
    ctx: Context,
    nanSession: WifiAwareSession,
    client: Client,
    srvcName: String?,
) {
    private var context = ctx
    private var currentPubSession: DiscoverySession? = null
    private val utility: WifiAwareUtility = WifiAwareUtility
    private val serviceName = srvcName
    private val wifiAwareSession = nanSession
    private var currentNetwork : Network? = null

    private var client = client
    private lateinit var networkCallbackPub: ConnectivityManager.NetworkCallback
    private var clientSocket: Socket? = null
    private val messagesReceived: MutableList<String> = mutableListOf()

    fun publishUsingWifiAware() {
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

                        if (shouldConnectToDevice(String(message))) {
                            Log.d("1Wifi", "PUBLISH: Message received from peer in publisher $peerHandle")
                            CoroutineScope(Dispatchers.IO).launch {
                                createNetwork(peerHandle, context)
                                Log.d("1Wifi", "PUBLISH: PLEASE TELL ME THIS WORKS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        currentPubSession?.sendMessage(
                                            peerHandle,
                                            0, // Message type (0 for unsolicited),
                                            "".toByteArray()
                                        )
                                    }
                                }, 100) // Delay in milliseconds*/

                                Log.d("1Wifi", "PUBLISH: sending message now via publisher to $peerHandle")
                            }


                        }
                    }
                }, handler)
            }
        } else {
            Log.d("1Wifi", "PUBLISH: Wifi Aware session is not available.")
        }
    }
    fun createNetwork(peerHandle : PeerHandle, context : Context){
        this.context = context
        var connectivityManager = ConnectivityManagerHelper.getManager(context)

        val serverSocket = ServerSocket(0)
        serverSocket.soTimeout = 5000
        val port = serverSocket.localPort

        Log.d("1Wifi", "PUBLISHER: We have set new socket ports etc $port")

        var networkSpecifier = WifiAwareNetworkSpecifier.Builder(currentPubSession!!, peerHandle)
            .setPskPassphrase(Config.getConfigData()!!.getString("discoveryPassphrase"))
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
                    Log.d("NETWORKWIFI","PUBLISH: Trying to accept socket connections")
                    clientSocket = serverSocket?.accept()
                } catch (e: Exception) {
                    Log.d("NETWORKWIFI","PUBLISH: Connection failed to establish. ${e.message} stack: ${Log.getStackTraceString(e)}")
                    serverSocket?.close()
                    connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
                    return
                }
                Log.d("NETWORKWIFI","PUBLISH: DET GICK BRA")
                CoroutineScope(Dispatchers.IO).launch {
                    handleClient(clientSocket, connectivityManager)
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI","PUBLISH: onCapabilitiesChanged")
            }

            override fun onLost(network: Network) {
                Log.d("1Wifi", "PUBLISH: Connection lost: $network")
                //connectivityManager.unregisterNetworkCallback(networkCallbackPub)
            }
        }
        connectivityManager.requestNetwork(myNetworkRequest, networkCallbackPub);
    }

    private fun handleClient(clientSocket: Socket?,connectivityManager : ConnectivityManager ) {
        Log.d("1Wifi", "PUBLISH: handleClient started.")
        client.insertSingleMockedReading("publish")

        clientSocket!!.getInputStream().use { inputStream ->
            val dataInputStream = DataInputStream(inputStream)
            try {
                val size = dataInputStream.readInt()
                if (size > 0) {
                    val messageBytes = ByteArray(size)
                    dataInputStream.readFully(messageBytes)
                    try{
                        Log.d("1Wifi", "MESSAGE: $messageBytes")
                        client.insert(messageBytes)
                    }catch (e: Exception){
                        Log.d("1Wifi", "Error in inserting in handleClient" + e.message.toString())
                    }
                    Log.d("INFOFROMCLIENT", "Received protobuf message: ${client.getReadableOfSingleState(messageBytes)}")
                } else {
                    Log.d("INFOFROMCLIENT", "End of stream reached or the connection")
                }
            } catch (e: EOFException) {
                Log.d("INFOFROMCLIENT", "End of stream reached or the connection was closed.")
            } catch (e: IOException) {
                Log.e("INFOFROMCLIENT", "I/O exception: ${e.message}")
            }
        }
        Log.d("DONEEE", "PUBLISH: All information received we are done $messagesReceived, ${client.getReadableOfSingleState(client.state)}")

        utility.saveToFile(context,client.state)
        Log.d("1Wifi", "PUBLISH: UnregisterNetworkCallback")
        connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
    }

    fun shouldConnectToDevice(deviceIdentifier: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis: Long = 5 * 60 * 1000
        val deviceConnection = utility.findDevice(deviceIdentifier)

        return if (deviceConnection != null) {
            val timeSinceLastConnection = currentTime - deviceConnection.timestamp
            if (timeSinceLastConnection < fiveMinutesInMillis) {
                Log.d("1Wifi", "Publisher: Device [$deviceIdentifier] was connected ${timeSinceLastConnection / 1000} seconds ago. Not connecting again.")
                false
            }; false
        } else {
            utility.add(
                utility.createDeviceConnection(
                    deviceIdentifier,
                    System.currentTimeMillis()
                )
            )
            Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] is not in the list. Adding it and allowing connection.")
            true
        }
    }
}