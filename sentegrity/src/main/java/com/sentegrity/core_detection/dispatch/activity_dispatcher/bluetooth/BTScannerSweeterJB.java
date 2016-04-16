package com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.sentegrity.core_detection.constants.SentegrityConstants;

/**
 * Created by dmestrov on 16/04/16.
 */
public class BTScannerSweeterJB {

    public static void startScanning(final BTDeviceCallback callback) {

        final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                callback.onDeviceUpdate(device);
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter.getDefaultAdapter().stopLeScan(scanCallback);
            }
        }, SentegrityConstants.BLUETOOTH_SEARCH_TIME);

        BluetoothAdapter.getDefaultAdapter().startLeScan(scanCallback);
    }
}
