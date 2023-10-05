package com.epiroc.ble.data.ble;

import java.lang.System;

@kotlin.Metadata(mv = {1, 8, 0}, k = 2, d1 = {"\u00002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\u001a\u0012\u0010\u0002\u001a\u00020\u0003*\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u001a\u0012\u0010\u0007\u001a\u00020\u0003*\u00020\b2\u0006\u0010\t\u001a\u00020\u0006\u001a\n\u0010\n\u001a\u00020\u0003*\u00020\u0004\u001a\n\u0010\u000b\u001a\u00020\u0003*\u00020\b\u001a\n\u0010\f\u001a\u00020\u0003*\u00020\b\u001a\n\u0010\r\u001a\u00020\u0003*\u00020\b\u001a\n\u0010\r\u001a\u00020\u0003*\u00020\u0004\u001a\n\u0010\u000e\u001a\u00020\u0003*\u00020\b\u001a\n\u0010\u000e\u001a\u00020\u0003*\u00020\u0004\u001a\n\u0010\u000f\u001a\u00020\u0003*\u00020\b\u001a\n\u0010\u0010\u001a\u00020\u0011*\u00020\u0012\u001a\n\u0010\u0013\u001a\u00020\u0001*\u00020\b\u001a\n\u0010\u0013\u001a\u00020\u0001*\u00020\u0004\u001a\n\u0010\u0014\u001a\u00020\u0001*\u00020\u0015\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"CCCD_DESCRIPTOR_UUID", "", "containsPermission", "", "Landroid/bluetooth/BluetoothGattDescriptor;", "permission", "", "containsProperty", "Landroid/bluetooth/BluetoothGattCharacteristic;", "property", "isCccd", "isIndicatable", "isNotifiable", "isReadable", "isWritable", "isWritableWithoutResponse", "printGattTable", "", "Landroid/bluetooth/BluetoothGatt;", "printProperties", "toHexString", "", "app_debug"})
public final class BleExtensionsKt {
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CCCD_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB";
    
    public static final void printGattTable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGatt $this$printGattTable) {
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String printProperties(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$printProperties) {
        return null;
    }
    
    public static final boolean isReadable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$isReadable) {
        return false;
    }
    
    public static final boolean isWritable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$isWritable) {
        return false;
    }
    
    public static final boolean isWritableWithoutResponse(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$isWritableWithoutResponse) {
        return false;
    }
    
    public static final boolean isIndicatable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$isIndicatable) {
        return false;
    }
    
    public static final boolean isNotifiable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$isNotifiable) {
        return false;
    }
    
    public static final boolean containsProperty(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattCharacteristic $this$containsProperty, int property) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String printProperties(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattDescriptor $this$printProperties) {
        return null;
    }
    
    public static final boolean isReadable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattDescriptor $this$isReadable) {
        return false;
    }
    
    public static final boolean isWritable(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattDescriptor $this$isWritable) {
        return false;
    }
    
    public static final boolean containsPermission(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattDescriptor $this$containsPermission, int permission) {
        return false;
    }
    
    public static final boolean isCccd(@org.jetbrains.annotations.NotNull
    android.bluetooth.BluetoothGattDescriptor $this$isCccd) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String toHexString(@org.jetbrains.annotations.NotNull
    byte[] $this$toHexString) {
        return null;
    }
}