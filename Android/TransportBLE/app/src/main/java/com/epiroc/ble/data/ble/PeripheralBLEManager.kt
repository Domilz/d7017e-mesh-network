package com.epiroc.ble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import com.epiroc.ble.data.ConnectManager
import com.epiroc.ble.data.ConnectionResult
import com.epiroc.ble.data.ConnectionState
import com.epiroc.ble.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class PeripheralBLEManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : ConnectManager {
    override val data: MutableSharedFlow<Resource<ConnectionResult>> = MutableSharedFlow()

    val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

    val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(false)
        .setTimeout(0)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
        .build()

    val advertiseData = AdvertiseData.Builder()
        .setIncludeDeviceName(true) // Include device name in advertisement
        .build()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            // Advertising started successfully
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Advertising failed
        }

    }

    override fun startReceiving() {
        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        val result = ConnectionResult("Server on", ConnectionState.Connected)
        coroutineScope.launch {
            data.emit(Resource.Success(result))
        }
    }

    override fun reconnect() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }


    override fun closeConnection() {
        advertiser.stopAdvertising(advertiseCallback)
    }
}