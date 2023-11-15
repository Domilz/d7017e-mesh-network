package com.epiroc.ble.data

sealed interface PeripheralState {
    object Started: PeripheralState
    object Stopped: PeripheralState
    object NotSetup: PeripheralState
    object Starting: PeripheralState
}