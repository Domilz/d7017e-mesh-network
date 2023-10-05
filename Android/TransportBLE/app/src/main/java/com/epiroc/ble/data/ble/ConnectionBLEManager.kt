package com.epiroc.ble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.epiroc.ble.data.ConnectManager
import com.epiroc.ble.data.ConnectionResult
import com.epiroc.ble.data.ConnectionState
import com.epiroc.ble.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class ConnectionBLEManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : ConnectManager {

    override val data: MutableSharedFlow<Resource<ConnectionResult>> = MutableSharedFlow()

    private val DEVICE_ADDRESS = "6D:65:37:B3:7E:21"
    // private val DEVICE_NAME = "Maltes S20 FE"
    private val DEVICE_NAME = "OnePlus 6T"

    private val INDOOR_SERVICE_UUID = "00001821-0000-1000-8000-00805f9b34fb"
    private val INDOOR_CHARACTERISTIC_EAST_UUID = "00002ab1-0000-1000-8000-00805f9b34fb"
    private val INDOOR_CHARACTERISTIC_NORHT_UUID = "00002ab0-0000-1000-8000-00805f9b34fb"


    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.address == DEVICE_ADDRESS ) {
                Log.d("RESULT_SCAN", "Result is: ${result}")

                //var connResult = ConnectionResult("Tag ID: ${result.device}", connectionState = ConnectionState.Connected)
                //coroutineScope.launch {
                //    data.emit(Resource.Success(data = connResult))

                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device..."))
                    //data.emit(Resource.Loading(message = result.toString()))

                }
                if (isScanning) {
                    result.device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }

        }
    }


    private var currentConnectionAttempt = 1
    private var MAXIMUM_CONNECTION_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services..."))
                    }
                    Thread.sleep(1_000)
                    gatt.discoverServices()
                    this@ConnectionBLEManager.gatt = gatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = ConnectionResult("", ConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            } else {
                gatt.close()
                currentConnectionAttempt += 1
                coroutineScope.launch {
                    data.emit(Resource.Loading(
                            message = "Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"
                        )
                    )
                }
                if (currentConnectionAttempt <= MAXIMUM_CONNECTION_ATTEMPTS) {
                    startReceiving()
                } else {
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to ble device"))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                printGattTable()
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Adjusting MTU space..."))
                }
                gatt.requestMtu(517)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val characteristic = findCharacteristic(INDOOR_SERVICE_UUID, INDOOR_CHARACTERISTIC_EAST_UUID)
            if(characteristic == null) {
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find indoor publisher"))
                }
                return
            }
            coroutineScope.launch {
                data.emit(Resource.Loading(message = "MTU changed.."))
            }

            enableNotification(characteristic)
            gatt.readCharacteristic(characteristic)
         }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(INDOOR_CHARACTERISTIC_EAST_UUID) -> {
                        val coord = value.last().toString()
                        val result = ConnectionResult(coord, ConnectionState.Connected)

                        coroutineScope.launch{
                            data.emit(
                                Resource.Success(data = result)
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(INDOOR_CHARACTERISTIC_EAST_UUID) -> {
                        val coord = value.last().toString()
                        val result = ConnectionResult(coord, ConnectionState.Connected)

                        coroutineScope.launch{
                            data.emit(
                                Resource.Success(data = result)
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(INDOOR_CHARACTERISTIC_EAST_UUID) -> {
                        val coord = value.last().toString()
                        val result = ConnectionResult(coord, ConnectionState.Connected)

                        coroutineScope.launch{
                            data.emit(
                                Resource.Success(data = result)
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(INDOOR_CHARACTERISTIC_EAST_UUID) -> {

                        val coord = value.last()
                        val result = ConnectionResult(coord.toString(), ConnectionState.Connected)

                        coroutineScope.launch {
                            data.emit(
                                Resource.Success(data = result)
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun enableNotification(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, true) == false){
                Log.d("BLEReceiveManager","set characteristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        gatt?.let { gatt ->
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            } else {
                gatt.writeDescriptor(descriptor, payload)
            }
        } ?: error("Not connected to a BLE device!")
    }

    private fun findCharacteristic(serviceUUID: String, characteristicsUUID: String) : BluetoothGattCharacteristic? {
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }

    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning BLE devices..."))
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }


    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        gatt?.close()
    }
}