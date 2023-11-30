package com.epiroc.wifiaware.transport

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.lib.Client
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.transport.network.ConnectivityManagerHelper
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.Timer
import java.util.TimerTask

class Publisher (
    ctx: Context,
    nanSession: WifiAwareSession,
    private val client: Client,
    serviceName: String?,
) {
    private var _context = ctx
    private var _currentPubSession: DiscoverySession? = null
    private var _currentNetwork : Network? = null
    private var _activeConnection : Boolean = false
    private var _responseTimer: Timer? = null
    private var _clientSocket: Socket? = null

    private val _messagesReceived: MutableList<String> = mutableListOf()
    private val _utility: WifiAwareUtility = WifiAwareUtility
    private val _wifiAwareSession = nanSession

    private val SERVICE_NAME = serviceName
    private val RESPONSE_TIMEOUT = 25000L // 15 seconds for example

    fun publishUsingWifiAware() {
        Log.d("Publisher", "PUBLISH: Attempting to start publishUsingWifiAware.")
        Log.d("Publisher", "PUBLISH: ServiceName is set to $SERVICE_NAME.")
        val config = PublishConfig.Builder()
            .setServiceName(SERVICE_NAME!!)
            .build()
        val handler = Handler(Looper.getMainLooper())
        if (ActivityCompat.checkSelfPermission(
                _context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Publisher","PUBLISH: NO PREM FOR PUB")
        } else {
            Log.d("Publisher","PUBLISH: WE HAVE PREM TO PUBLISH")
            _wifiAwareSession.publish(config, object : DiscoverySessionCallback() {
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    Log.d("Publisher", "PUBLISH: Publish started")
                    _currentPubSession = session
                }

                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                    Log.d("ActiveConnection", "PUBLISH: current state of activeConnection in onMessageReceived is $_activeConnection")
                    if (shouldConnectToDevice(String(message)) && !_activeConnection) {
                        _activeConnection = true
                        Log.d("Publisher", "PUBLISH: Message received from peer in publisher $peerHandle")
                        CoroutineScope(Dispatchers.IO).launch {
                            createNetwork(peerHandle, _context,String(message))
                            Log.d("Publisher", "PUBLISH: PLEASE TELL ME THIS WORKS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                            startResponseTimer()
                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    try {
                                        _currentPubSession?.sendMessage(
                                            peerHandle,
                                            0, // Message type (0 for unsolicited),
                                            "".toByteArray()
                                        )
                                    }catch (e : Exception){
                                        Log.e("1Wifi","Could not send message ${e.message} Stacktrace: ${Log.getStackTraceString(e)}")
                                    }
                                }
                            }, 100) // Delay in milliseconds*/
                            Log.d("Publisher", "PUBLISH: sending message now via publisher to $peerHandle")
                        }
                    }else{
                        Log.d("Publisher", "PUBLISH: we are in else in onMessageReceived, activeConnection: $_activeConnection")
                    }
                }
            }, handler)
        }
    }

    fun createNetwork(peerHandle : PeerHandle, context : Context, deviceIdentifier : String){
        this._context = context
        val connectivityManager = ConnectivityManagerHelper.getManager(context)

        val serverSocket = ServerSocket(0)
        serverSocket.soTimeout = 5000
        val port = serverSocket.localPort

        Log.d("Publisher", "PUBLISHER: We have set new socket ports etc $port")

        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(_currentPubSession!!, peerHandle)
            .setPskPassphrase(Config.getConfigData()!!.getString("discoveryPassphrase"))
            .setPort(port)
            .build()
        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        Log.d("NETWORKWIFI","PUBLISH: All necessary wifiaware network things created now awaiting callback on port $port and this is port from local ${serverSocket.localPort}")
        val networkCallbackPub = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _currentNetwork = network

                val maxRetries = 3
                val retryDelayMillis = 2000L  // Delay between retries, e.g., 5000 milliseconds (5 seconds)
                var retryCount = 0

                while (retryCount < maxRetries) {
                    try {
                        Log.d("NETWORKWIFI", "PUBLISH: Trying to accept socket connections. Attempt: ${retryCount + 1}")

                        if (_clientSocket != null && !_clientSocket!!.isClosed) {
                            _clientSocket!!.close()  // Close any existing client socket
                        }
                        _clientSocket = serverSocket.accept()  // Attempt to accept a connection (TODO: maybe val clientSocket)
                        _responseTimer?.cancel()
                        Log.d("NETWORKWIFI", "PUBLISH: Connection successful")
                        CoroutineScope(Dispatchers.IO).launch {
                            handleClient(_clientSocket,deviceIdentifier)
                            withContext(Dispatchers.IO) {
                                serverSocket.close()
                                _clientSocket?.close()
                            }
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

                Log.d("ActiveConnection", "PUBLISH: current state of activeConnection in onAvailable is $_activeConnection")
                _activeConnection = false

                if (retryCount >= maxRetries) {
                    Log.d("NETWORKWIFI", "PUBLISH: Maximum retry attempts reached. Failed to establish connection.")
                    serverSocket.close()
                    _clientSocket?.close()
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI", "PUBLISH: onCapabilitiesChanged")

                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    Log.d("NETWORKWIFI", "Publisher: Network has internet capability.")
                }

                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    val linkUpstreamBandwidthKbps = networkCapabilities.linkUpstreamBandwidthKbps
                    val linkDownstreamBandwidthKbps = networkCapabilities.linkDownstreamBandwidthKbps
                    Log.d("NETWORKWIFI", "Publisher: Link Upstream Bandwidth: $linkUpstreamBandwidthKbps Kbps, Downstream: $linkDownstreamBandwidthKbps Kbps")
                }
            }

            override fun onLost(network: Network) {
                Log.d("Publisher", "PUBLISH: Connection lost: $network")
                serverSocket.close()
                Log.d("ActiveConnection", "PUBLISH: current state of activeConnection in onLost is $_activeConnection")
                _activeConnection = false
            }
        }
        connectivityManager.requestNetwork(myNetworkRequest, networkCallbackPub)
    }

    private fun handleClient(clientSocket: Socket?,deviceIdentifier: String ) {
        Log.d("Publisher", "PUBLISH: handleClient started adding phone to list!")
        _utility.add(
            _utility.createDeviceConnection(
                deviceIdentifier,
                System.currentTimeMillis()
            )
        )

        clientSocket!!.getInputStream().use { inputStream ->
            val dataInputStream = DataInputStream(inputStream)
            try {
                val size = dataInputStream.readInt()
                if (size > 0) {
                    val messageBytes = ByteArray(size)
                    dataInputStream.readFully(messageBytes)
                    try{
                        Log.d("Publisher", "MESSAGE: $messageBytes")
                        client.tagClient.insert(messageBytes)
                    }catch (e: Exception){
                        Log.d("Publisher", "Error in inserting in handleClient" + e.message.toString())
                    }
                    Log.d("INFOFROMCLIENT", "Received protobuf message: ${client.tagClient.getReadableOfProvidedSerializedState(messageBytes)}")
                } else {
                    Log.d("INFOFROMCLIENT", "End of stream reached or the connection")
                }
            } catch (e: EOFException) {
                Log.d("INFOFROMCLIENT", "End of stream reached or the connection was closed.")
            } catch (e: IOException) {
                Log.e("INFOFROMCLIENT", "I/O exception: ${e.message} , ${Log.getStackTraceString(e)}")
            }
        }
        Log.d("DONEEE", "PUBLISH: All information received we are done $_messagesReceived, ${client.tagClient.deserializedState}")
        if(!clientSocket.isClosed){
            try {
                clientSocket.getInputStream()?.close()
            }catch (e : Exception){
                Log.d("Publisher", "${e.message}")
            }
        }
        _utility.saveToFile(_context,client.tagClient.serializedState)
    }

    fun shouldConnectToDevice(deviceIdentifier: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis: Long = 5 * 60 * 1000
        val deviceConnection = _utility.findDevice(deviceIdentifier)

        return if (deviceConnection != null) {
            val timeSinceLastConnection = currentTime - deviceConnection.timestamp
            if (timeSinceLastConnection < fiveMinutesInMillis) {
                Log.d("Publisher", "Publisher: Device [$deviceIdentifier] was connected ${timeSinceLastConnection / 1000} seconds ago. Not connecting again.")
            }; false
        } else {
            Log.d("Publisher", "Publisher: Device [$deviceIdentifier] is not in the list. Adding it and allowing connection.")
            true
        }
    }

    private fun startResponseTimer() {
        _responseTimer?.cancel()
        Log.d("Publisher", "TIMER: Starting response timer")
        _responseTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.d("Publisher", "TIMER: Response timeout")
                    _activeConnection = false
                }
            }, RESPONSE_TIMEOUT)
        }
    }
}


