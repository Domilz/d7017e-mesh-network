package com.epiroc.wifiaware

sealed class Screen(val route: String) {
    object Home: Screen(route = "home_screen")
}