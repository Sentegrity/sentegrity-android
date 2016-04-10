package com.sentegrity.core_detection.dispatch.activity_dispatcher;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.MagneticObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.PitchRollObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dmestrov on 05/04/16.
 */
public class SentegrityActivityDispatcher {


    public void runCoreDetectionActivities(Context context) {
        bleDevices = new HashSet<>();
        classicDevices = new HashSet<>();

        accelRadsArray = new ArrayList<>();
        pitchRollArray = new ArrayList<>();
        headingsArray = new ArrayList<>();
        gyroRadsArray = new ArrayList<>();

        startBluetooth(context);
        startLocation(context);
        startMotion(context);
    }

    /**
     * Get BLE and classic devices nearby (uses broadcast receiver)
     */
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

            unregisterBTReceivers(context, 5000);
            //TODO: different trustfactor?
//            for (BluetoothDevice device : BTAdapter.getBondedDevices()) {
//                updateBLDevice(device);
//            }
        }
    }

    /**
     * Get location data (uses location listener)
     */
    private void startLocation(final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationListener(context);
            return;
        }

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
                if (report.getGrantedPermissionResponses().size() == permissions.length) {
                    // we have location permission
                    startLocationListener(context);
                } else if (report.getDeniedPermissionResponses().size() == permissions.length) {
                    // we don't have location permission
                    SentegrityTrustFactorDatasets.getInstance().setLocationDNEStatus(DNEStatusCode.UNAUTHORIZED);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
            }
        };
        if(!Dexter.isRequestOngoing())
            Dexter.continuePendingRequestsIfPossible(new CompositeMultiplePermissionsListener(listener, listener2));
        Dexter.checkPermissions(new CompositeMultiplePermissionsListener(listener, listener2), permissions);
    }

    private void startLocationListener(Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d("activityDispatcher", "location: " + location);
                if (location == null)
                    return;
                SentegrityTrustFactorDatasets.getInstance().setLocationDNEStatus(DNEStatusCode.OK);
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

    /**
     * Get Motion pitch/roll, movement, orientation data
     */
    private void startMotion(Context context) {

        final SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //Gyro Data (grip)
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(gyroSensor == null){
            SentegrityTrustFactorDatasets.getInstance().setGyroMotionDNEStatus(DNEStatusCode.UNSUPPORTED);
//            SentegrityTrustFactorDatasets.getInstance().setMagneticHeadingDNEStatus(DNEStatusCode.UNSUPPORTED);
            SentegrityTrustFactorDatasets.getInstance().setUserMovementDNEStatus(DNEStatusCode.UNSUPPORTED);
        }else{

            //DEVICE MOTION!?
            //probably orientation -> this requires accel and mag

            Sensor acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            SensorEventListener listener = new SensorEventListener() {
                float[] mGravity;
                float[] mGeomagnetic;
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                        mGravity = event.values;
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                        mGeomagnetic = event.values;
                    if (mGravity != null && mGeomagnetic != null) {
                        float R[] = new float[9];
                        float I[] = new float[9];
                        boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                        if (success) {
                            float orientationData[] = new float[3];
                            SensorManager.getOrientation(R, orientationData);
                            PitchRollObject object = new PitchRollObject(SensorManager.getOrientation(R, orientationData));

                            pitchRollArray.add(object);
                            SentegrityTrustFactorDatasets.getInstance().setPitchRoll(pitchRollArray);

                            if(pitchRollArray.size() > 3)
                                sensorManager.unregisterListener(this);
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(listener, acc, 1000);
            sensorManager.registerListener(listener, mag, 1000);


            //USER MOVEMENT!?


            //GRIP MOVEMENT
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    GyroRadsObject object = new GyroRadsObject(event.values);
                    gyroRadsArray.add(object);
                    SentegrityTrustFactorDatasets.getInstance().setGyroRads(gyroRadsArray);

                    if(gyroRadsArray.size() > 3)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, gyroSensor, 1000);

        }

        //MAGNETOMETER DATA
        Sensor magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if(magnetometerSensor == null){
            SentegrityTrustFactorDatasets.getInstance().setMagneticHeadingDNEStatus(DNEStatusCode.UNSUPPORTED);
        }else{
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    MagneticObject object = new MagneticObject(event.values);
                    headingsArray.add(object);
                    SentegrityTrustFactorDatasets.getInstance().setMagneticHeading(headingsArray);

                    if(headingsArray.size() > 5)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, magnetometerSensor, 1000);
        }

        //ACCELEROMETER DATA
        Sensor accelerometerData = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometerData == null){
            SentegrityTrustFactorDatasets.getInstance().setAccelMotionDNEStatus(DNEStatusCode.UNSUPPORTED);
        }else{
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    AccelRadsObject object = new AccelRadsObject(event.values);
                    accelRadsArray.add(object);
                    SentegrityTrustFactorDatasets.getInstance().setAccelRads(accelRadsArray);

                    if(accelRadsArray.size() > 3)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, accelerometerData, 1000);
        }
    }



    /**
     * Bluetooth helpers
     */
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

    private void updateBLDevice(BluetoothDevice device) {
        if (device.getType() == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {

        } else if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            classicDevices.add(device.getAddress());
            SentegrityTrustFactorDatasets.getInstance().setConnectedClassicBTDevices(classicDevices);
            SentegrityTrustFactorDatasets.getInstance().setConnectedClassicDNEStatus(DNEStatusCode.OK);
        } else {
            bleDevices.add(device.getAddress());
            SentegrityTrustFactorDatasets.getInstance().setDiscoveredBLEDevices(bleDevices);
            SentegrityTrustFactorDatasets.getInstance().setDiscoveredBLEDNEStatus(DNEStatusCode.OK);
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

    /**
     * End bluetooth helpers
     */

    /**
     * Motion helpers
     */
    private List<AccelRadsObject> accelRadsArray = new ArrayList<>();
    private List<PitchRollObject> pitchRollArray = new ArrayList<>();
    private List<MagneticObject> headingsArray = new ArrayList<>();
    private List<GyroRadsObject> gyroRadsArray = new ArrayList<>();

    /**
     * End motion helpers
     */
}
