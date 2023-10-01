package com.epiroc.ble.data

import com.epiroc.ble.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface ConnectManager {

    val data: MutableSharedFlow<Resource<ConnectionResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}