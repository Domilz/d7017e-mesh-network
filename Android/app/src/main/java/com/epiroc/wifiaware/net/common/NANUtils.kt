package com.epiroc.wifiaware.net.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object NANUtils {
    //const val REQUEST_PERMISSION = 1001

    private val runtimePermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun checkWifiSupport(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)) {
            Log.w("WifiUtils", "Wifi is not supported")
            return false
        }

        return true
    }

    fun checkWifiAwareSupport(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            Log.w("WifiUtils", "Wifi aware is not supported")
            return false
        }

        return true
    }

    fun hasPermissions(context: Context): Boolean {
        return !runtimePermissions.any {
            context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun permissionsToAsk(context: Context): Array<String> {
        return runtimePermissions.filter {
            context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}