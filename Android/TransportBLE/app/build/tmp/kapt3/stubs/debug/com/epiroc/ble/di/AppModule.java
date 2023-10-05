package com.epiroc.ble.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u001a\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u001a\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0004H\u0007\u00a8\u0006\f"}, d2 = {"Lcom/epiroc/ble/di/AppModule;", "", "()V", "provideBluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "context", "Landroid/content/Context;", "provideConnectionBLEManager", "Lcom/epiroc/ble/data/ble/ConnectionBLEManager;", "bluetoothAdapter", "providePeripheralBLEManager", "Lcom/epiroc/ble/data/ble/PeripheralBLEManager;", "app_debug"})
@dagger.Module
public final class AppModule {
    @org.jetbrains.annotations.NotNull
    public static final com.epiroc.ble.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @javax.inject.Singleton
    @dagger.Provides
    public final android.bluetooth.BluetoothAdapter provideBluetoothAdapter(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @javax.inject.Singleton
    @dagger.Provides
    public final com.epiroc.ble.data.ble.ConnectionBLEManager provideConnectionBLEManager(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothAdapter bluetoothAdapter) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @javax.inject.Singleton
    @dagger.Provides
    public final com.epiroc.ble.data.ble.PeripheralBLEManager providePeripheralBLEManager(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothAdapter bluetoothAdapter) {
        return null;
    }
}