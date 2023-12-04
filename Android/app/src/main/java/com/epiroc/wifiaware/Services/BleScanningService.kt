package com.epiroc.wifiaware.Services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.lib.Client
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class BleScanningService : Service() {
    // Dagger hilt objects
    @Inject
    lateinit var client: Client
    @Inject
    lateinit var bluetoothAdapter : BluetoothAdapter
    private val bluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val binder = LocalBinder()

    private lateinit var wakeLock: PowerManager.WakeLock

    private val scanFilter = ScanFilter.Builder()
        .build()

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setLegacy(false)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceAddress : String
            val rssi = result.rssi
            if (rssi != 127) {
                if (result.device.name == "rpId1" || result.device.name == "rpId2") {
                    deviceAddress = result.device.name
                } else {
                    deviceAddress = result.device.address
                }
                Log.d("BLEService", "Device is: $deviceAddress and rssi is $rssi")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLEService", "Scan failed with error code: $errorCode")
        }
    }

    //todo: test if no bluetooth still causes crashes
    override fun onCreate() {
        super.onCreate()

        acquireWakeLock()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this,
            WifiAwareService.CHANNEL_ID
        )
            .setContentTitle("BLE Service Running")
            .setContentText("BLE Service is running in the background...")
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        createNotificationChannel()
        createNotification()
        startForeground(1, notification)

        // Check if BLE is supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e("BLEService", "BLE not supported")
            stopSelf()
        }

        Log.d("BLEService", "BLE Service started!")
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
        return NotificationCompat.Builder(this, WifiAwareService.CHANNEL_ID)
            .setContentTitle("BLE Aware Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // use your app's icon
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channelName = "BLE Service Channel"
        val description = "Channel for BLE foreground service"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(WifiAwareService.CHANNEL_ID, channelName, importance).apply {
            this.description = description
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun startScanning() {
        bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        //bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        Log.d("BLEService", "BLE scanner has started.")
    }

    private fun stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback)

        Log.d("BLEService", "BLE scanner has stopped.")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startScanning()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("BLEService", "onDestroy invoked")
        bluetoothLeScanner.stopScan(scanCallback)
        releaseWakeLock()
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleScanningService = this@BleScanningService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BleScanningService::WakeLock"
        )

        if (!wakeLock.isHeld) {
            Log.d("BleService", "Acquiring Wake Lock")
            wakeLock.acquire()
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
