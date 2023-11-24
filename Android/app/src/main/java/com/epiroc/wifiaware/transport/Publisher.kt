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
import java.net.SocketTimeoutException
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
    //private lateinit var networkCallbackPub: ConnectivityManager.NetworkCallback
    private var clientSocket: Socket? = null
    private val messagesReceived: MutableList<String> = mutableListOf()

    fun publishUsingWifiAware() {
        Log.d("Publisher", "PUBLISH: Attempting to start publishUsingWifiAware.")
        if (wifiAwareSession != null) {
            Log.d("Publisher", "PUBLISH: ServiceName is set to $serviceName.")
            val config = PublishConfig.Builder()
                .setServiceName(serviceName!!)
                .build()
            val handler = Handler(Looper.getMainLooper())
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("Publisher","PUBLISH: NO PREM FOR PUB")
            } else {
                Log.d("Publisher","PUBLISH: WE HAVE PREM TO PUBLISH")
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {

                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        Log.d("Publisher", "PUBLISH: Publish started")
                        currentPubSession = session

                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {

                        if (shouldConnectToDevice(String(message))) {
                            Log.d("Publisher", "PUBLISH: Message received from peer in publisher $peerHandle")
                            CoroutineScope(Dispatchers.IO).launch {
                                createNetwork(peerHandle, context,String(message))
                                Log.d("Publisher", "PUBLISH: PLEASE TELL ME THIS WORKS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        currentPubSession?.sendMessage(
                                            peerHandle,
                                            0, // Message type (0 for unsolicited),
                                            "".toByteArray()
                                        )
                                    }
                                }, 100) // Delay in milliseconds*/

                                Log.d("Publisher", "PUBLISH: sending message now via publisher to $peerHandle")
                            }


                        }
                    }
                }, handler)
            }
        } else {
            Log.d("Publisher", "PUBLISH: Wifi Aware session is not available.")
        }
    }
    fun createNetwork(peerHandle : PeerHandle, context : Context, deviceIdentifier : String){
        this.context = context
        var connectivityManager = ConnectivityManagerHelper.getManager(context)

        val serverSocket = ServerSocket(0)
        serverSocket.soTimeout = 5000
        val port = serverSocket.localPort

        Log.d("Publisher", "PUBLISHER: We have set new socket ports etc $port")

        var networkSpecifier = WifiAwareNetworkSpecifier.Builder(currentPubSession!!, peerHandle)
            .setPskPassphrase(Config.getConfigData()!!.getString("discoveryPassphrase"))
            .setPort(port)
            .build()
        var myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        Log.d("NETWORKWIFI","PUBLISH: All necessary wifiaware network things created now awaiting callback on port $port and this is port from local ${serverSocket!!.localPort}")
        val networkCallbackPub = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                currentNetwork = network

                val maxRetries = 3
                val retryDelayMillis = 2000L  // Delay between retries, e.g., 5000 milliseconds (5 seconds)
                var retryCount = 0

                while (retryCount < maxRetries) {
                    try {
                        Log.d("NETWORKWIFI", "PUBLISH: Trying to accept socket connections. Attempt: ${retryCount + 1}")

                        if (clientSocket != null && !clientSocket!!.isClosed) {
                            clientSocket!!.close()  // Close any existing client socket
                        }
                        clientSocket = serverSocket?.accept()  // Attempt to accept a connection

                        Log.d("NETWORKWIFI", "PUBLISH: Connection successful")
                        CoroutineScope(Dispatchers.IO).launch {
                            handleClient(clientSocket,deviceIdentifier)
                            serverSocket?.close()
                            clientSocket?.close()
                        }
                        break  // Break out of the loop if connection is successful
                    } catch (e: SocketTimeoutException) {
                        Log.d("NETWORKWIFI", "PUBLISH: Socket accept timed out. Retrying... (${retryCount + 1})")
                    } catch (e: Exception) {
                        Log.d("NETWORKWIFI", "PUBLISH: Connection failed to establish. ${e.message} stack: ${Log.getStackTraceString(e)}")
                        // Consider whether to break or continue retrying depending on the exception type
                        break
                    }

                    retryCount++
                    Thread.sleep(retryDelayMillis)  // Delay before the next retry
                }

                if (retryCount >= maxRetries) {
                    Log.d("NETWORKWIFI", "PUBLISH: Maximum retry attempts reached. Failed to establish connection.")
                    serverSocket?.close()
                    clientSocket?.close()
                    //connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
                }
            }


            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI", "PUBLISH: onCapabilitiesChanged")

                // Check for specific network capabilities, if relevant to your publisher logic
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    Log.d("NETWORKWIFI", "Publisher: Network has internet capability.")
                    // Handle any logic that requires the network to have internet access
                }

                // If your publisher also sends data and you want to adapt to bandwidth changes
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    val linkUpstreamBandwidthKbps = networkCapabilities.linkUpstreamBandwidthKbps
                    val linkDownstreamBandwidthKbps = networkCapabilities.linkDownstreamBandwidthKbps
                    Log.d("NETWORKWIFI", "Publisher: Link Upstream Bandwidth: $linkUpstreamBandwidthKbps Kbps, Downstream: $linkDownstreamBandwidthKbps Kbps")
                    // Adjust your data sending strategy based on available bandwidth
                }

                // Add any other relevant checks or logic specific to your application
            }

            override fun onLost(network: Network) {
                Log.d("Publisher", "PUBLISH: Connection lost: $network")
                serverSocket?.close()
                //connectivityManager.unregisterNetworkCallback(networkCallbackPub)
            }
        }
        connectivityManager.requestNetwork(myNetworkRequest, networkCallbackPub);
    }

    private fun handleClient(clientSocket: Socket?,deviceIdentifier: String ) {
        Log.d("Publisher", "PUBLISH: handleClient started adding phone to list!")
        utility.add(
            utility.createDeviceConnection(
                deviceIdentifier,
                System.currentTimeMillis()
            )
        )
        client.insertSingleMockedReading("publish")

        clientSocket!!.getInputStream().use { inputStream ->
            val dataInputStream = DataInputStream(inputStream)
            try {
                val size = dataInputStream.readInt()
                if (size > 0) {
                    val messageBytes = ByteArray(size)
                    dataInputStream.readFully(messageBytes)
                    try{
                        Log.d("Publisher", "MESSAGE: $messageBytes")
                        client.insert(messageBytes)
                    }catch (e: Exception){
                        Log.d("Publisher", "Error in inserting in handleClient" + e.message.toString())
                    }
                    Log.d("INFOFROMCLIENT", "Received protobuf message: ${client.getReadableOfSingleState(messageBytes)}")
                } else {
                    Log.d("INFOFROMCLIENT", "End of stream reached or the connection")
                }
            } catch (e: EOFException) {
                Log.d("INFOFROMCLIENT", "End of stream reached or the connection was closed.")
            } catch (e: IOException) {
                Log.e("INFOFROMCLIENT", "I/O exception: ${e.message} , ${Log.getStackTraceString(e)}")
            }
        }
        Log.d("DONEEE", "PUBLISH: All information received we are done $messagesReceived, ${client.getReadableOfSingleState(client.state)}")
        if(!clientSocket.isClosed){
            try {
                clientSocket?.getInputStream()?.close()
            }catch (e : Exception){
                Log.d("Publisher", "${e.message}")
            }

        }
        utility.saveToFile(context,client.state)
        //Log.d("1Wifi", "PUBLISH: UnregisterNetworkCallback")
        //connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
    }

    fun shouldConnectToDevice(deviceIdentifier: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis: Long = 5 * 60 * 1000
        val deviceConnection = utility.findDevice(deviceIdentifier)

        return if (deviceConnection != null) {
            val timeSinceLastConnection = currentTime - deviceConnection.timestamp
            if (timeSinceLastConnection < fiveMinutesInMillis) {
                Log.d("Publisher", "Publisher: Device [$deviceIdentifier] was connected ${timeSinceLastConnection / 1000} seconds ago. Not connecting again.")
                false
            }; false
        } else {


            Log.d("Publisher", "Publisher: Device [$deviceIdentifier] is not in the list. Adding it and allowing connection.")
            true
        }
    }
}