package com.epiroc.wifiaware.Services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R

class BLEService : Service() {
    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            // TODO: Process the scan result, extract data
            //Log.d("BLEService", "Found BLE device: ${result.device.address} with data: ${result.scanRecord}")
            if (result.device.address == "F2:43:4B:13:A3:15") {
                var rssi = result.rssi
                Log.d("BLEService", rssi.toString())
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            // TODO: Process batch scan results if necessary
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLEService", "Scan failed with error code: $errorCode")
        }
    }

    override fun onCreate() {
        super.onCreate()

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



        // Get BluetoothManager and BluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        // Check if BLE is supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e("BLEService", "BLE not supported")
            stopSelf()
        }

        Log.d("BLEService", "BLE Service started!")
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions
            Log.e("BLEService", "Location permission not granted")
            stopSelf()
        } else {
            // Start scanning
            bluetoothLeScanner?.startScan(scanCallback)
            Log.d("BLEService", "BLE scanner has started")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions?
            return
        }
        bluetoothLeScanner?.stopScan(scanCallback)
        super.onDestroy()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    inner class LocalBinder : Binder() {
        fun getService(): BLEService = this@BLEService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}
