package com.epiroc.wifiaware

import PublishActivity
import SubscribeActivity
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.aware.*
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.navigation.NavHost
import com.epiroc.wifiaware.ui.theme.WifiAwareTransportTheme


class MainActivity : ComponentActivity() {
    private var wifiAwareSession: WifiAwareSession? = null
    private val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    private var currentSession: SubscribeDiscoverySession? = null
    private val connectedDevices = mutableListOf<PeerHandle>()
    private var wifiAwareManager: WifiAwareManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        val acquisitionMessage: String
        acquisitionMessage = if (hasSystemFeature) {
            wifiAwareState(this)
            acquireWifiAwareSession(this)
        } else {
            "Wifi Aware is not supported on this device."
        }

        setContent {
            WifiAwareTransportTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WifiAwareContent(acquisitionMessage)
                }
                val navController = remeberNavConteoller()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        WifiAwareContent(acquisitionMessage, navController)
                    }
                    composable("publish") {
                        PublishActivity(navController)
                    }
                    composable("subscribe") {
                        SubscribeActivity(navController)
                    }
                }
            }
        }
    }
    private fun wifiAwareState(context: Context): String {
        var wifiAwareAvailable = ""
        wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // discard current sessions
                if (wifiAwareManager?.isAvailable == true) {
                    // I think
                    wifiAwareAvailable = "has Wifi Aware on"

                } else {
                    // Probably
                    wifiAwareAvailable = "has Wifi Aware off"
                }

            }
        }
        context.registerReceiver(myReceiver, filter)
        return wifiAwareAvailable
    }

    private fun acquireWifiAwareSession(context: Context): String {
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
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )

                return "Requesting permissions..."
            } else {
                it.attach(attachCallback, identityChangedListener, Handler(mainLooper))
                return "Wifi Aware session attached."
            }
        } ?: run {
            // Handle the case where wifiAwareManager is null.
            return "Wifi Aware manager is null."
        }
    }

    @Composable
    fun WifiAwareContent(acquisitionMessage: String) {
        var hasWifiAware by remember { mutableStateOf("Checking Wifi Aware availability...") }
        var publishMessage by remember { mutableStateOf("") }
        var subscribeMessage by remember { mutableStateOf("") }


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (checkWifiAwareAvailability()) {
                        publishMessage = "Publish Using Wifi Aware started...:" + wifiAwareSession.toString()
                        publishUsingWifiAware()
                    } else {
                        hasWifiAware = "Wifi Aware is not available."
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Publish Using Wifi Aware")
            }

            Button(
                onClick = {
                    if (checkWifiAwareAvailability()) {
                        subscribeMessage = "Subscribe to Wifi Aware Sessions started...:" + currentSession.toString()
                        subscribeToWifiAwareSessions()
                    } else {
                        hasWifiAware = "Wifi Aware is not available."
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Subscribe to Wifi Aware Sessions")
            }

            Button(
                onClick = {
                    // Start the PublishActivity when this button is clicked
                    val intent = Intent(this@MainActivity, PublishActivity::class.java)
                    startActivity(intent)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Go to Publish Activity")
            }

            Button(
                onClick = {
                    // Start the SubscribeActivity when this button is clicked
                    val intent = Intent(this@MainActivity, SubscribeActivity::class.java)
                    startActivity(intent)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Go to Subscribe Activity")
            }

            Text(
                text = acquisitionMessage,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = hasWifiAware,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = publishMessage, // Display the publish message here
                modifier = Modifier.padding(16.dp)
            )


        }
    }

    private fun checkWifiAwareAvailability(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
    }


    private fun subscribeToWifiAwareSessions() {
        if (wifiAwareSession == null) {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("subscribe_message", "SUBSCRIBE: Wifi Aware session is not available")
            }
            return
        }

        val serviceName = "epiroc_mesh" // Match the service name used for publishing.

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        val handler = Handler(mainLooper) // Use the main looper.

        val discoverySessionCallback = object : DiscoverySessionCallback() {

            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {

                currentSession = session

            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                if (peerHandle != null) {
                    currentSession!!.sendMessage(peerHandle,2,serviceSpecificInfo)
                }
            }



            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle incoming data here.
                val messageText = String(message, Charsets.UTF_8)
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit {
                    putString("subscribe_message", "SUBSCRIBE: MessageReceived from $peerHandle: $messageText")
                }

                // Optionally, you can establish a connection with the sender (Device A).
                // For simplicity, you can store the PeerHandle in a list of connected devices.
                // You should have a mechanism to manage and maintain these connections.
                connectedDevices.add(peerHandle)

                // Respond to the sender (Device A) if needed.
                val responseMessage = "Hello from Device B!"
                currentSession?.sendMessage(
                    peerHandle,
                    0, // Message type (0 for unsolicited)
                    responseMessage.toByteArray(Charsets.UTF_8)
                )
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
        }
        wifiAwareSession!!.subscribe(subscribeConfig, discoverySessionCallback, handler)
    }

    private fun publishUsingWifiAware() {
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"

            val config = PublishConfig.Builder()
                .setServiceName(serviceName)
                .build()

            val handler = Handler(mainLooper)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions are not granted, request them first.
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )
            } else {
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        //printContent("PUBLISH:  PublishStarted")
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit {
                            putString("publish_message", "PUBLISH:  MessageReceived from $peerHandle message: ${message.toString()}")
                        }
                    }
                }, handler)
            }
        } else {
            // Wi-Fi Aware session is not available.
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("publish_message", "PUBLISH: Wifi Aware session is not available")
            }
        }
    }



    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        val acquisitionMessage = "This is a sample acquisition message"

        WifiAwareTransportTheme {
            MainActivity().WifiAwareContent(acquisitionMessage)
        }
    }
}