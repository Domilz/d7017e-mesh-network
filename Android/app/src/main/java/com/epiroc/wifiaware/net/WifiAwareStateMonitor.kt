package com.epiroc.wifiaware.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.aware.WifiAwareManager

class WifiAwareStateMonitor(private val context: Context) {

    interface StateCallback {
        fun onWifiAwareAvailable(available: Boolean)
    }

    private val awareManager by lazy {
        context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
    }

    private var initialized = false
    private val stateCallbacks = hashSetOf<StateCallback>()

    private val intentFilter = IntentFilter().apply {
        addAction(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
    }

    fun start() {
        if (!initialized) {
            initialized = true
            context.registerReceiver(stateReceiver, intentFilter)
        }
    }

    fun addCallback(callback: StateCallback) {
        stateCallbacks.add(callback)
    }

    fun removeCallback(callback: StateCallback) {
        stateCallbacks.remove(callback)
    }

    fun stop() {
        if (initialized) {
            context.unregisterReceiver(stateReceiver)
            initialized = false
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED -> {
                    stateCallbacks.forEach {
                        it.onWifiAwareAvailable(awareManager.isAvailable)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "WifiAwareStateMonitor"

        @Volatile
        private var INSTANCE: WifiAwareStateMonitor? = null

        fun get(context: Context): WifiAwareStateMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WifiAwareStateMonitor(context).also { INSTANCE = it }
            }
        }
    }
}