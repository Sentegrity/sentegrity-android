package com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Handler;

import com.sentegrity.core_detection.constants.SentegrityConstants;

/**
 * Created by dmestrov on 16/04/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BTScannerLollipop {

    public static void startScanning(final BTDeviceCallback callback) {

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                callback.onDeviceUpdate(result.getDevice());
            }
        };

        final BluetoothLeScanner leScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                leScanner.stopScan(scanCallback);
            }
        }, SentegrityConstants.BLUETOOTH_SEARCH_TIME);

        leScanner.startScan(scanCallback);
    }
}
