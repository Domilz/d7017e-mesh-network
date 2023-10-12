package com.epiroc.wifiaware.Screens

import android.Manifest
import android.content.Intent
import android.net.wifi.aware.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.epiroc.wifiaware.Screen
import com.epiroc.wifiaware.Services.WifiAwareForegroundService
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


lateinit var navController: NavHostController
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeScreenViewModel){

    val context = LocalContext.current
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )
    val activity = LocalContext.current as ComponentActivity
    val coroutineScope = rememberCoroutineScope()

    // Use LaunchedEffect to run a coroutine when the composable is initially displayed
    LaunchedEffect(viewModel) {
        coroutineScope.launch {
            viewModel.checkAndRequestPermissions(activity,permissionsToRequest)
        }
    }


    Button(
        onClick = {
            // Start the WiFi Aware service or perform some action
            Log.d("buttonClick","CLICKED")
            val serviceIntent = Intent(context, WifiAwareForegroundService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Start WiFi Aware")
    }
}


