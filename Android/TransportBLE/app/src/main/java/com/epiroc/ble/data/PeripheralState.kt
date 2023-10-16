package com.epiroc.ble.data

sealed interface PeripheralState {
    object Started: CentralState
    object Stopped: CentralState
    object NotSetup: PeripheralState
    object Starting: PeripheralState
}