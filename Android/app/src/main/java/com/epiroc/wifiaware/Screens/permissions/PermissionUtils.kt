package com.epiroc.wifiaware.Screens.permissions

import android.Manifest
import android.os.Build
import android.util.Log

object PermissionUtils {

    val servicePermission =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            listOf(
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.NEARBY_WIFI_DEVICES
            )
        }else{
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        }

    val blePeripheralPermission =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }

}