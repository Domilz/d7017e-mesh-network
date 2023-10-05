package com.epiroc.ble
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.epiroc.ble.screens.Navigation
import com.epiroc.ble.ui.theme.TransportBLETheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TransportBLETheme {
                Navigation(
                    onBluetoothStateChanged = {
                        showBluetoothDialog()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothDialog()
    }

    private fun showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startBluetoothIntentForResult.launch(enableBluetoothIntent)
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if (result.resultCode != Activity.RESULT_OK) {
                showBluetoothDialog()
            }
        }
}