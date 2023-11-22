package com.epiroc.wifiaware.Screens

import android.content.ServiceConnection
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){
        composable(Screen.HomeScreen.route){
            HomeScreen(navController = navController)
        }

        composable(Screen.TransportServiceScreen.route){
            TransportServiceScreen(navController = navController)
        }

        composable(Screen.BlePeripheralScreen.route) {
            PeripheralScreen(navController = navController)
        }

    }
}