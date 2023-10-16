package com.epiroc.ble.data

import com.epiroc.ble.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface PeripheralManager {

    val data: MutableSharedFlow<Resource<ConnectionResult>>

    fun startAdvertising()

    fun stopAdvertising()

    fun closePeripheral()

}