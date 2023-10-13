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
import java.net.ServerSocket
import java.net.Socket

class WifiAwareService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private lateinit var viewModel: HomeScreenViewModel

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

        //Thread.sleep(10000)
        // Start publish and subscribe tasks
        Log.d("1Wifi","about to call viewmodel")
        if (checkWifiAwareAvailability()) {
            Log.e("1Wifi","ITS TRUE")

            //publishUsingWifiAware()
            //subscribeToWifiAwareSessions()
        } else {
            Log.d("com.epiroc.wifiaware.WifiAwareService", "Wifi Aware is not available.")
        }


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Handle any cleanup if necessary
    }


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
    val activeConnections = mutableMapOf<PeerHandle, Socket>()


    var ss: ServerSocket? = null
    var port: Int = 1337

    fun checkWifiAwareAvailability(): Boolean {
        Log.d("1Wifi","checkWifiAwareAvailability" + packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE))

        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
    }
    fun subscribeToWifiAwareSessions() {

        Log.d("1Wifi","subscribeToWifiAwareSessions")
        if (wifiAwareSession == null) {
            val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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

                if (isConnectionActive(peerHandle)) {
                    // Use the existing connection for data exchange.
                    val socket = activeConnections[peerHandle]
                    if (socket != null) {
                        handleDataExchange(peerHandle, socket)
                    }
                } else {
                    // Connection was lost, re-establish it.
                    establishConnection(peerHandle)
                }


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
        val networkSpecifier = currentSubSession?.let {
            WifiAwareNetworkSpecifier.Builder(it, peerHandle)
                .setPskPassphrase("somePassword")
                .build()
        }
        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (currentNetworkCapabilities != null) {
                    val peerAwareInfo = currentNetworkCapabilities?.transportInfo as WifiAwareNetworkInfo
                    val peerIpv6 = peerAwareInfo.peerIpv6Addr
                    val peerPort = peerAwareInfo.port
                    var socket: Socket? = null
                    try {
                        socket = network.getSocketFactory().createSocket(peerIpv6, peerPort)
                    }catch (e: Exception){
                        Log.e("ERROR", "SOCKET COULD NOT BE MADE! ${e.message}")
                    }
                    if (socket != null) {
                        storeConnection(peerHandle, socket)
                    }
                    if (socket != null) {
                        handleDataExchange(peerHandle, socket)
                    }
                    //port += 1
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                //Thread.sleep(100)
                Log.d("onCapabilitiesChanged","SUBSCRIBE onCapabilitiesChanged incoming: $networkCapabilities")
                currentNetworkCapabilities = networkCapabilities
                onAvailable(network)
            }
        }

        // Request the network and handle connection in the callback as shown above.
        connectivityManager?.requestNetwork(myNetworkRequest, callback)
    }

    private fun isConnectionActive(peerHandle: PeerHandle): Boolean {
        return activeConnections.containsKey(peerHandle)
    }

    private fun storeConnection(peerHandle: PeerHandle, socket: Socket) {
        activeConnections[peerHandle] = socket
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket) {
        val outputStream = socket.getOutputStream()
        val printWriter = PrintWriter(OutputStreamWriter(outputStream))

        for(i in 1..10000){
            val dataToSend = "HELLO, server we are subscriber! count: $i"
            printWriter.println(dataToSend)
            printWriter.flush()
        }
    }


    fun publishUsingWifiAware() {
        Log.d("1Wifi","publishUsingWifiAware",)
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"

            val config = PublishConfig.Builder()
                .setServiceName(serviceName)
                .build()
            Log.d("1Wifi","BEFORE HANDLER")
            val handler = Handler(Looper.getMainLooper())
            Log.d("1Wifi","AFTER HANDLER")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("1Wifi","NO PREM FOR PUB!!!!!!!!!!!!!!!!!!!!")
                // Permissions are not granted, request them first.
                ActivityCompat.requestPermissions(
                    this as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )
            } else {
                Log.d("1Wifi","WE HAVE PREM TO PUBLISH!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        currentPubSession = session
                        //printContent("PUBLISH:  PublishStarted")
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                       // var ss = ServerSocket(0)
                      //  val port = ss.localPort


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

                                    // Create a ServerSocket and bind it to a specific port
                                    var ss = ServerSocket(port)
                                    val clientSocket = ss.accept()

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
                                    //port += 1


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
                                Log.d("onLost", "PUBLISH CONNECTION LOST $network")
                                publishMessageLiveData.value = "PUBLISH: the connection is lost"

                                // 1. Release any held resources.
                                // Close the PublishDiscoverySession if it's not null
                                currentPubSession?.let {
                                    it.close()
                                    currentPubSession = null // Set it to null after closing
                                }

                                // Close any open ServerSocket
                                try {
                                    ss?.close() // Assuming ss is your ServerSocket instance
                                } catch (e: IOException) {
                                    Log.e("onLost", "Error while closing the server socket", e)
                                }

                                // Remove the NetworkCallback from the Connectivity Manager
                                connectivityManager?.unregisterNetworkCallback(this)

                                // 2. Re-initialize the publish logic.
                                // You may want to introduce a delay or condition to avoid aggressive re-initialization.
                                handler.postDelayed({
                                    publishUsingWifiAware()
                                }, 5000)  // delay for 5 seconds before attempting to re-initialize
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
        val attachCallback = object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                wifiAwareSession = session
                publishUsingWifiAware()
                subscribeToWifiAwareSessions()
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
                ActivityCompat.requestPermissions(
                    this as ComponentActivity, // Cast to ComponentActivity if needed
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






    // Binder to return the service instance
    inner class LocalBinder : Binder() {
        fun getService(): WifiAwareService = this@WifiAwareService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

}
