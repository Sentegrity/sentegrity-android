package com.sentegrity.core_detection.dispatch.activity_dispatcher;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dmestrov on 05/04/16.
 */
public class SentegrityActivityDispatcher {

    Set<String> bleDevices = new HashSet<>();
    Set<String> classicDevices = new HashSet<>();
    private boolean prevStateEnabled = false;

    private BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            Log.d("bluetooth", action);

            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                updateBLDevice(device);
                return;
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        SentegrityTrustFactorDatasets.getInstance().setConnectedClassicDNEStatus(DNEStatusCode.DISABLED);
                        SentegrityTrustFactorDatasets.getInstance().setDiscoveredBLEDNEStatus(DNEStatusCode.DISABLED);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        SentegrityTrustFactorDatasets.getInstance().setConnectedClassicDNEStatus(DNEStatusCode.OK);
                        SentegrityTrustFactorDatasets.getInstance().setDiscoveredBLEDNEStatus(DNEStatusCode.OK);
                        BluetoothAdapter.getDefaultAdapter().startDiscovery();
                        unregisterBTReceivers(context, 5000);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
                return;
            }
        }
    };


    public void runCoreDetectionActivities(Context context) {
        bleDevices = new HashSet<>();
        classicDevices = new HashSet<>();
        startBluetooth(context);
        startLocation(context);
    }

    private void updateBLDevice(BluetoothDevice device) {
        if (device.getType() == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {

        } else if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            classicDevices.add(device.getAddress());
            SentegrityTrustFactorDatasets.getInstance().setConnectedClassicBTDevices(classicDevices);
        } else {
            bleDevices.add(device.getAddress());
            SentegrityTrustFactorDatasets.getInstance().setDiscoveredBLEDevices(bleDevices);
        }
    }

    private void unregisterBTReceivers(Context context) {
        try {
            context.unregisterReceiver(btReceiver);
        } catch (IllegalArgumentException e) {

        }
    }

    private void unregisterBTReceivers(final Context context, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                unregisterBTReceivers(context);
                if (!prevStateEnabled) {
                    BluetoothAdapter.getDefaultAdapter().disable();
                    return;
                }

                if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            }
        }, delay);
    }

    private void startBluetooth(Context context) {
        unregisterBTReceivers(context);

        BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter == null) {
            SentegrityTrustFactorDatasets.getInstance().setConnectedClassicDNEStatus(DNEStatusCode.UNSUPPORTED);
            SentegrityTrustFactorDatasets.getInstance().setDiscoveredBLEDNEStatus(DNEStatusCode.UNSUPPORTED);
            return;
        }
        IntentFilter bt = new IntentFilter();
        bt.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bt.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        context.registerReceiver(btReceiver, bt);

        prevStateEnabled = BTAdapter.isEnabled();

        if (!prevStateEnabled) {
            BTAdapter.enable();
        } else {
            if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
            unregisterBTReceivers(context, 5000);
            for (BluetoothDevice device : BTAdapter.getBondedDevices()) {
                updateBLDevice(device);
            }
        }
    }

    private void startLocation(final Context context) {
        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        MultiplePermissionsListener listener = DialogOnAnyDeniedMultiplePermissionsListener.Builder
                .withContext(context)
                .withTitle("Location Permission")
                .withMessage("We need your location")
                .withButtonText("Ok")
                .build();
        MultiplePermissionsListener listener2 = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.getGrantedPermissionResponses().size() == permissions.length){
                    // we have location permission
                    startLocationListener(context);
                }else if(report.getDeniedPermissionResponses().size() == permissions.length){
                    // we don't have location permission
                    SentegrityTrustFactorDatasets.getInstance().setLocationDNEStatus(DNEStatusCode.UNAUTHORIZED);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
            }
        };
        Dexter.checkPermissions(new CompositeMultiplePermissionsListener(listener, listener2), permissions);
    }

    private void startLocationListener(Context context){
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d("activityDispatcher", "location: " + location);
                SentegrityTrustFactorDatasets.getInstance().setLocation(location);
                locationManager.removeUpdates(this);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, context.getMainLooper());
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, context.getMainLooper());
//        Location location;
//        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if(location == null)
//            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if(location == null)
//            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//        if(location == null)
//            return;
//        SentegrityTrustFactorDatasets.getInstance().setLocation(location);
    }


}
