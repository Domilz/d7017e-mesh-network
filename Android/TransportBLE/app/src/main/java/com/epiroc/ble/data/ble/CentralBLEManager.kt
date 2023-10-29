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
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.epiroc.ble.data.CentralConnectionManager
import com.epiroc.ble.data.ConnectionResult
import com.epiroc.ble.data.CentralState
import com.epiroc.ble.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class CentralBLEManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : CentralConnectionManager {

    override val data: MutableSharedFlow<Resource<ConnectionResult>> = MutableSharedFlow()

    val SERVICE_UUID = "0000181a-0000-1000-8000-00805f9b34fb"
    val CHARACTERISTIC_UUID = "dc78e1f1-45d7-4b14-b66d-71d6e3b6aaf5"


    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
        .build()

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result?.device
            if (device != null) {
                Log.d("RESULT_SCAN", "Result is: ${result}")

                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device..."))

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
                    this@CentralBLEManager.gatt = gatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = ConnectionResult("", CentralState.Disconnected)))
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
            val characteristic = findCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID)
            if(characteristic == null) {
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find publisher"))
                }
                return
            }
            coroutineScope.launch {
                data.emit(Resource.Loading(message = "MTU changed.."))
            }
            enableNotification(characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d("Central", "onCharacteristicChanged")
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(CHARACTERISTIC_UUID) -> {
                        val charData = String(value, Charset.defaultCharset())
                        val result = ConnectionResult(charData, CentralState.Connected)

                        coroutineScope.launch{
                            data.emit(
                                Resource.Success(data = result)
                            )
                        }
                    }

                    else -> Unit
                }
            }
            gatt.readCharacteristic(characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d("Central", "onCharacteristicChanged")
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(CHARACTERISTIC_UUID) -> {

                        val charData = String(value, Charset.defaultCharset())
                        val result = ConnectionResult(charData, CentralState.Connected)

                        coroutineScope.launch {
                            data.emit(
                                Resource.Success(data = result)
                            )
                        }
                    }

                    else -> Unit
                }
            }
            gatt.readCharacteristic(characteristic)
        }

    }

    private fun enableNotification(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }

        val cccDescriptor = characteristic.getDescriptor(cccdUuid)
        gatt?.setCharacteristicNotification(characteristic, true)
        writeDescription(cccDescriptor, payload)

    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        gatt?.let { gatt ->
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                descriptor.setValue(payload)
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
        bleScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        Log.d("Central", "Disconnected connection")
        gatt?.disconnect()
    }


    override fun closeConnection() {
        Log.d("Central", "Closed connection")
        bleScanner.stopScan(scanCallback)
        gatt?.close()
    }
}