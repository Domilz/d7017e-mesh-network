package com.epiroc.ble.data;

import java.lang.System;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\tH&J\b\u0010\u000b\u001a\u00020\tH&J\b\u0010\f\u001a\u00020\tH&R\u001e\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\r"}, d2 = {"Lcom/epiroc/ble/data/ConnectManager;", "", "data", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/epiroc/ble/util/Resource;", "Lcom/epiroc/ble/data/ConnectionResult;", "getData", "()Lkotlinx/coroutines/flow/MutableSharedFlow;", "closeConnection", "", "disconnect", "reconnect", "startReceiving", "app_debug"})
public abstract interface ConnectManager {
    
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.MutableSharedFlow<com.epiroc.ble.util.Resource<com.epiroc.ble.data.ConnectionResult>> getData();
    
    public abstract void reconnect();
    
    public abstract void disconnect();
    
    public abstract void startReceiving();
    
    public abstract void closeConnection();
}