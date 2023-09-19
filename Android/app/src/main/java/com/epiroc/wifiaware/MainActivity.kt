package com.epiroc.wifiaware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
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
import com.epiroc.wifiaware.ui.theme.WifiAwareTransportTheme

class MainActivity : ComponentActivity() {
    private var wifiAwareSession: WifiAwareSession? = null
    private var wifiAwareManager: WifiAwareManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var hasWifiAware: String
        val hasSystemFeature = this.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        if (hasSystemFeature) {
            wifiAwareState(this)
            hasWifiAware = "has Wifi Aware available"
            acquireWifiAwareSession(this)
            publishUsingWifiAware()
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
            }

            override fun onAttachFailed() {
                wifiAwareSession = null
            }
        }

        wifiAwareManager?.attach(attachCallback, Handler(mainLooper))
    }

    private fun publishUsingWifiAware() {
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"

            val config = PublishConfig.Builder()
                .setServiceName(serviceName)
                .build()

            val handler = Handler(mainLooper) //look into thread handling

            wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    // Handle publish started
                }

                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                    // Handle received message
                }
            }, handler)
        } else {
            // Wi-Fi Aware session is not available do something!!!
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