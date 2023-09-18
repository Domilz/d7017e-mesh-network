package com.epiroc.wifiaware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.aware.WifiAwareManager
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var hasWifiAware: String
        val hasSystemFeature = this.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        if (hasSystemFeature) {
            wifiAwareState(this)
            hasWifiAware = "has Wifi Aware available"
        } else {
            hasWifiAware = "does not have Wifi Aware available"
        }
        setContent {
            printContent(hasWifiAware)
        }

    }

    private fun wifiAwareState(context: Context) {
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
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