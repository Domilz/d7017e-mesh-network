package com.epiroc.wifiaware.Screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.core.content.ContextCompat
import com.epiroc.wifiaware.Services.BLEService
import com.epiroc.wifiaware.Services.WifiAwareService

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.NEARBY_WIFI_DEVICES,
    Manifest.permission.BLUETOOTH_SCAN
)
val LocalWifiAwareService = compositionLocalOf<WifiAwareService?> { null }

@Composable
fun ServiceAwareContent(service: WifiAwareService) {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                val nanIntent = Intent(context, WifiAwareService::class.java)
                val bleIntent = Intent(context, BLEService::class.java)
                if (!isServiceRunning) {
                    // Start the service
                    ContextCompat.startForegroundService(context, nanIntent)
                    ContextCompat.startForegroundService(context, bleIntent)
                    isServiceRunning = true
                } else {
                    // Stop the service
                    context.stopService(nanIntent)
                    context.stopService(bleIntent)
                    isServiceRunning = false
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
            HomeScreen(service)
        }
    }
}


@Composable
fun HomeScreen(service: WifiAwareService) {
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
