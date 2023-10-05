// Generated by Dagger (https://dagger.dev).
package com.epiroc.ble;

import android.bluetooth.BluetoothAdapter;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<BluetoothAdapter> bluetoothAdapterProvider;

  public MainActivity_MembersInjector(Provider<BluetoothAdapter> bluetoothAdapterProvider) {
    this.bluetoothAdapterProvider = bluetoothAdapterProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<BluetoothAdapter> bluetoothAdapterProvider) {
    return new MainActivity_MembersInjector(bluetoothAdapterProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectBluetoothAdapter(instance, bluetoothAdapterProvider.get());
  }

  @InjectedFieldSignature("com.epiroc.ble.MainActivity.bluetoothAdapter")
  public static void injectBluetoothAdapter(MainActivity instance,
      BluetoothAdapter bluetoothAdapter) {
    instance.bluetoothAdapter = bluetoothAdapter;
  }
}
