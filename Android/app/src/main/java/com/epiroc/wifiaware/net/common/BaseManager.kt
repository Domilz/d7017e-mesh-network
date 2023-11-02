package com.epiroc.wifiaware.net.common

import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import androidx.annotation.CallSuper
import com.epiroc.wifiaware.net.WifiAwareStateMonitor
import kotlin.properties.Delegates

abstract class BaseManager(protected val context: Context) {

    private val stateMonitor = WifiAwareStateMonitor.get(context)
    protected var manager: WifiAwareManager by Delegates.notNull()
    protected var activeSession: WifiAwareSession? = null

    init {
        check(NANUtils.checkWifiSupport(context) && NANUtils.checkWifiAwareSupport(context))
        manager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
    }

    private val stateCallback = object : WifiAwareStateMonitor.StateCallback {
        override fun onWifiAwareAvailable(available: Boolean) {
            if (available) onWifiAwareAvailable()
            else onWifiAwareUnavailable()
        }
    }

    private val attachCallback = object : AttachCallback() {

        override fun onAttached(session: WifiAwareSession?) {
            super.onAttached(session)
            activeSession = session
        }

        override fun onAttachFailed() {
            super.onAttachFailed()
            onSessionAttachFailed()
        }
    }

    @CallSuper
    open fun start() {
        stateMonitor.addCallback(stateCallback)
        stateMonitor.start()
        manager.attach(attachCallback, null)
    }

    @CallSuper
    open fun stop() {
        activeSession?.close()
        stateMonitor.stop()
        stateMonitor.removeCallback(stateCallback)
    }

    fun isWifiAwareAvailable(): Boolean {
        return manager.isAvailable
    }

    open fun onWifiAwareUnavailable() {}
    open fun onWifiAwareAvailable() {}
    open fun onSessionAttachFailed() {}
    open fun onSessionAttached() {}
}