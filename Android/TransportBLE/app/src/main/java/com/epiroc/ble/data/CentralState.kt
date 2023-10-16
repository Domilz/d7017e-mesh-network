package com.epiroc.ble.data

sealed interface CentralState{
    object Connected: CentralState
    object Disconnected: CentralState
    object Uninitialized: CentralState
    object CurrentlyInitializing: CentralState
}