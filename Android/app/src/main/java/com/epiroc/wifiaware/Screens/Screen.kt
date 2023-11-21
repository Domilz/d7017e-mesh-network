package com.epiroc.wifiaware.Screens

sealed class Screen(val route: String) {
    object HomeScreen: Screen(route = "home_screen")
    object TransportServiceScreen: Screen(route = "transport_service_screen")
    object BlePeripheralScreen: Screen(route = "ble_peripheral_screen")
}