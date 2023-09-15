package com.epiroc.wifiaware

import android.content.pm.PackageManager
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
        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        if (hasSystemFeature) {
            hasWifiAware = "has Wifi Aware available"
        } else {
            hasWifiAware = "does not have Wifi Aware available"
        }
        setContent {
            WifiAwareTransportTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Print(hasWifiAware)
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