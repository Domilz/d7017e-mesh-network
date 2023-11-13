package com.epiroc.wifiaware

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.Screens.ServiceAwareContent
import com.epiroc.wifiaware.Screens.permissionsToRequest
import com.epiroc.wifiaware.Services.WifiAwareService


class MainActivity : ComponentActivity() {
    private var service: WifiAwareService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            service = (serviceBinder as? WifiAwareService.LocalBinder)?.getService()
            setContent {
                ServiceAwareContent(service!!)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
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

        Log.d("1Wifi","Service created in main")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest,
                123
            )
        }

        super.onCreate(savedInstanceState)

        // Here we bind to WifiAwareService
        Intent(this, WifiAwareService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        Log.d("1Wifi","Service destroyed in main")
        super.onDestroy()
        unbindService(connection)  // It's crucial to unbind the service when you're done using it.
    }
}






