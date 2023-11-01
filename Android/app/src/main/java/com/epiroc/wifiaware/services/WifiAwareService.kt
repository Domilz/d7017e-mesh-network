package com.epiroc.wifiaware.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.net.Publisher
import com.epiroc.wifiaware.net.Subscriber
import java.util.Timer
import java.util.TimerTask

class WifiAwareService : Service() {

    private lateinit var publisher: Publisher
    private lateinit var subscriber: Subscriber
    private val messagesReceived = mutableListOf<String>()
    var connectivityManager: ConnectivityManager? = null
    var wifiAwareSession: WifiAwareSession? = null
    var wifiAwareManager: WifiAwareManager? = null
    val hasWifiAwareText: MutableState<String> = mutableStateOf("")

    override fun onCreate() {
        super.onCreate()

        // Initialize WifiAwareManager and WifiAwareSession
        wifiAwareManager = getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        acquireWifiAwareSession()

        wifiAwareManager!!.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                super.onAttached(session)
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                // Initialize the publisher and subscriber
                publisher = Publisher(
                    context = applicationContext,
                    serviceName = "NANService",
                    callback = object : Publisher.Callback {
                        override fun onServicePublished() {
                            Log.d("1Wifi", "Service published")
                        }

                        override fun onMessageSendResult(id: Int, success: Boolean) {
                            Log.d("1Wifi", "Message send result: $success")
                        }

                        override fun onMessageReceived(message: ByteArray) {
                            Log.d("1Wifi", "Message received: ${String(message)}")
                        }
                    },
                    nanSession = wifiAwareSession!!,
                    cManager = connectivityManager!!
                )

                subscriber = Subscriber(
                    context = applicationContext,
                    serviceName = "NANService",
                    object : Subscriber.Callback {
                        override fun onServiceSubscribed() {
                            // Handle service subscribed event
                        }

                        override fun onServiceFound(serviceInfo: ByteArray, matchFilter: List<ByteArray>) {
                            // Handle service found event
                        }

                        override fun onMessageSendResult(id: Int, success: Boolean) {
                            // Handle message send result event
                        }

                        override fun onMessageReceived(message: ByteArray) {
                            // Handle message received event
                        }
                    },
                    session,
                    connectivityManager
                )

                subscriber.subscribeToWifiAwareSessions()
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                // Handle attach failed event
            }
        }, null)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification channel
        createNotificationChannel()

        // Create the notification
        val notification = createNotification()

        // Start the service in the foreground with the notification
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "wifiAwareServiceChannel"
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "WifiAware Service Channel"
            val description = "Channel for WifiAware foreground service"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                this.description = description
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Create an intent that will open your app when the notification is clicked
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Create the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wifi Aware Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // use your app's icon
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the publisher and subscriber
        publisher.stop()
        subscriber.stop()
    }

    private fun wifiAwareState(): String {
        Log.d("1Wifi", "Checking Wifi Aware state.")
        var wifiAwareAvailable = ""
        wifiAwareManager = this.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // discard current sessions
                if (wifiAwareManager?.isAvailable == true) {
                    hasWifiAwareText.value = "has Wifi Aware on"
                } else {
                    hasWifiAwareText.value = "has Wifi Aware off"
                }
            }
        }
        this.registerReceiver(myReceiver, filter)
        return wifiAwareAvailable
    }

    private fun acquireWifiAwareSession(): String {
        Log.d("1Wifi", "Attempting to acquire Wifi Aware session.")
        val attachCallback = object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                wifiAwareSession = session
                Timer().schedule(object : TimerTask() {
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    override fun run() {
                        publisher.publishUsingWifiAware()
                        subscriber.subscribeToWifiAwareSessions()
                    }
                }, 1000) // Delay in milliseconds

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
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Permissions not granted."
            } else {
                it.attach(attachCallback, identityChangedListener, Handler(Looper.getMainLooper()))
                return "Wifi Aware session attached."
            }
        } ?: run {
            return "Wifi Aware manager is null."
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): WifiAwareService = this@WifiAwareService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}
