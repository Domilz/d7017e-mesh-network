package com.epiroc.wifiaware.Services

import android.Manifest
import android.annotation.SuppressLint
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
import android.net.wifi.WifiManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.lib.Client
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.transport.Publisher
import com.epiroc.wifiaware.transport.Subscriber
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import com.epiroc.wifiaware.workers.NetworkWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@AndroidEntryPoint
class WifiAwareService : Service() {
    private var wifiLock: WifiManager.WifiLock? = null
    private lateinit var cleanUpRunnable: Runnable
    private lateinit var cleanUpHandler: Handler
    private val hasWifiAwareText: MutableState<String> = mutableStateOf("")
    private val utility: WifiAwareUtility = WifiAwareUtility
    private val binder = LocalBinder()

    private var connectivityManager: ConnectivityManager? = null
    private var wifiAwareSession: WifiAwareSession? = null
    private var wifiAwareManager: WifiAwareManager? = null

    private lateinit var publisher: Publisher
    private lateinit var subscriber: Subscriber
    @Inject
    lateinit var client: Client

    private lateinit var wakeLock: WakeLock

    override fun onCreate() {
        super.onCreate()
        // Initialize WifiAwareManager and WifiAwareSession
        wifiAwareManager = getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager

    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("1Wifi","WifiAwareService STARTED")

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp:MyWakeLockTag"
        )
        wakeLock.acquire()

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

        startNetworkWorker()
        wifiAwareState()
        acquireWifiAwareSession()

        cleanUpHandler = Handler(Looper.getMainLooper())
        cleanUpRunnable = object: Runnable {
            override fun run() {

                if (::subscriber.isInitialized) {
                    if(utility.isNotEmpty()) {
                        val didremove = utility.removeIf()
                      //  Log.e("1Wifi", "It removed? : $didremove")
                        if (didremove) {
                           // Log.e("1Wifi", "It removed? : $didremove YES")
                        }
                    } else {
                        utility.incrementTryCount()
                        //Log.e("1Wifi", "recentlyConnectedDevices: ${utility.count()}")
                    }
                } else {
                    Log.e("1Wifi", "subscriber: not init")
                }
                //Log.d("1Wifi","tryCount: ${utility.getTryCount()}")
                if (utility.getTryCount() == 10) {
                   /* publisher.getCurrent()?.close()
                    subscriber.getCurrent()?.close()
                    publisher.publishUsingWifiAware()
                    subscriber.subscribeToWifiAwareSessions()*/
                    utility.setTryCount(0)
                }
                cleanUpHandler.postDelayed(this, 1000)

            }
        }
        cleanUpHandler.post(cleanUpRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        wifiAwareSession!!.close()

        cleanUpHandler.removeCallbacks(cleanUpRunnable)
        Log.d("1Wifi","Service destroyed")
        super.onDestroy()
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    companion object {
        const val CHANNEL_ID = "wifiAwareServiceChannel"
    }

    private fun createNotificationChannel() {

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
        val wifiAwareAvailable = ""
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
            @SuppressLint("WakelockTimeout")
            override fun onAttached(session: WifiAwareSession) {
                val powerManager = getSystemService(POWER_SERVICE) as PowerManager
                wakeLock =
                    powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
                wakeLock.acquire()
                val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
                wifiLock = wifiManager.createWifiLock(
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    "MyApp:WifiLockTag"
                )
                wifiLock!!.acquire()
                wifiAwareSession = session

                val serviceName = Config.getConfigData()?.getString("service_name")
                // Initialize the publisher and subscriber
                publisher = Publisher(
                    ctx = applicationContext,
                    nanSession = wifiAwareSession!!,
                    client = client,
                    serviceName = serviceName
                )
                subscriber = Subscriber(
                    ctx = applicationContext,
                    nanSession = session,
                    client = client,
                    serviceName = serviceName!!,
                )
                CoroutineScope(Dispatchers.IO).launch {
                    publisher.publishUsingWifiAware()

                }

                CoroutineScope(Dispatchers.IO).launch {
                    subscriber.subscribeToWifiAwareSessions()
                }
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
                wifiLock?.release()            }
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

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun startNetworkWorker() {
        // Set up constraints to require network connectivity
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a WorkRequest for your Worker and set the constraints
        val networkWorkRequest = OneTimeWorkRequest.Builder(NetworkWorker::class.java)
            .setConstraints(constraints)
            .build()

        // Enqueue the work
        WorkManager.getInstance(this).enqueue(networkWorkRequest)
    }

}

