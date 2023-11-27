package com.epiroc.wifiaware.Screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController
) {
    TopAppBar(navController = navController,
        ) {
        Column(
            modifier = Modifier
                .padding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(Color.Blue, CircleShape)
                    .clickable {
                        navController.navigate("transport_service_screen")
                    },
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "Scanning Service",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(Color.Blue, CircleShape)
                    .clickable {
                        navController.navigate("ble_peripheral_screen")
                    },
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "BLE Peripheral",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@SuppressLint("BatteryLife")
fun checkBatteryOptimizations(context: Context) {
    val packageName = context.packageName
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

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
}