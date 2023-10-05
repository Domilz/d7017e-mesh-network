package com.epiroc.ble.data.ble;

import java.lang.System;

@android.annotation.SuppressLint(value = {"MissingPermission"})
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010)\u001a\u00020*H\u0016J\b\u0010+\u001a\u00020*H\u0016J\u0010\u0010,\u001a\u00020*2\u0006\u0010-\u001a\u00020.H\u0002J\u001a\u0010/\u001a\u0004\u0018\u00010.2\u0006\u00100\u001a\u00020\b2\u0006\u00101\u001a\u00020\bH\u0002J\b\u00102\u001a\u00020*H\u0016J\b\u00103\u001a\u00020*H\u0016J\u0018\u00104\u001a\u00020*2\u0006\u00105\u001a\u0002062\u0006\u00107\u001a\u000208H\u0002R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R#\u0010\u000f\u001a\n \u0011*\u0004\u0018\u00010\u00100\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R \u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u001b0\u001aX\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0010\u0010\u001f\u001a\u0004\u0018\u00010 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\"X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\'\u001a\n \u0011*\u0004\u0018\u00010(0(X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00069"}, d2 = {"Lcom/epiroc/ble/data/ble/ConnectionBLEManager;", "Lcom/epiroc/ble/data/ConnectManager;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "context", "Landroid/content/Context;", "(Landroid/bluetooth/BluetoothAdapter;Landroid/content/Context;)V", "DEVICE_ADDRESS", "", "DEVICE_NAME", "INDOOR_CHARACTERISTIC_EAST_UUID", "INDOOR_CHARACTERISTIC_NORHT_UUID", "INDOOR_SERVICE_UUID", "MAXIMUM_CONNECTION_ATTEMPTS", "", "bleScanner", "Landroid/bluetooth/le/BluetoothLeScanner;", "kotlin.jvm.PlatformType", "getBleScanner", "()Landroid/bluetooth/le/BluetoothLeScanner;", "bleScanner$delegate", "Lkotlin/Lazy;", "coroutineScope", "Lkotlinx/coroutines/CoroutineScope;", "currentConnectionAttempt", "data", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/epiroc/ble/util/Resource;", "Lcom/epiroc/ble/data/ConnectionResult;", "getData", "()Lkotlinx/coroutines/flow/MutableSharedFlow;", "gatt", "Landroid/bluetooth/BluetoothGatt;", "gattCallback", "Landroid/bluetooth/BluetoothGattCallback;", "isScanning", "", "scanCallback", "Landroid/bluetooth/le/ScanCallback;", "scanSettings", "Landroid/bluetooth/le/ScanSettings;", "closeConnection", "", "disconnect", "enableNotification", "characteristic", "Landroid/bluetooth/BluetoothGattCharacteristic;", "findCharacteristic", "serviceUUID", "characteristicsUUID", "reconnect", "startReceiving", "writeDescription", "descriptor", "Landroid/bluetooth/BluetoothGattDescriptor;", "payload", "", "app_debug"})
public final class ConnectionBLEManager implements com.epiroc.ble.data.ConnectManager {
    private final android.bluetooth.BluetoothAdapter bluetoothAdapter = null;
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.epiroc.ble.util.Resource<com.epiroc.ble.data.ConnectionResult>> data = null;
    private final java.lang.String DEVICE_ADDRESS = "6D:65:37:B3:7E:21";
    private final java.lang.String DEVICE_NAME = "OnePlus 6T";
    private final java.lang.String INDOOR_SERVICE_UUID = "00001821-0000-1000-8000-00805f9b34fb";
    private final java.lang.String INDOOR_CHARACTERISTIC_EAST_UUID = "00002ab1-0000-1000-8000-00805f9b34fb";
    private final java.lang.String INDOOR_CHARACTERISTIC_NORHT_UUID = "00002ab0-0000-1000-8000-00805f9b34fb";
    private final kotlin.Lazy bleScanner$delegate = null;
    private final android.bluetooth.le.ScanSettings scanSettings = null;
    private android.bluetooth.BluetoothGatt gatt;
    private boolean isScanning = false;
    private final kotlinx.coroutines.CoroutineScope coroutineScope = null;
    private final android.bluetooth.le.ScanCallback scanCallback = null;
    private int currentConnectionAttempt = 1;
    private int MAXIMUM_CONNECTION_ATTEMPTS = 5;
    private final android.bluetooth.BluetoothGattCallback gattCallback = null;
    
    @javax.inject.Inject
    public ConnectionBLEManager(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothAdapter bluetoothAdapter, @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @java.lang.Override
    public kotlinx.coroutines.flow.MutableSharedFlow<com.epiroc.ble.util.Resource<com.epiroc.ble.data.ConnectionResult>> getData() {
        return null;
    }
    
    private final android.bluetooth.le.BluetoothLeScanner getBleScanner() {
        return null;
    }
    
    private final void enableNotification(android.bluetooth.BluetoothGattCharacteristic characteristic) {
    }
    
    private final void writeDescription(android.bluetooth.BluetoothGattDescriptor descriptor, byte[] payload) {
    }
    
    private final android.bluetooth.BluetoothGattCharacteristic findCharacteristic(java.lang.String serviceUUID, java.lang.String characteristicsUUID) {
        return null;
    }
    
    @java.lang.Override
    public void startReceiving() {
    }
    
    @java.lang.Override
    public void reconnect() {
    }
    
    @java.lang.Override
    public void disconnect() {
    }
    
    @java.lang.Override
    public void closeConnection() {
    }
}