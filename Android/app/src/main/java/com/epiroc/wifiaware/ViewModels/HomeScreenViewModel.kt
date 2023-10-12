package com.epiroc.wifiaware.ViewModels

import android.Manifest
import android.R.attr.port
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareNetworkInfo
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.Exception
import java.net.BindException
import java.net.ServerSocket
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class HomeScreenViewModel(
    val context: Context,
    private val packageManager: PackageManager
): ViewModel() {

    suspend fun checkAndRequestPermissions(activity: ComponentActivity, permissions: Array<String>): Boolean {
        val missingPermissions = permissions.filter {
            activity.checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            return true
        }

        return suspendCoroutine { continuation ->
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
                if (permissionsResult.isNotEmpty() && permissionsResult.all { it.value }) {
                    continuation.resume(true)
                } else {
                    continuation.resume(false)
                }
            }.launch(permissions)
        }
    }
}