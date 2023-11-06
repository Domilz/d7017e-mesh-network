package com.epiroc.wifiaware.Services

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
import com.epiroc.wifiaware.transport.Publisher
import com.epiroc.wifiaware.transport.Subscriber
import com.epiroc.wifiaware.transport.network.PublisherNetwork
import com.epiroc.wifiaware.transport.network.SubscriberNetwork
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class WifiAwareService : Service() {
    private val hasWifiAwareText: MutableState<String> = mutableStateOf("")
    private val utility: WifiAwareUtility = WifiAwareUtility
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private val binder = LocalBinder()

    private var connectivityManager: ConnectivityManager? = null
    private var wifiAwareSession: WifiAwareSession? = null
    private var wifiAwareManager: WifiAwareManager? = null

    private lateinit var serviceUUID: String
    private lateinit var publisher: Publisher
    private lateinit var subscriber: Subscriber

    override fun onCreate() {
        super.onCreate()
        // Initialize WifiAwareManager and WifiAwareSession
        wifiAwareManager = getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager

         serviceUUID = UUID.randomUUID().toString()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("1Wifi","WifiAwareService STARTED")

        // Show notification for the foreground service
        val intent = Intent(this, MainActivity::class.java).apply {
            flags
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WifiAware Service Running")
            .setContentText("Service is running in the background...")
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        createNotificationChannel()
        createNotification()
        startForeground(1, notification)

        wifiAwareState()
        acquireWifiAwareSession()

        val cleanUpHandler = Handler(Looper.getMainLooper())
        val cleanUpRunnable = object: Runnable {
            override fun run() {
                if (::subscriber.isInitialized) {
                    if(utility.isNotEmpty()) {
                        var didremove = utility.removeIf()
                        Log.e(
                            "1Wifi",
                            "It removed? : $didremove"
                        )
                        if (didremove) {
                            Log.e(
                                "1Wifi",
                                "It removed? : $didremove YES AND IT CLOSES THE SESSION!"
                            )
                            wifiAwareSession?.close()
                        }
                    } else {
                        Log.e("1Wifi", "recentlyConnectedDevices: ${utility.count()}")
                    }
                } else {
                    Log.e("1Wifi", "subscriber: not init")
                }
                utility.incrementTryCount()
                if(utility.getTryCount() <= 10)
                    cleanUpHandler.postDelayed(this, 1000)
                else
                    cleanUpHandler.postDelayed(this, 10000)
            }
        }
        cleanUpHandler.post(cleanUpRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("1Wifi","Service destroyed")
        super.onDestroy()
        publisher.closeServerSocket()
        serviceScope.cancel()
    }

    companion object {
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
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create the notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wifi Aware Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // use your app's icon
            .setContentIntent(pendingIntent)
            .build()
    }

    // UI function for if WifiAware is available or not.
    private fun wifiAwareState(): String {
        Log.d("1Wifi", "Checking Wifi Aware state.")
        var wifiAwareAvailable = ""
        wifiAwareManager = this.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
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
                        val serviceName = "epiroc_mesh"
                        // Initialize the publisher and subscriber
                        publisher = Publisher(
                            ctx = applicationContext,
                            nanSession = wifiAwareSession!!,
                            network = PublisherNetwork(),
                            srvcName = serviceName,
                            uuid = serviceUUID
                        )
                        subscriber = Subscriber(
                            ctx = applicationContext,
                            nanSession = session,
                            network = SubscriberNetwork(),
                            srvcName = serviceName,
                            uuid = serviceUUID
                        )
                        publisher.publishUsingWifiAware()
                        subscriber.subscribeToWifiAwareSessions()
                    }
                }, 1000) // Delay in milliseconds
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                wifiAwareSession = null
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        wifiAwareState()
                        acquireWifiAwareSession()
                    }
                }, 1000) // Delay in milliseconds
            }

            override fun onAwareSessionTerminated() {
                super.onAwareSessionTerminated()
                wifiAwareSession = null
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        wifiAwareState()
                        acquireWifiAwareSession()
                    }
                }, 1000) // Delay in milliseconds
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

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}
