package com.epiroc.wifiaware.Services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.Screens.permissions.PermissionUtils
import com.epiroc.wifiaware.lib.Client
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class BlePeripheralService () : Service() {
    private val binder = LocalBinder()

    private val SERVICE_UUID = UUID.fromString("527af0f6-83af-11ee-b962-0242ac120002")

    @Inject
    lateinit var client: Client
    @Inject
    lateinit var bluetoothManager : BluetoothManager

    val advertiser by lazy {
        bluetoothManager.adapter.bluetoothLeAdvertiser
    }

    private var gattServer: BluetoothGattServer? = null


    private val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(true)
        .setTimeout(0)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
        .build()

    private val advertiseData = AdvertiseData.Builder()
        .setIncludeDeviceName(true)
        .addServiceUuid(ParcelUuid(SERVICE_UUID))
        .build()

    private val service = BluetoothGattService(
        SERVICE_UUID,
        BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("PeripheralService", "Started successfully")
            // Advertising started successfully
        }


        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d("PeripheralService", "Starting failed $errorCode")
            // Advertising failed
        }

    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
    }


    override fun onCreate() {
        Log.d("PeripheralService", "Created")
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


        // Check if BLE is supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e("PeripheralService", "BLE not supported")
            stopSelf()
        }

        Log.d("PeripheralService", "Peripheral Service started!")
    }

    private fun startAdvertising() {
        Log.d("PeripheralService", "Start advertising")

        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)

        gattServer = bluetoothManager.openGattServer(this, gattServerCallback)
        gattServer?.addService(service)
    }

    private fun stopAdvertising() {
        Log.d("PeripheralService", "Stop advertising")

        advertiser?.stopAdvertising(advertiseCallback)
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
            .setContentTitle("BLE Peripheral Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // use your app's icon
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channelName = "Peripheral Channel"
        val description = "Channel for BLE Peripheral foreground service"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(WifiAwareService.CHANNEL_ID, channelName, importance).apply {
            this.description = description
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("PeripheralService", "onStartCommand")

        val data = intent.getStringExtra("rpID")

        if (data != null) {
            Log.d("PeripheralService", "Beacon name: $data")
            BluetoothAdapter.getDefaultAdapter().setName(data)
        } else {
            Log.d("PeripheralService", "Beacon name: BEACON TEST 1")
            BluetoothAdapter.getDefaultAdapter().setName("RPDefault")
        }
        startAdvertising()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("PeripheralService", "onDestroy invoked")
        stopAdvertising()
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): BlePeripheralService = this@BlePeripheralService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}