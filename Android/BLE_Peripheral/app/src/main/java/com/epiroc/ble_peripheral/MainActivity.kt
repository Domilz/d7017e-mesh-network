package com.epiroc.ble_peripheral

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.epiroc.ble_peripheral.ui.theme.BLE_PeripheralTheme
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.app.ActivityCompat
import java.util.UUID
import android.bluetooth.le.AdvertiseCallback
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


class MainActivity : ComponentActivity() {



    class MyGattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            // Handle connection state changes here
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            // Handle read requests for characteristics here
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            // Handle write requests for characteristics here
        }
    }

    class MyAdvertiseCallback : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            println("succ")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Advertising failed to start, handle the error
            println("fail")
        }
    }

    private fun setupPeripheral(bluetoothAdapter : BluetoothAdapter, serviceID : UUID){
        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val advertiseCallback = MyAdvertiseCallback()


        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(serviceID))
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Permission failed in setupPeripheral")
            return
        }
        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
    }

    private fun setupGATT(bluetoothManager : BluetoothManager, serviceID: UUID, characteristicID: UUID){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Permission failed in setupGatt")
            return
        }
        val gattServerCallback = MyGattServerCallback()
        val gattServer = bluetoothManager.openGattServer(this, gattServerCallback)
        val service = BluetoothGattService(
            serviceID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val characteristic = BluetoothGattCharacteristic(
            characteristicID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)
        gattServer.addService(service)




    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BLE_PeripheralTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val YOUR_SERVICE_UUID = UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb")
        val YOUR_CHARACTERISTIC_UUID = UUID.fromString("dc78e1f1-45d7-4b14-b66d-71d6e3b6aaf5")
        val REQUEST_BLUETOOTH_PERMISSIONS = 1

        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }

        setupPeripheral(bluetoothAdapter, YOUR_SERVICE_UUID)
        setupGATT(bluetoothManager, YOUR_SERVICE_UUID, YOUR_CHARACTERISTIC_UUID)
        println("helo")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BLE_PeripheralTheme {
        Greeting("Android")
    }
}