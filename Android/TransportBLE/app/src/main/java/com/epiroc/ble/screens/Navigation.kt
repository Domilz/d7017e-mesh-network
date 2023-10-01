package com.epiroc.ble.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(
    onBluetoothStateChanged:()->Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){
        composable(Screen.HomeScreen.route){
            HomeScreen(navController = navController)
        }

        composable(Screen.BleListScreen.route){
            BleListScreen(onBluetoothStateChanged)
        }
    }
}

sealed class Screen(val route:String) {
    object HomeScreen:Screen("home_screen")
    object BleListScreen:Screen("ble_list_screen")
}