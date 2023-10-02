package com.epiroc.wifiaware

sealed class Screen(val route: String) {
    object Home: Screen(route = "home_screen")
    object Publish: Screen(route = "publish_screen")
    object Subscribe: Screen(route = "subscribe_screen")
}