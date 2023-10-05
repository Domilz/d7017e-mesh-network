package com.epiroc.ble.screens;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u001d\u001a\u00020\u001eJ\u0006\u0010\u001f\u001a\u00020\u001eJ\b\u0010 \u001a\u00020\u001eH\u0014J\u0006\u0010!\u001a\u00020\u001eJ\b\u0010\"\u001a\u00020\u001eH\u0002R+\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\f\u0010\r\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000e8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0014\u0010\r\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R/\u0010\u0015\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0018\u0010\r\u001a\u0004\b\u0016\u0010\t\"\u0004\b\u0017\u0010\u000bR/\u0010\u0019\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001c\u0010\r\u001a\u0004\b\u001a\u0010\t\"\u0004\b\u001b\u0010\u000b\u00a8\u0006#"}, d2 = {"Lcom/epiroc/ble/screens/BleListViewModel;", "Landroidx/lifecycle/ViewModel;", "connectionBLEManager", "Lcom/epiroc/ble/data/ble/ConnectionBLEManager;", "(Lcom/epiroc/ble/data/ble/ConnectionBLEManager;)V", "<set-?>", "", "connectedDevice", "getConnectedDevice", "()Ljava/lang/String;", "setConnectedDevice", "(Ljava/lang/String;)V", "connectedDevice$delegate", "Landroidx/compose/runtime/MutableState;", "Lcom/epiroc/ble/data/ConnectionState;", "connectionState", "getConnectionState", "()Lcom/epiroc/ble/data/ConnectionState;", "setConnectionState", "(Lcom/epiroc/ble/data/ConnectionState;)V", "connectionState$delegate", "errorMessage", "getErrorMessage", "setErrorMessage", "errorMessage$delegate", "initializingMessage", "getInitializingMessage", "setInitializingMessage", "initializingMessage$delegate", "disconnect", "", "initializeConnection", "onCleared", "reconnect", "subscribeToChanges", "app_debug"})
public final class BleListViewModel extends androidx.lifecycle.ViewModel {
    private final com.epiroc.ble.data.ble.ConnectionBLEManager connectionBLEManager = null;
    @org.jetbrains.annotations.Nullable
    private final androidx.compose.runtime.MutableState initializingMessage$delegate = null;
    @org.jetbrains.annotations.Nullable
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState connectedDevice$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState connectionState$delegate = null;
    
    @javax.inject.Inject
    public BleListViewModel(@org.jetbrains.annotations.NotNull
    com.epiroc.ble.data.ble.ConnectionBLEManager connectionBLEManager) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getInitializingMessage() {
        return null;
    }
    
    private final void setInitializingMessage(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getConnectedDevice() {
        return null;
    }
    
    private final void setConnectedDevice(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.epiroc.ble.data.ConnectionState getConnectionState() {
        return null;
    }
    
    public final void setConnectionState(@org.jetbrains.annotations.NotNull
    com.epiroc.ble.data.ConnectionState p0) {
    }
    
    private final void subscribeToChanges() {
    }
    
    public final void disconnect() {
    }
    
    public final void reconnect() {
    }
    
    public final void initializeConnection() {
    }
    
    @java.lang.Override
    protected void onCleared() {
    }
}