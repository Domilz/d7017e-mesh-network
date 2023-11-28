package com.epiroc.wifiaware.Screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PeripheralScreen(
    navController: NavController
) {
    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.blePeripheralPermission)
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    var isPeripheralServiceRunning by remember { mutableStateOf(false) }
    var beacon1 by remember { mutableStateOf(false) }
    var beacon2 by remember { mutableStateOf(false) }


    var rpID by remember { mutableStateOf("NO:ID:SET") }

    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            Log.d("PeripheralScreen", "DisposableEffect")

            val observer = LifecycleEventObserver{_,event ->
                if(event == Lifecycle.Event.ON_START && !permissionState.allPermissionsGranted){
                    permissionState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    LaunchedEffect(key1 = permissionState.allPermissionsGranted){
        Log.d("PeripheralScreen", "LaunchedEffect")
        if(permissionState.allPermissionsGranted){
            Log.d("PeripheralScreen", "All permissions granted")
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
            Button(onClick = {
                beacon1 = if (!beacon1) {
                    rpID = "rpId1"
                    beacon2 = false
                    true
                } else {
                    rpID = "NO:ID:SET"
                    false
                }
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (beacon1) Color.Red else Color.Green
                ),
                modifier = Modifier
                    .size(200.dp) // Increase the size of the button
                    .padding(8.dp)
            ) {
                Text(
                    text = "rpId1",
                    color = Color.White,
                    modifier = Modifier.padding(6.dp) // Add padding to content for more rounded edges
                )
            }
            Button(onClick = {
                beacon2 = if (!beacon2) {
                    rpID = "rpId2"
                    beacon1 = false
                    true
                } else {
                    rpID = "NO:ID:SET"
                    false
                }
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (beacon2) Color.Red else Color.Green
                ),
                modifier = Modifier
                    .size(200.dp) // Increase the size of the button
                    .padding(8.dp)
            ) {
                Text(
                    text = "rpId2",
                    color = Color.White,
                    modifier = Modifier.padding(6.dp) // Add padding to content for more rounded edges
                )
            }
            Button(
                onClick = {
                    val intent = Intent(context, BlePeripheralService::class.java)

                    isPeripheralServiceRunning = if (!isPeripheralServiceRunning) {
                        // Start the service
                        intent.putExtra("rpID", rpID)
                        ContextCompat.startForegroundService(context, intent)
                        Log.d("PeripheralScreen", "Starting foreground")
                        true
                    } else {
                        // Stop the service
                        context.stopService(intent)
                        false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPeripheralServiceRunning) Color.Yellow else Color.Blue
                ),
                modifier = Modifier
                    .size(200.dp) // Increase the size of the button
                    .padding(8.dp) // Add padding around the button
            ) {
                Text(
                    text = if (isPeripheralServiceRunning) "Stop Advertising" else "Start Advertising",
                    color = Color.White,
                    modifier = Modifier.padding(7.dp) // Add padding to content for more rounded edges
                )
            }
        }
    }

}


