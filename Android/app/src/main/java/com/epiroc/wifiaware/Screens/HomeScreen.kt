package com.epiroc.wifiaware.Screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat.startForegroundService
import com.epiroc.wifiaware.Services.BLEService
import com.epiroc.wifiaware.Services.WifiAwareService

val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.NEARBY_WIFI_DEVICES,
    Manifest.permission.BLUETOOTH_SCAN
)

@Composable
fun ServiceAwareContent(service: WifiAwareService) {

    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Button(
            onClick = {
                val nanIntent = Intent(context, WifiAwareService::class.java)
                val bleIntent = Intent(context, BLEService::class.java)
                isServiceRunning = if (!isServiceRunning) {
                    // Start the service
                    startForegroundService(context, nanIntent)
                    checkBatteryOptimizations(context)
                    //ContextCompat.startForegroundService(context, bleIntent)
                    true
                } else {
                    // Stop the service
                    service.onDestroy()
                    context.stopService(nanIntent)
                    //context.stopService(bleIntent)
                    false
                }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning) Color.Red else Color.Green
            ),
            modifier = Modifier
                .size(150.dp) // Increase the size of the button
                .padding(16.dp) // Add padding around the button
        ) {
            Text(
                text = if (isServiceRunning) "Stop" else "Start",
                color = Color.White,
                modifier = Modifier.padding(8.dp) // Add padding to content for more rounded edges
            )
        }
        // Conditionally display HomeScreen content when service is running
        if (isServiceRunning) {
            HomeScreen()
        }
    }
}


@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TODO: Maybe implement further
    }
}

fun checkBatteryOptimizations(context: Context) {
    val packageName = context.packageName
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    if (pm != null) {
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d("BatteryOptimization", "App is not on the whitelist. Asking user to disable battery optimization.")
            // App is not on the whitelist, show dialog to ask user to disable battery optimization
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)
        } else {
            Log.d("BatteryOptimization", "App is already on the whitelist.")
        }
    } else {
        Log.e("BatteryOptimization", "PowerManager is null.")
    }
}