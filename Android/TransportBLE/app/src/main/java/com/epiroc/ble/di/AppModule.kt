package com.epiroc.ble.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import com.epiroc.ble.data.ble.CentralBLEManager
import com.epiroc.ble.data.ble.PeripheralBLEManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideBluetoothManager(@ApplicationContext context: Context) : BluetoothManager {
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context) : BluetoothAdapter {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    fun provideConnectionBLEManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ): CentralBLEManager {
        return CentralBLEManager(bluetoothAdapter, context)
    }

    @Provides
    @Singleton
    fun providePeripheralBLEManager(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager
    ) : PeripheralBLEManager {
        return PeripheralBLEManager(bluetoothManager, context)
    }
}