package com.epiroc.ble.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.epiroc.ble.data.ble.ConnectionBLEManager
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
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context):BluetoothAdapter{
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    fun provideConnectionBLEManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ): ConnectionBLEManager {
        return ConnectionBLEManager(bluetoothAdapter,context)
    }
}