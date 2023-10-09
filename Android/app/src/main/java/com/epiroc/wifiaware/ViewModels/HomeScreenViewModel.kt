package com.epiroc.wifiaware.ViewModels

import android.Manifest
import android.R.attr.port
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareNetworkInfo
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.Exception
import java.net.BindException
import java.net.ServerSocket


class HomeScreenViewModel(
    private val context: Context,
    private val packageManager: PackageManager
): ViewModel() {
    val hasWifiAwareText: MutableState<String> = mutableStateOf("")
    val publishMessageLiveData: MutableState<String> = mutableStateOf("")
    val subscribeMessageLiveData: MutableState<String> = mutableStateOf("")


    val currentPubString: MutableState<String> = mutableStateOf("")
    val currentSubString: MutableState<String> = mutableStateOf("")


    var wifiAwareSession: WifiAwareSession? = null
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    var currentSubSession: DiscoverySession? = null
    var currentPubSession: DiscoverySession? = null
    val connectedDevices = mutableListOf<PeerHandle>()
    val messagesReceived = mutableListOf<String>()
    var wifiAwareManager: WifiAwareManager? = null
    var connectivityManager: ConnectivityManager? = null
    var currentNetworkCapabilities: NetworkCapabilities? = null

    fun checkWifiAwareAvailability(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
    }
    fun subscribeToWifiAwareSessions() {
        if (wifiAwareSession == null) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("subscribe_message", "SUBSCRIBE: Wifi Aware session is not available")
            }
            return
        }

        val serviceName = "epiroc_mesh" // Match the service name used for publishing.

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        val handler = Handler(Looper.getMainLooper()) // Use the main looper.

        val discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {

                currentSubSession = session

            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                if (peerHandle != null) {
                    Thread.sleep(100)
                    subscribeMessageLiveData.value = "SUBSCRIBE: Connected to  $peerHandle: ${serviceSpecificInfo.toString()} ${matchFilter.toString()}"
                    val byteArrayToSend = "tag_id:\"SUBSCRIBE\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"

                    currentSubSession?.sendMessage(
                        peerHandle,
                        0, // Message type (0 for unsolicited)
                        byteArrayToSend.toByteArray(Charsets.UTF_8)
                    )

                }
            }



            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle incoming data here.
                // Optionally, you can establish a connection with the sender (Device A).
                // For simplicity, you can store the PeerHandle in a list of connected devices.
                // You should have a mechanism to manage and maintain these connections.
                connectedDevices.add(peerHandle)

                //messagesReceived.add(message.decodeToString())
                Log.d("rec","received the following:${message.decodeToString()}")

                subscribeMessageLiveData.value = "SUBSCRIBE: MessageReceived from $peerHandle message: ${message.decodeToString()}"



                val networkSpecifier =
                    currentSubSession?.let {
                        WifiAwareNetworkSpecifier.Builder(it, peerHandle)
                            .setPskPassphrase("somePassword")
                            .build()
                    }
                val myNetworkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                    .setNetworkSpecifier(networkSpecifier)
                    .build()
                val callback = object : ConnectivityManager.NetworkCallback() {

                    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                        Thread.sleep(100)
                        Log.d("onCapabilitiesChanged","SUBSCRIBE onCapabilitiesChanged incoming: $networkCapabilities")
                        currentNetworkCapabilities = networkCapabilities
                        onAvailable(network)
                    }

                    override fun onAvailable(network: Network) {
                        if(currentNetworkCapabilities != null){
                            val peerAwareInfo = currentNetworkCapabilities?.transportInfo as WifiAwareNetworkInfo
                            val peerIpv6 = peerAwareInfo.peerIpv6Addr
                            val peerPort = peerAwareInfo.port

                            val socket = network.getSocketFactory().createSocket(peerIpv6, 1337)
                            try {
                                // Get the output stream from the socket
                                val outputStream = socket.getOutputStream()

                                // Create a PrintWriter for writing data to the output stream
                                val printWriter = PrintWriter(OutputStreamWriter(outputStream))
                                val startTime = System.currentTimeMillis()
                                val duration = 10 * 60 * 1000 // 10 minutes in milliseconds
                                for(i in 1..5) {
                                    Thread.sleep(3000)
                                    val dataToSend = "Hello, server! Count: $i"
                                    printWriter.println(dataToSend)
                                    printWriter.flush()

                                    /*
                                    if(System.currentTimeMillis() - startTime > duration){
                                        break
                                    }

                                     */
                                }

                                // Close the PrintWriter and the socket when done
                                printWriter.close()
                                //socket.close()
                            } catch (e: IOException) {
                                // Handle any IO errors that may occur
                                e.printStackTrace()
                            }
                        }

                    }



                    override fun onLost(network: Network) {
                        Log.d("onLost","SUBSCRIBE CONNECTION LOST $network")
                        subscribeMessageLiveData.value = "SUBSCRIBE: the connection is lost"
                    }
                }
                //connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager?.requestNetwork(myNetworkRequest, callback);


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

    fun publishUsingWifiAware() {
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"

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
                // Permissions are not granted, request them first.
                ActivityCompat.requestPermissions(
                    context as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )
            } else {
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        currentPubSession = session
                        //printContent("PUBLISH:  PublishStarted")
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        val ss = ServerSocket(0)
                        val port = ss.localPort


                        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(currentPubSession!!, peerHandle)
                            .setPskPassphrase("somePassword")
                            .setPort(1337)
                            .build()
                        val myNetworkRequest = NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                            .setNetworkSpecifier(networkSpecifier)
                            .build()
                        val callback = object : ConnectivityManager.NetworkCallback() {
                            override fun onAvailable(network: Network) {
                                try {

                                    // Create a ServerSocket and bind it to a specific port
                                    val serverSocket = ServerSocket(1337)
                                    val clientSocket = serverSocket.accept()

                                    // Create a BufferedReader to read data from the client
                                    val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

                                    // Read data from the client in a loop
                                    var line: String?
                                    while (true) {
                                        line = reader.readLine()
                                        if (line == null || line.isEmpty()) {
                                            break // Exit the loop when no more data is available or an empty line is received
                                        }
                                        messagesReceived.add(line.toString())
                                        publishMessageLiveData.value = "PUBLISH: MessageReceived in network $network message: $line"
                                        Log.d("INFO FROM CLIENT","Received from client: $line")
                                    }
                                    publishMessageLiveData.value = "PUBLISH: while loop now done and the count of the list is ${messagesReceived.count()}"
                                    // Close the client socket
                                    clientSocket.close()



                                    // Accept incoming connections from clients





                                    // Close the reader and client socket when done
                                    reader.close()

                                }  catch (e: Exception) {
                                    if (e is BindException && e.message?.contains("Address already in use") == true) {

                                    } else {
                                        // Handle other IOExceptions
                                    }
                                }
                            }

                            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                                Thread.sleep(100)
                                Log.d("onCapabilitiesChanged","PUBLISH onCapabilitiesChanged incoming: $networkCapabilities")
                            }

                            override fun onLost(network: Network) {
                                Log.d("onLost","PUBLISH CONNECTION LOST $network")
                                publishMessageLiveData.value = "PUBLISH: the connection is lost"
                            }
                        }
                       // connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        connectivityManager?.requestNetwork(myNetworkRequest, callback);





                        publishMessageLiveData.value = "PUBLISH: MessageReceived from $peerHandle message: ${message.decodeToString()}"
                        // Respond to the sender (Device A) if needed.
                        val byteArrayToSend = "tag_id:\"PUBLISH\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"
                        currentPubSession?.sendMessage(
                            peerHandle,
                            0, // Message type (0 for unsolicited)
                            byteArrayToSend.toByteArray(Charsets.UTF_8)
                        )
                            //Thread.sleep(50)


                    }
                }, handler)
            }
        } else {
            // Wi-Fi Aware session is not available.
            publishMessageLiveData.value = ("PUBLISH: Wifi Aware session is not available")
        }

    }

    private fun wifiAwareState(): String {
        var wifiAwareAvailable = ""
        wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // discard current sessions
                if (wifiAwareManager?.isAvailable == true) {
                    hasWifiAwareText.value = "has Wifi Aware on"
                } else {
                    hasWifiAwareText.value = "has Wifi Aware off"
                }
            }
        }
        context.registerReceiver(myReceiver, filter)
        return wifiAwareAvailable
    }

    private fun acquireWifiAwareSession(): String {
        val attachCallback = object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                wifiAwareSession = session
                // Perform any necessary operations here.
            }

            override fun onAttachFailed() {
                wifiAwareSession = null
            }
        }

        val identityChangedListener = object : IdentityChangedListener() {
            override fun onIdentityChanged(peerId: ByteArray) {
                // Handle identity change if needed.
            }
        }

        wifiAwareManager?.let {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )

                return "Requesting permissions..."
            } else {
                it.attach(attachCallback, identityChangedListener, Handler(Looper.getMainLooper()))
                return "Wifi Aware session attached."
            }
        } ?: run {
            // Handle the case where wifiAwareManager is null.
            return "Wifi Aware manager is null."
        }
    }

    fun availability(): String {
        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        val acquisitionMessage: String
        acquisitionMessage = if (hasSystemFeature) {
            wifiAwareState()
            acquireWifiAwareSession()
        } else {
            "Wifi Aware is not supported on this device."
        }
        return acquisitionMessage
    }


}