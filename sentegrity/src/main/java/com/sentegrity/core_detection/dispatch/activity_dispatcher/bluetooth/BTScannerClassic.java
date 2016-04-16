package com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.sentegrity.core_detection.constants.SentegrityConstants;

/**
 * Created by dmestrov on 16/04/16.
 */
public class BTScannerClassic {

    public static void startScanning(final Context context, final BTDeviceCallback callback) {

        final BroadcastReceiver btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    callback.onDeviceUpdate(device);
                }
            }
        };

        IntentFilter bt = new IntentFilter();
        bt.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        context.registerReceiver(btReceiver, bt);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                context.unregisterReceiver(btReceiver);
            }
        }, SentegrityConstants.BLUETOOTH_SEARCH_TIME);

        if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }
}
