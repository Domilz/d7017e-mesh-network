package com.epiroc.ble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.epiroc.ble.data.CentralState
import com.epiroc.ble.data.ConnectionResult
import com.epiroc.ble.data.PeripheralManager
import com.epiroc.ble.data.PeripheralState
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

    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    private var bluetoothDevices: HashSet<BluetoothDevice>? = null


    val SERVICE_UUID = UUID.fromString("527af0f6-83af-11ee-b962-0242ac120002")
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
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ,
    )

    var counter = 1

//    val characteristicData = "Hello, World".toByteArray(Charset.defaultCharset())


    private var gattServer: BluetoothGattServer? = null


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

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    bluetoothDevices?.add(device);
                    Log.d("Peripheral", "Connected to device: " + device.address)
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    bluetoothDevices?.remove(device);
                    Log.d("Peripheral", "Disconnected from device: " + device.address)
                }
            } else {
                bluetoothDevices?.remove(device);
                Log.e("Peripheral", "Some error?")
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {

            val bluetoothClient = device
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            gattServer?.notifyCharacteristicChanged(bluetoothClient, characteristic, false);
            Log.d("Peripheral", "On Descriptor Write Request")
            notifyCharacteristicChanged()
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            Log.d("Peripheral", "Notification sent. Status: $status")
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
            Log.d("Peripheral", "Entered Read Request")
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                val d = characteristic.value
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, d)
                Log.d("Peripheral", "Notifying")
                updateCharacteristic();
                notifyCharacteristicChanged()

            } else {
                // Handle other characteristics or unknown UUID
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
            }
        }
    }

    fun updateCharacteristic() {
        counter++;
        characteristic.setValue(counter.toString())
    }

    fun notifyCharacteristicChanged() {
        Log.d("Peripheral", "Notify characteristic changed")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (device in bluetoothDevices!!) {
                if (gattServer != null) {
                    val triggered = gattServer!!.notifyCharacteristicChanged(device, characteristic, false, characteristic.value);
                    Log.d("Peripheral", "Triggered: " + triggered)
                }
            }
        } else {
            for (device in bluetoothDevices!!) {
                val success = gattServer!!.notifyCharacteristicChanged(device, characteristic, false)
                if (success) {
                    Log.d("Peripheral", "Notification success")
                } else {
                    Log.d("Peripheral", "Notification failed")
                }
            }
        }
    }
    override fun startAdvertising() {
        bluetoothDevices = HashSet()

        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        val cccd = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )

        characteristic.addDescriptor(cccd)
        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)

        characteristic.setValue(counter.toString())
        service.addCharacteristic(characteristic)
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        gattServer?.addService(service)

        Log.d("PeripheralManagar", "Started advertising")
        coroutineScope.launch {
            data.emit(Resource.Success(data = ConnectionResult("Started advertising", CentralState.Connected, PeripheralState.Started)))
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