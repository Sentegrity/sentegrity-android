package com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import com.sentegrity.core_detection.constants.SentegrityConstants;

/**
 * Created by dmestrov on 16/04/16.
 */
public class BTScanner {

    public static void startScanning(final Context context, final BTDeviceCallback callback) {
        if (callback == null)
            throw new IllegalArgumentException("Bluetooth device callback cannot be null!");

        final boolean prevStateEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();

        if (prevStateEnabled) {
            for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
                callback.onDeviceUpdate(device);
            }
            scan(context, callback);
            return;
        }

        final BroadcastReceiver btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context ctx, Intent intent) {
                String action = intent.getAction();

                if (TextUtils.equals(BluetoothAdapter.ACTION_STATE_CHANGED, action)) {

                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    callback.onStateUpdate(state);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
                                callback.onDeviceUpdate(device);
                            }
                            scan(context, callback);
                            context.unregisterReceiver(this);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    BluetoothAdapter.getDefaultAdapter().disable();
                                }
                            }, SentegrityConstants.BLUETOOTH_SEARCH_TIME + 1000);
                            break;
                    }
                }
            }
        };

        IntentFilter bt = new IntentFilter();
        bt.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(btReceiver, bt);

        BluetoothAdapter.getDefaultAdapter().enable();
    }

    private static void scan(Context context, BTDeviceCallback callback) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) || Build.VERSION.SDK_INT < 18) {
            BTScannerClassic.startScanning(context, callback);
            return;
        }

        if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 21) {
            BTScannerSweeterJB.startScanning(callback);
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            BTScannerLollipop.startScanning(callback);
            return;
        }
    }
}
