package com.epiroc.ble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import com.epiroc.ble.data.ConnectionResult
import com.epiroc.ble.data.CentralState
import com.epiroc.ble.data.PeripheralManager
import com.epiroc.ble.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class PeripheralBLEManager @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val context: Context
) : PeripheralManager {
    override val data: MutableSharedFlow<Resource<ConnectionResult>> = MutableSharedFlow()

    val SERVICE_UUID = UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb")
    val CHARACTERISTIC_UUID = UUID.fromString("dc78e1f1-45d7-4b14-b66d-71d6e3b6aaf5")

    val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

    val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(true)
        .setTimeout(0)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
        .build()

    val advertiseData = AdvertiseData.Builder()
        .setIncludeDeviceName(true)
        .addServiceUuid(ParcelUuid(SERVICE_UUID))
        .build()

    val service = BluetoothGattService(
        SERVICE_UUID,
        BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    val characteristic = BluetoothGattCharacteristic(
        CHARACTERISTIC_UUID,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val characteristicData = "Hello, World".toByteArray(Charset.defaultCharset())

    private var gattServer: BluetoothGattServer? = null

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

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                val data = characteristic.value
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, data)
            } else {
                // Handle other characteristics or unknown UUID
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
            }
        }
    }

    override fun startAdvertising() {
        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        characteristic.setValue(characteristicData)
        service.addCharacteristic(characteristic)
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        gattServer?.addService(service)
        val result = ConnectionResult("Server on", CentralState.Connected)
        coroutineScope.launch {
            data.emit(Resource.Success(result))
        }
    }

    override fun stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback)
    }

    override fun closePeripheral() {
        advertiser.stopAdvertising(advertiseCallback)
        gattServer?.close()
    }
}