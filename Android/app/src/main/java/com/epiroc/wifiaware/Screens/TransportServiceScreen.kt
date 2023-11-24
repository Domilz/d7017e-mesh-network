package com.epiroc.wifiaware.Screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.epiroc.wifiaware.Screens.permissions.PermissionUtils
import com.epiroc.wifiaware.Services.BlePeripheralService
import com.epiroc.wifiaware.Services.BleScanningService
import com.epiroc.wifiaware.Services.WifiAwareService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TransportServiceScreen(
    navController: NavController,
) {
    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.servicePermission)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isWifiServiceRunning by remember { mutableStateOf(false) }
    var isBLEServiceRunning by remember { mutableStateOf(false) }


    var service by remember { mutableStateOf<WifiAwareService?>(null) }
    var connection by remember { mutableStateOf<ServiceConnection?>(null) }
    var nanIntent by remember { mutableStateOf<Intent?>(null) }



    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            Log.d("Wifiaware", "DisposableEffect")
            val observer = LifecycleEventObserver{_,event ->
                if(event == Lifecycle.Event.ON_START && !permissionState.allPermissionsGranted){
                    Log.d("Wifiaware", "Request Permissions")
                    permissionState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
    

    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
        Log.d("Wifiaware", "Launched Effect")
        if (permissionState.allPermissionsGranted) {
            Log.d("Wifiaware", "Create intent")

            connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
                    Log.d("Wifiaware", "onServiceConnected")
                    service = (serviceBinder as? WifiAwareService.LocalBinder)?.getService()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.d("Wifiaware", "onServiceDisconnected")
                    service = null
                }
            }

            nanIntent = Intent(context, WifiAwareService::class.java).also { intent ->
                context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
            }
        }
    }

    TopAppBar(navController = navController,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Button(
                onClick = {
                    if (nanIntent != null) {
                        isWifiServiceRunning = if (!isWifiServiceRunning) {
                            // Start the service
                            ContextCompat.startForegroundService(context, nanIntent!!)
                            checkBatteryOptimizations(context)
                            //ContextCompat.startForegroundService(context, bleIntent)
                            true
                        } else {
                            // Stop the service
                            Log.d("Wifiaware", "Stopping service")
                            service?.onDestroy()
                            context.stopService(nanIntent)
                            false
                        }
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWifiServiceRunning) Color.Red else Color.Green
                ),
                modifier = Modifier
                    .size(150.dp) // Increase the size of the button
                    .padding(16.dp) // Add padding around the button
            ) {
                Text(
                    text = if (isWifiServiceRunning) "Stop" else "Start",
                    color = Color.White,
                    modifier = Modifier.padding(8.dp) // Add padding to content for more rounded edges
                )
            }
            Button(
                onClick = {
                    val bleIntent = Intent(context, BleScanningService::class.java)
                    isBLEServiceRunning = if (!isBLEServiceRunning) {
                        ContextCompat.startForegroundService(context, bleIntent)
                        true
                    } else {
                        context.stopService(bleIntent)
                        false
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBLEServiceRunning) Color.Red else Color.Green
                ),
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp)
                ) {
                Text(text = if (isBLEServiceRunning) "Stop BLE Scan" else "Start BLE Scan",
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
            // Conditionally display HomeScreen content when service is running
            /*if (isServiceRunning) {
                HomeScreen()
            }

             */
        }

    }

}