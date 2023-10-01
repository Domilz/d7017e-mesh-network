package com.epiroc.wifiaware

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
            }
        }
    }
    private fun wifiAwareState(context: Context) {
        wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                var wifiAwareAvailable: String
                // discard current sessions
                if (wifiAwareManager?.isAvailable == true) {
                    // I think
                    wifiAwareAvailable = "has Wifi Aware on"

                } else {
                    // Probably
                    wifiAwareAvailable = "has Wifi Aware off"
                }
                printContent(wifiAwareAvailable)
            }
        }
        context.registerReceiver(myReceiver, filter)
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
                        publishMessage = "Publish Using Wifi Aware started..."
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
                        subscribeMessage = "Subscribe to Wifi Aware Sessions started..."
                        subscribeToWifiAwareSessions()
                    } else {
                        hasWifiAware = "Wifi Aware is not available."
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Subscribe to Wifi Aware Sessions")
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

            Text(
                text = subscribeMessage, // Display the subscribe message here
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    private fun checkWifiAwareAvailability(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
    }

    private fun printContent(text: String) {
        setContent {
            WifiAwareTransportTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Print(text)
                }
            }
        }
    }

    private fun subscribeToWifiAwareSessions() {
        if (wifiAwareSession == null) {
            printContent("SUBSCRIBE: Wifi Aware session is not available")
            return
        }

        val serviceName = "epiroc_mesh" // Match the service name used for publishing.

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        val handler = Handler(mainLooper) // Use the main looper.

        val discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                //printContent("SUBSCRIBE: SubscribeStarted")
                currentSession = session
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle incoming data here.
                val messageText = String(message, Charsets.UTF_8)
                printContent("SUBSCRIBE: MessageReceived from $peerHandle: $messageText")

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
                        printContent("PUBLISH:  MessageReceived")
                    }
                }, handler)
            }
        } else {
            // Wi-Fi Aware session is not available.
            printContent("PUBLISH: Wifi Aware session is not available")
        }
    }

    @Composable
    fun Print(hasWifiAware: String, modifier: Modifier = Modifier) {
        Text(
            text = "This user $hasWifiAware",
            modifier = modifier
        )
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