package com.epiroc.wifiaware.Services


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModelFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.Exception
import java.net.BindException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class WifiAwareService : Service() {
    private lateinit var serviceUUID: String
    data class DeviceConnection(val deviceIdentifier: String, val timestamp: Long)


    val hasWifiAwareText: MutableState<String> = mutableStateOf("")
    val publishMessageLiveData: MutableState<String> = mutableStateOf("")
    val networkMessageLiveData: MutableState<String> = mutableStateOf("")
    val uuidMessageLiveData: MutableState<String> = mutableStateOf("")
    val subscribeMessageLiveData: MutableState<String> = mutableStateOf("")


    var wifiAwareSession: WifiAwareSession? = null
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    var currentSubSession: DiscoverySession? = null
    var currentPubSession: DiscoverySession? = null
    val recentlyConnectedDevices = mutableListOf<DeviceConnection>()
    val messagesReceived = mutableListOf<String>()
    var wifiAwareManager: WifiAwareManager? = null
    var connectivityManager: ConnectivityManager? = null
    var currentNetworkCapabilities: NetworkCapabilities? = null
    //val activeConnections = mutableMapOf<PeerHandle, Socket>()
    var callbackSub: ConnectivityManager.NetworkCallback? = null

    var cs: Socket? = null

    override fun onCreate() {
        Log.d("1Wifi","Service created")
        serviceUUID = UUID.randomUUID().toString()
        uuidMessageLiveData.value = serviceUUID
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "WifiAware Service Channel"
            val description = "Channel for WifiAware foreground service"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                this.description = description
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "wifiAwareServiceChannel"
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("1Wifi","STARTED")

        // Show notification for the foreground service
        val intent = Intent(this, MainActivity::class.java).apply {
            flags
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WifiAware Service Running")
            .setContentText("Service is running in the background...")
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)


        // Initialize the ViewModel
        //viewModel = HomeScreenViewModel(this, packageManager)
        availability()

        val cleanUpHandler = Handler(Looper.getMainLooper())
        val cleanUpRunnable = object: Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val fiveMinutesInMillis: Long = 5 * 60 * 1000
                recentlyConnectedDevices.removeIf { currentTime - it.timestamp > fiveMinutesInMillis }
                cleanUpHandler.postDelayed(this, fiveMinutesInMillis)
            }
        }

        // To start cleanup process
        cleanUpHandler.post(cleanUpRunnable)

        //Thread.sleep(10000)
        // Start publish and subscribe tasks


        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("1Wifi","Service destroyed")
        super.onDestroy()
        // Handle any cleanup if necessary
    }



    fun shouldConnectToDevice(deviceIdentifier: String): Boolean {

        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMillis: Long = 5 * 60 * 1000

        val deviceConnection = recentlyConnectedDevices.find { it.deviceIdentifier == deviceIdentifier }

        if (deviceConnection != null) {
            val timeSinceLastConnection = currentTime - deviceConnection.timestamp
            if (timeSinceLastConnection < fiveMinutesInMillis) {
                Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] was connected ${timeSinceLastConnection / 1000} seconds ago. Not connecting again.")
                return false
            } else {
                // Update the timestamp for this device since we're going to reconnect.
                Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] was connected more than 5 minutes ago. Updating timestamp and reconnecting.")
                recentlyConnectedDevices.remove(deviceConnection)
                recentlyConnectedDevices.add(DeviceConnection(deviceIdentifier, currentTime))
                return true
            }
        } else {
            // Device is not in the list. Add it and allow the connection.
            Log.d("1Wifi", "SUBSCRIBE: Device [$deviceIdentifier] is not in the list. Adding it and allowing connection.")
            recentlyConnectedDevices.add(DeviceConnection(deviceIdentifier, currentTime))
            return true
        }
    }


    fun subscribeToWifiAwareSessions() {
        Log.d("1Wifi","SUBSCRIBE: subscribeToWifiAwareSessions called")

        if (wifiAwareSession == null) {
            val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("subscribe_message", "SUBSCRIBE: Wifi Aware session is not available")
            }
            Log.d("1Wifi","SUBSCRIBE: Wifi Aware session is not available")
            return
        }

        val serviceName = "epiroc_mesh" // Match the service name used for publishing.

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
                if (peerHandle != null && shouldConnectToDevice(peerHandle.toString()))   {
                    Log.d("1Wifi", "SUBSCRIBE: We Connected to $serviceUUID In the sub")
                    Thread.sleep(100)
                    recentlyConnectedDevices.add(DeviceConnection(peerHandle.toString(),System.currentTimeMillis()))
                    subscribeMessageLiveData.value = "SUBSCRIBE: Connected to  $serviceUUID                                                      THESE ARE THE DETAILS:($peerHandle: ${serviceSpecificInfo.toString()} ${matchFilter.toString()})"
                    val byteArrayToSend = "tag_id:\"SUBSCRIBE\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"
                    Log.d("1Wifi", "SUBSCRIBE: we are sending a message now")
                    currentSubSession?.sendMessage(
                        peerHandle,
                        0, // Message type (0 for unsolicited)
                        serviceUUID.toByteArray(Charsets.UTF_8)
                    )

                }else{
                    Log.d("1Wifi", "SUBSCRIBE: Device has already been discovered "+peerHandle.toString())
                }

            }



            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle incoming data here.
                subscribeMessageLiveData.value = "SUBSCRIBE: GOT A MESSAGE FROM THE FOLLOWING UUID " + String(message, Charsets.UTF_8)

                Log.d("1Wifi", "SUBSCRIBE: Message received from peer: $peerHandle")
                establishConnection(peerHandle)

            }
        }

        // Subscribe to WiFi Aware sessions.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permissions here if needed.
        }
        wifiAwareSession!!.subscribe(subscribeConfig, discoverySessionCallback, handler)
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
        if(callbackSub == null){
            callbackSub = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d("1Wifi", "SUBSCRIBE: Network available for peer: $peerHandle")
                    if (currentNetworkCapabilities != null) {
                        val peerAwareInfo = currentNetworkCapabilities?.transportInfo as WifiAwareNetworkInfo
                        val peerIpv6 = peerAwareInfo.peerIpv6Addr
                        val peerPort = peerAwareInfo.port
                        var socket: Socket? = null
                        try {
                            socket = network.getSocketFactory().createSocket() // Don't pass the address and port here.
                            socket.connect(InetSocketAddress(peerIpv6, peerPort), 5000)
                        } catch (e: Exception) {
                            Log.e("1Wifi", "SUBSCRIBE: ERROR SOCKET COULD NOT BE MADE! ${e.message}")
                        }
                        if (socket != null) {
                            handleDataExchange(peerHandle, socket)
                        }
                        //port += 1
                        socket?.close()
                        currentNetworkCapabilities = null
                    }
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    //Thread.sleep(100)
                    Log.d("1Wifi", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")

                    currentNetworkCapabilities = networkCapabilities
                    onAvailable(network)
                }

                override fun onLost(network: Network) {
                    Log.d("1Wifi", "SUBSCRIBE: Network lost for peer: $peerHandle")
                    callbackSub?.let { handleSubNetworkLost(it) }
                }
            }
        }


        // Request the network and handle connection in the callback as shown above.
        connectivityManager?.requestNetwork(myNetworkRequest,
            callbackSub as ConnectivityManager.NetworkCallback
        )
    }





    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket) {
        Log.d("1Wifi", "SUBSCRIBE: Attempting to send information to: $peerHandle")
        val outputStream = socket.getOutputStream()
        val printWriter = PrintWriter(OutputStreamWriter(outputStream))

        for(i in 1..100){
            var dataToSend = ""
            dataToSend = if (i==100){
                "DONE"
            }else{
                "HELLO, server we are subscriber! count: $i"
            }

            printWriter.println(dataToSend)
            printWriter.flush()

        }
        Log.d("1Wifi", "SUBSCRIBE: All information sent we are done")
        /* val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

         val confirmation = reader.readLine()
         if (confirmation == "CONFIRMATION") {
             // Send an acknowledgment back to the publisher
             val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()))
             writer.println("ACK")
             writer.flush()
             Log.d("DataExchange", "SUBSCRIBE: Sent ACK to publisher")

         }*/
        //Log.d("1Wifi", "SUBSCRIBE: Trigger the handleSubNetworkLost function")
        //callbackSub?.let { handleSubNetworkLost(it) } // Trigger the handleSubNetworkLost function
    }

    private fun handleSubNetworkLost(callback: ConnectivityManager.NetworkCallback) {
        Log.d("1Wifi", "SUBSCRIBE: SUBSCRIBE CONNECTION LOST")

        // Close the SubscribeDiscoverySession
        currentSubSession?.let {
            it.close()
            currentSubSession = null // Set it to null after closing
        }

        // Close any open ClientSocket
        try {
            cs?.close()
        } catch (e: IOException) {
            Log.d("1Wifi", "SUBSCRIBE: Error while closing the client socket", e)
        }

        // Remove the NetworkCallback from the Connectivity Manager
        connectivityManager?.unregisterNetworkCallback(callback)

        // Re-initialize the subscribe logic after a delay
        Timer().schedule(object : TimerTask() {
            override fun run() {
                subscribeToWifiAwareSessions()
            }
        }, 1000) // Delay in milliseconds
    }



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
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
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
                        currentPubSession = session
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        Log.d("1Wifi", "PUBLISH: Message received from peer in publisher $peerHandle")
                        val ss = ServerSocket(0)
                        val port = if (ss.localPort != -1) ss.localPort else 1337

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
                                    networkMessageLiveData.value = "NETWORK: we are ???${network}"
                                    val clientSocket = ss.accept()
                                    handleClient(clientSocket)
                                    Log.d("1Wifi", "PUBLISH: Accepting client ${network}")
                                } catch (e: Exception) {
                                    Log.e("1Wifi", "PUBLISH: ERROR Exception while accepting client", e)
                                }
                            }



                            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                                networkMessageLiveData.value = "NETWORK: we are ???${network}"
                                Log.d("1Wifi", "PUBLISH: onCapabilitiesChanged incoming: $networkCapabilities")
                            }

                            override fun onLost(network: Network) {
                                networkMessageLiveData.value = "NETWORK: we are ???${network}"
                                Log.d("1Wifi", "PUBLISH: Connection lost: $network")
                                handleNetworkLost()
                            }
                        }
                        // connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        connectivityManager?.requestNetwork(myNetworkRequest, callback);





                        //publishMessageLiveData.value = "PUBLISH: MessageReceived from $peerHandle message: ${message.decodeToString()}"
                        // Respond to the sender (Device A) if needed.
                        val byteArrayToSend = "tag_id:\"PUBLISH\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"
                        Log.d("1Wifi", "PUBLISH: eeding message in one sec via publisher to $peerHandle")
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                currentPubSession?.sendMessage(
                                    peerHandle,
                                    0, // Message type (0 for unsolicited)
                                    serviceUUID.toByteArray(Charsets.UTF_8)
                                )
                            }
                        }, 1000) // Delay in milliseconds

                        //Thread.sleep(50)


                    }
                }, handler)
            }
        } else {
            Log.d("1Wifi", "PUBLISH: Wifi Aware session is not available.")
            publishMessageLiveData.value = ("PUBLISH: Wifi Aware session is not available")
        }
    }

    private fun handleClient(clientSocket: Socket) {
        Log.d("1Wifi", "PUBLISH: handleClient started.")
        val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val writer = PrintWriter(OutputStreamWriter(clientSocket.getOutputStream()))
        var line: String?
        while (true) {
            line = reader.readLine()
            if (line.isNullOrEmpty() || line.toString() == "DONE") {
                break
            }
            messagesReceived.add(line.toString())
            Log.d("INFOFROMCLIENT", "Received from client: $line")
        }
        Log.d("1Wifi", "PUBLISH: All information received we are done")
      /*  // After reading all messages, send a confirmation
        writer.println("CONFIRMATION")
        writer.flush()

        // Wait for acknowledgment from the subscriber
        line = reader.readLine()
        if (line == "ACK") {
            Log.d("DataEx", "PUBLISH: Received ACK from client")
        }*/

        publishMessageLiveData.value = "PUBLISH: Messages received count: ${messagesReceived.count()}"
        //clientSocket.close()
        //handleNetworkLost()
    }


    private fun handleNetworkLost() {
        Log.d("1Wifi", "Network lost. Handle it now.")

        // Close the PublishDiscoverySession
        currentPubSession?.close()
        currentPubSession = null

        // Re-initialize the publish logic after a delay
        Timer().schedule(object : TimerTask() {
            override fun run() {
                publishUsingWifiAware()
            }
        }, 1000)
    }

    private fun wifiAwareState(): String {
        Log.d("1Wifi", "Checking Wifi Aware state.")
        var wifiAwareAvailable = ""
        wifiAwareManager = this.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        this.registerReceiver(myReceiver, filter)
        return wifiAwareAvailable
    }

    private fun acquireWifiAwareSession(): String {
        Log.d("1Wifi", "Attempting to acquire Wifi Aware session.")
        val attachCallback = object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                wifiAwareSession = session
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        publishUsingWifiAware()
                        subscribeToWifiAwareSessions()
                    }
                }, 1000) // Delay in milliseconds

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
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Permissions not granted."
            } else {
                it.attach(attachCallback, identityChangedListener, Handler(Looper.getMainLooper()))
                return "Wifi Aware session attached."
            }
        } ?: run {
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






    // Binder to return the service instance
    inner class LocalBinder : Binder() {
        fun getService(): WifiAwareService = this@WifiAwareService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

}
