package com.epiroc.ble.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epiroc.ble.data.ConnectionState
import com.epiroc.ble.data.ble.ConnectionBLEManager
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.epiroc.ble.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

class BleListViewModel @Inject constructor(
    private val connectionBLEManager: ConnectionBLEManager
) : ViewModel() {
    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var connectedDevice by mutableStateOf("000")
        private set


    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    private fun subscribeToChanges(){
        viewModelScope.launch {
            connectionBLEManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        connectedDevice = result.data.title
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect(){
        connectionBLEManager.disconnect()
    }

    fun reconnect(){
        connectionBLEManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        connectionBLEManager.startReceiving()
    }

    override fun onCleared() {
        super.onCleared()
        connectionBLEManager.closeConnection()
    }
}