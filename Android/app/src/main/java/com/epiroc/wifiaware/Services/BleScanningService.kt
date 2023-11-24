package com.epiroc.wifiaware.Services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.lib.Client
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
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

    private val SERVICE_UUID = "527af0f6-83af-11ee-b962-0242ac120002"

    private val binder = LocalBinder()

    private val scanningInterval = 10000L // 10 seconds
    private val idlePeriod = 5000L // 5 seconds
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var wakeLock: PowerManager.WakeLock


    private val scanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
        .build()

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setLegacy(false)
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device

            if (device.address == "F2:43:4B:13:A3:15") {

                val deviceName = result.device.address
                val rssi = result.rssi
                Log.d("BLEService", "Device is: ${deviceName} and rssi is ${rssi}")
                if (rssi != 127 && deviceName != null) {
                    client.updateReadingOfSelf(deviceName, rssi)
                }

                // 2023-11-22 13:46:59.069 12715-12715 BLEService              com.epiroc.wifiaware                 D  ScanResult{device=F2:43:4B:13:A3:15, scanRecord=ScanRecord [mAdvertiseFlags=6, mServiceUuids=[0000feaa-0000-1000-8000-00805f9b34fb], mServiceSolicitationUuids=[], mManufacturerSpecificData={}, mServiceData={0000feaa-0000-1000-8000-00805f9b34fb=[0, -9, 73, 78, 122, -114, -66, 21, 60, -72, 19, 121, 0, 0, 0, 3, 16, -40, 0, 0]}, mTxPowerLevel=-2147483648, mDeviceName=null, mTransportDiscoveryData=null], rssi=127, timestampNanos=371324353004, eventType=16, primaryPhy=1, secondaryPhy=0, advertisingSid=255, txPower=127, periodicAdvertisingInterval=0}
                // 2023-11-22 13:47:08.104 12715-12715 BLEService              com.epiroc.wifiaware                 D  ScanResult{device=F2:43:4B:13:A3:15, scanRecord=ScanRecord [mAdvertiseFlags=6, mServiceUuids=[0000feaa-0000-1000-8000-00805f9b34fb], mServiceSolicitationUuids=[], mManufacturerSpecificData={}, mServiceData={0000feaa-0000-1000-8000-00805f9b34fb=[0, -9, 73, 78, 122, -114, -66, 21, 60, -72, 19, 121, 0, 0, 0, 3, 16, -40, 0, 0]}, mTxPowerLevel=-2147483648, mDeviceName=null, mTransportDiscoveryData=null], rssi=-47, timestampNanos=380329916281, eventType=16, primaryPhy=1, secondaryPhy=0, advertisingSid=255, txPower=127, periodicAdvertisingInterval=0}
                // result.device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            }
        }

        // Maybe use instead?
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (results.size > 0) {
                Log.d("BLEService", "New Batch:")
                for (result in results) {
                    handler.postDelayed({
                        val deviceName = result.device.name
                        val rssi = result.rssi
                        Log.d("BLEService","From batch: $deviceName: with rssi: $rssi")
                        if (rssi != 127 && deviceName != null) {
                            client.updateReadingOfSelf(deviceName, rssi)
                        }
                    }, 20)
                }
            }
            // Handle the results here.
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLEService", "Scan failed with error code: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        val success = gatt.readRemoteRssi()
                        if (success == true) {
                            Log.d("BleService", "Requested RSSI reading")
                        } else {
                            Log.e("BleService", "Failed to request RSSI reading")
                        }
                    }
                }
            } else {
                gatt.close()
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // RSSI value is available in the 'rssi' variable
                Log.d("BLEService", "RSSI: $rssi")
            } else {
                Log.e("BLEService", "Failed to read RSSI")

            }
        }
    }

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
        // Start scanning
        //bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        Log.d("BLEService", "BLE scanner has started.")

        //handler.postDelayed({ stopScanning() }, scanningInterval)
    }

    private fun stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback)

        Log.d("BLEService", "BLE scanner has stopped.")

        //handler.postDelayed({ startScanning() }, idlePeriod)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startScanning()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("BLEService", "onDestroy invoked")
        bluetoothLeScanner.stopScan(scanCallback)
        releaseWakeLock()
        //handler.removeCallbacksAndMessages(null)
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
