package com.epiroc.wifiaware.net

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import com.epiroc.wifiaware.net.common.BaseManager
import com.epiroc.wifiaware.screens.permissionsToRequest
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.Timer
import java.util.TimerTask

class Publisher(
    context: Context, serviceName: String,
    private val callback: Callback,
    nanSession: WifiAwareSession,
    cManager: ConnectivityManager,


): BaseManager(context) {

    interface Callback {
        fun onServicePublished()
        fun onMessageSendResult(id: Int, success: Boolean)
        fun onMessageReceived(message: ByteArray)
    }

    private var connectivityManager = cManager
    private var discoverySession: DiscoverySession? = null
    private var subscriberHandle: PeerHandle? = null
    private var serverSocket: ServerSocket? = null
    private var currentPubSession: DiscoverySession? = null

    private val messagesReceived: MutableList<String> = mutableListOf<String>()

    private val wifiAwareSession = nanSession

    private val config: PublishConfig = PublishConfig.Builder()
        .setServiceName(serviceName)
        .build()

    private val discoverySessionCallback = object : DiscoverySessionCallback() {

        override fun onPublishStarted(session: PublishDiscoverySession) {
            super.onPublishStarted(session)
            discoverySession = session
            callback.onServicePublished()
        }

        override fun onMessageSendSucceeded(messageId: Int) {
            super.onMessageSendSucceeded(messageId)
            callback.onMessageSendResult(messageId, true)
        }

        override fun onMessageSendFailed(messageId: Int) {
            super.onMessageSendFailed(messageId)
            callback.onMessageSendResult(messageId, false)
        }

        override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray?) {
            super.onMessageReceived(peerHandle, message)
            subscriberHandle = peerHandle
            callback.onMessageReceived(message ?: ByteArray(0))
        }
    }

    override fun onSessionAttached() {
        super.onSessionAttached()
        publish()
    }

    private fun publish() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        activeSession?.publish(config, discoverySessionCallback, null)
    }

    fun sendMessage(id: Int, data: ByteArray) {
        subscriberHandle?.let {
            discoverySession?.sendMessage(it, id, data)
        }
    }

    override fun stop() {
        discoverySession?.close()
        super.stop()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun publishUsingWifiAware() {
        Log.d("1Wifi", "PUBLISH: Attempting to start publishUsingWifiAware.")
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"
            Log.d("1Wifi", "PUBLISH: ServiceName is set to $serviceName.")

            val config = PublishConfig.Builder()
                .setServiceName(serviceName)
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
                // Permissions are not granted, request them first.
                ActivityCompat.requestPermissions(
                    this as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )
            } else {
                Log.d("1Wifi","PUBLISH: WE HAVE PREM TO PUBLISH")
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        Log.d("1Wifi", "PUBLISH: Publish started")
                        var currentPubSession = session
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        Log.d("1Wifi", "PUBLISH: Message received from peer in publisher $peerHandle")

                        if (serverSocket == null || serverSocket!!.isClosed) {
                            serverSocket = ServerSocket(0)
                        }

                        val port = if (serverSocket!!.localPort != -1) serverSocket!!.localPort else 1337

                        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(currentPubSession!!, peerHandle)
                            .setPskPassphrase("somePassword")
                            .setPort(port)
                            .build()
                        val myNetworkRequest = NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                            .setNetworkSpecifier(networkSpecifier)
                            .build()

                        val callback = object : ConnectivityManager.NetworkCallback() {
                            override fun onAvailable(network: Network) {
                                try {
                                    //networkMessageLiveData.value = "NETWORK: we are ??? $network"
                                    val clientSocket = serverSocket!!.accept()
                                    handleClient(clientSocket)
                                    Log.d("1Wifi", "PUBLISH: Accepting client $network")
                                } catch (e: Exception) {
                                    Log.e("1Wifi", "PUBLISH: ERROR Exception while accepting client", e)
                                }
                            }

                            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                                //networkMessageLiveData.value = "NETWORK: we are ???${network}"
                                Log.d("1Wifi", "PUBLISH: onCapabilitiesChanged incoming: $networkCapabilities")
                            }

                            override fun onLost(network: Network) {
                                //networkMessageLiveData.value = "NETWORK: Connection lost: $network"
                                Log.d("1Wifi", "PUBLISH: Connection lost: $network")
                                currentPubSession?.close()
                                currentPubSession = null
                                wifiAwareSession?.close()
                                try {
                                    serverSocket?.close()
                                    serverSocket = null
                                } catch (e: IOException) {
                                    Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
                                }
                                Log.e("1Wifi", "PUBLISH: EVERYTHING IN PUBLISH IS NOW CLOSED")
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        //TO-DO: Implement
                                    }
                                }, 10000) // Delay in milliseconds
                            }
                        }
                        //connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        connectivityManager?.requestNetwork(myNetworkRequest, callback);

                        //publishMessageLiveData.value = "PUBLISH: MessageReceived from $peerHandle message: ${message.decodeToString()}"
                        // Respond to the sender (Device A) if needed.
                        //val byteArrayToSend = "tag_id:\"PUBLISH\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"
                        Log.d("1Wifi", "PUBLISH: sending message now via publisher to $peerHandle")


                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                currentPubSession?.sendMessage(
                                    peerHandle,
                                    0, // Message type (0 for unsolicited)
                                    "hej".toByteArray()
                                )
                            }
                        }, 1000) // Delay in milliseconds*/

                        //Thread.sleep(50)


                    }
                }, handler)
            }
        } else {
            Log.d("1Wifi", "PUBLISH: Wifi Aware session is not available.")
            //publishMessageLiveData.value = ("PUBLISH: Wifi Aware session is not available")
        }
    }
    private fun closeServerSocket() {
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
        }
    }

    private fun handleClient(clientSocket: Socket) {
        Log.d("1Wifi", "PUBLISH: handleClient started.")
        clientSocket.getInputStream().bufferedReader().use { reader ->
            reader.lineSequence().forEach { line ->
                messagesReceived.add(line)
                Log.d("INFOFROMCLIENT", "Received from client: $line")
            }
        }
        Log.d("1Wifi", "PUBLISH: All information received we are done")
        //publishMessageLiveData.value = "PUBLISH: Messages received count: ${messagesReceived.count()}"
        Log.d("DONEEE", "publisherDone = true")
        //publisherDone = true
    }
}