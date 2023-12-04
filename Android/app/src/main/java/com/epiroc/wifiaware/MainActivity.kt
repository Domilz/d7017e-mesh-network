package com.epiroc.wifiaware

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.epiroc.wifiaware.Screens.Navigation
import com.epiroc.wifiaware.lib.Client
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.ui.theme.WifiAwareTransportTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Random
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var client : Client
    private lateinit var byteArray: ByteArray

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        Config.loadConfig(this)

        window.navigationBarColor = Color.Black.toArgb()
        window.statusBarColor = Color.Black.toArgb()

        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        setContent{
            WifiAwareTransportTheme {
                Navigation()
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val random = Random()
        byteArray = ByteArray(16) // 16 bytes for a UUID
        random.nextBytes(byteArray)
        client.setupClient(byteArray.toString())
    }

    override fun onDestroy() {
        Log.d("1Wifi","Service destroyed in main")
        super.onDestroy()
    }
}






