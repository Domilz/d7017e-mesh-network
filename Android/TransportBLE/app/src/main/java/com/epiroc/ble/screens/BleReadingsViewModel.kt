package com.epiroc.ble.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epiroc.ble.data.CentralState
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.epiroc.ble.data.PeripheralState
import com.epiroc.ble.data.ble.CentralBLEManager
import com.epiroc.ble.data.ble.PeripheralBLEManager
import com.epiroc.ble.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel
class BleReadingsViewModel @Inject constructor(
    private val centralBLEManager: CentralBLEManager,
    private val peripheralBLEManager: PeripheralBLEManager
) : ViewModel() {
    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var resultMessage by mutableStateOf("000")
        private set


    var centralState by mutableStateOf<CentralState>(CentralState.Uninitialized)

    var peripheralState by mutableStateOf<PeripheralState>(PeripheralState.NotSetup)

    private fun subscribeToChanges(){
        viewModelScope.launch {
            peripheralBLEManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        centralState = result.data.centralState
                        peripheralState = result.data.peripheralState
                        resultMessage = result.data.title
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        centralState = CentralState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        centralState = CentralState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect(){
        centralBLEManager.disconnect()
    }

    fun reconnect(){
        centralBLEManager.reconnect()

    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        //centralBLEManager.startReceiving()
        peripheralBLEManager.startAdvertising()
    }

    override fun onCleared() {
        super.onCleared()
        //centralBLEManager.closeConnection()
        peripheralBLEManager.stopAdvertising()
    }
}