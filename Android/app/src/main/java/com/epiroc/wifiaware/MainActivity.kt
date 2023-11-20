package com.epiroc.wifiaware

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri

import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.epiroc.wifiaware.Screens.Navigation
import com.epiroc.wifiaware.Services.WifiAwareService
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.ui.theme.WifiAwareTransportTheme


class MainActivity : ComponentActivity() {
    private var service: WifiAwareService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            service = (serviceBinder as? WifiAwareService.LocalBinder)?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

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
                Navigation(connection)
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d("1Wifi","Service destroyed in main")
        super.onDestroy()
        unbindService(connection)  // It's crucial to unbind the service when you're done using it.
    }
}






