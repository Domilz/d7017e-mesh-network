package com.epiroc.wifiaware

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import android.net.wifi.aware.WifiAwareSession
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.ui.theme.WifiAwareTransportTheme

class MainActivity : ComponentActivity() {
    private var wifiAwareSession: WifiAwareSession? = null
    private var wifiAwareManager: WifiAwareManager? = null
    private var currentSession: SubscribeDiscoverySession? = null
    private val connectedDevices = mutableListOf<PeerHandle>()
    private val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var hasWifiAware: String
        val hasSystemFeature = this.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        if (hasSystemFeature) {
            wifiAwareState(this)
            hasWifiAware = "has Wifi Aware available"
            acquireWifiAwareSession(this)
            publishUsingWifiAware() // Publish a session on this device.
            subscribeToWifiAwareSessions() // Subscribe to sessions on this device.
        } else {
            hasWifiAware = "does not have Wifi Aware available"
        }
        setContent {
            printContent(hasWifiAware)
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

    private fun acquireWifiAwareSession(context: Context) {
        val attachCallback = object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                wifiAwareSession = session
                publishUsingWifiAware()
            }

            override fun onAttachFailed() {
                wifiAwareSession = null
            }
        }

        val identityChangedListener = object : IdentityChangedListener() {
            override fun onIdentityChanged(peerId: ByteArray) {
                printContent("this is the identity changed "+peerId.toString())
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



                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }else{
                printContent("has access")
            }
            it.attach(attachCallback, identityChangedListener, Handler(mainLooper))
        } ?: run {
            // Handle the case where wifiAwareManager is null, e.g., log an error or show a message.
            // You can also consider adding error handling or fallback logic here.
            wifiAwareSession = null
        }
    }




    private fun publishUsingWifiAware() {
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"

            val config = PublishConfig.Builder()
                .setServiceName(serviceName)
                .build()

            val handler = Handler(mainLooper) //look into thread handling idk if this is correct


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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    printContent( "PUBLISH:  PublishStarted")
                }

                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                    printContent( "PUBLISH:  MessageReceived")

                }
            }, handler)
        } else {
            // Wi-Fi Aware session is not available do something!!!
            printContent( "PUBLISH: Wifi Aware session is not available")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Both permissions are granted. You can proceed with WiFi Aware operations.
                printContent("has access")
            } else {
                // Permissions are denied. Handle the case where the user denied permissions.
                // You may show a message or take appropriate action.
            }
        }
    }

    private fun subscribeToWifiAwareSessions() {
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh" // Match the service name used for publishing.

            val subscribeConfig = SubscribeConfig.Builder()
                .setServiceName(serviceName)
                .build()

            val handler = Handler(mainLooper) // Use the main looper.

            val discoverySessionCallback = object : DiscoverySessionCallback() {
                override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                    printContent("SUBSCRIBE: SubscribeStarted")
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            wifiAwareSession!!.subscribe(subscribeConfig, discoverySessionCallback, handler)
        } else {
            // Wi-Fi Aware session is not available. Handle this case appropriately.
            printContent("SUBSCRIBE: Wifi Aware session is not available")
        }
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
    WifiAwareTransportTheme {
        Print("Android")
    }
}