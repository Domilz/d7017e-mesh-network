package com.epiroc.wifiaware

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.Screens.ServiceAwareContent
import com.epiroc.wifiaware.Screens.permissionsToRequest
import com.epiroc.wifiaware.Services.WifiAwareService
import android.Manifest


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
        super.onDestroy()
        unbindService(connection)  // It's crucial to unbind the service when you're done using it.
    }
}






