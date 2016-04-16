package com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by dmestrov on 16/04/16.
 */
public interface BTDeviceCallback {
    void onDeviceUpdate(BluetoothDevice device);
    void onStateUpdate(int btAdapterState);
}
