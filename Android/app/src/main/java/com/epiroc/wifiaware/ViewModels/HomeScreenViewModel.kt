package com.epiroc.wifiaware.ViewModels

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class HomeScreenViewModel(
    private val context: Context,
    private val packageManager: PackageManager
): ViewModel() {

    val subscribeMessageLiveData = MutableLiveData<String>("No status available")
    val publishMessageLiveData = MutableLiveData<String>("No status available")

    var wifiAwareSession: WifiAwareSession? = null
    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    var currentSession: SubscribeDiscoverySession? = null
    val connectedDevices = mutableListOf<PeerHandle>()
    var wifiAwareManager: WifiAwareManager? = null

    private val _publishMessage = MutableLiveData<String>()
   // val publishMessage: LiveData<String> = _publishMessage
    fun checkWifiAwareAvailability(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
    }
    fun subscribeToWifiAwareSessions() {
        if (wifiAwareSession == null) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("subscribe_message", "SUBSCRIBE: Wifi Aware session is not available")
            }
            return
        }

        val serviceName = "epiroc_mesh" // Match the service name used for publishing.

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        val handler = Handler(Looper.getMainLooper()) // Use the main looper.

        val discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {

                currentSession = session

            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                if (peerHandle != null) {
                    subscribeMessageLiveData.value = "SUBSCRIBE: Connected to  $peerHandle: ${serviceSpecificInfo.toString()} ${matchFilter.toString()}"
                    currentSession!!.sendMessage(peerHandle,2,serviceSpecificInfo)
                }
            }



            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle incoming data here.

                subscribeMessageLiveData.value = "SUBSCRIBE: MessageReceived from $peerHandle: ${message.toString()}"


                // Optionally, you can establish a connection with the sender (Device A).
                // For simplicity, you can store the PeerHandle in a list of connected devices.
                // You should have a mechanism to manage and maintain these connections.
                connectedDevices.add(peerHandle)

                // Respond to the sender (Device A) if needed.
                val responseMessage = "Hello from Device B!"
                currentSession?.sendMessage(
                    peerHandle,
                    0, // Message type (0 for unsolicited)
                    responseMessage.toByteArray(Charsets.UTF_8)
                )
            }
        }

        // Subscribe to WiFi Aware sessions.
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permissions here if needed.
        }
        wifiAwareSession!!.subscribe(subscribeConfig, discoverySessionCallback, handler)
    }

    fun publishUsingWifiAware() {
        if (wifiAwareSession != null) {
            val serviceName = "epiroc_mesh"

            val config = PublishConfig.Builder()
                .setServiceName(serviceName)
                .build()

            val handler = Handler(Looper.getMainLooper())

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions are not granted, request them first.
                ActivityCompat.requestPermissions(
                    context as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )
            } else {
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        //printContent("PUBLISH:  PublishStarted")
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        publishMessageLiveData.value = "PUBLISH:  MessageReceived from $peerHandle message: ${message.toString()}"

                    }
                }, handler)
            }
        } else {
            // Wi-Fi Aware session is not available.
            publishMessageLiveData.value = ("PUBLISH: Wifi Aware session is not available")
        }
    }

    private fun wifiAwareState(): String {
        var wifiAwareAvailable = ""
        wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // discard current sessions
                if (wifiAwareManager?.isAvailable == true) {
                    wifiAwareAvailable = "has Wifi Aware on"
                } else {
                    wifiAwareAvailable = "has Wifi Aware off"
                }
            }
        }
        context.registerReceiver(myReceiver, filter)
        return wifiAwareAvailable
    }

    private fun acquireWifiAwareSession(): String {
        val attachCallback = object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                wifiAwareSession = session
                // Perform any necessary operations here.
            }

            override fun onAttachFailed() {
                wifiAwareSession = null
            }
        }

        val identityChangedListener = object : IdentityChangedListener() {
            override fun onIdentityChanged(peerId: ByteArray) {
                // Handle identity change if needed.
            }
        }

        wifiAwareManager?.let {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )

                return "Requesting permissions..."
            } else {
                it.attach(attachCallback, identityChangedListener, Handler(Looper.getMainLooper()))
                return "Wifi Aware session attached."
            }
        } ?: run {
            // Handle the case where wifiAwareManager is null.
            return "Wifi Aware manager is null."
        }
    }

    fun availability(): String {
        val hasSystemFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
        val acquisitionMessage: String
        acquisitionMessage = if (hasSystemFeature) {
            wifiAwareState()
            acquireWifiAwareSession()
        } else {
            "Wifi Aware is not supported on this device."
        }
        return acquisitionMessage
    }


}