package com.sentegrity.core_detection.dispatch.activity_dispatcher;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth.BTDeviceCallback;
import com.sentegrity.core_detection.dispatch.activity_dispatcher.bluetooth.BTScanner;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetNetstat;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.ActiveConnection;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.MagneticObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.PitchRollObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.root.RootDetection;
import com.stericson.RootShell.RootShell;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dmestrov on 05/04/16.
 */
public class SentegrityActivityDispatcher implements BTDeviceCallback {

    private Handler mHandler = new Handler();

    public void runCoreDetectionActivities(Context context) {
        //we need to restart data
        scannedDevices = new HashSet<>();
        pairedDevices = new HashSet<>();

        accelRadsArray = new ArrayList<>();
        pitchRollArray = new ArrayList<>();
        headingsArray = new ArrayList<>();
        gyroRadsArray = new ArrayList<>();

        ambientLightArray = new ArrayList<>();

        startAmbientLight(context);
        startNetstat();
        startBluetooth(context);
        startLocation(context);
        startMotion(context);
        startCellularSignal(context);
        startRootCheck();
    }

    private void startNetstat() {
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                List<ActiveConnection> tcpNetstatData = new ArrayList<>();

                boolean failedV4 = false, failedV6 = false;
                try {
                    tcpNetstatData.addAll(SentegrityTrustFactorDatasetNetstat.getTcp4());
                } catch (IOException e) {
                    failedV4 = true;
                }
                try {
                    tcpNetstatData.addAll(SentegrityTrustFactorDatasetNetstat.getTcp6());
                } catch (IOException e) {
                    failedV6 = true;
                }

                if (failedV6 && failedV4) {
                    SentegrityTrustFactorDatasets.getInstance().setNetstatData(null);
                    SentegrityTrustFactorDatasets.getInstance().setNetstatDataDNEStatus(DNEStatusCode.ERROR);
                    return;
                }

                SentegrityTrustFactorDatasets.getInstance().setNetstatDataDNEStatus(DNEStatusCode.OK);
                SentegrityTrustFactorDatasets.getInstance().setNetstatData(tcpNetstatData);
            }
        }.start();
    }


    /**
     * Get BLE and classic devices nearby (uses broadcast receiver)
     */
    private void startBluetooth(Context context) {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDNEStatus(DNEStatusCode.UNSUPPORTED);
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDNEStatus(DNEStatusCode.UNSUPPORTED);
            return;
        }

        BTScanner.startScanning(context, this);
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
        if (!Dexter.isRequestOngoing())
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
        if (gyroSensor == null) {
            SentegrityTrustFactorDatasets.getInstance().setGyroMotionDNEStatus(DNEStatusCode.UNSUPPORTED);
            SentegrityTrustFactorDatasets.getInstance().setUserMovementDNEStatus(DNEStatusCode.UNSUPPORTED);
        } else {
            //USER MOVEMENT!?


            //GRIP MOVEMENT
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    GyroRadsObject object = new GyroRadsObject(event.values);
                    gyroRadsArray.add(object);
                    SentegrityTrustFactorDatasets.getInstance().setGyroRads(gyroRadsArray);

                    if (gyroRadsArray.size() > 3)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, gyroSensor, 1000000);
        }


        //DEVICE MOTION
        //probably orientation -> this requires accel and mag
        //maybe even use gyro if available?
        Sensor acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (acc == null || mag == null) {
            //we don't have pitch/roll data ?
        } else {
            SensorEventListener listener = new SensorEventListener() {
                float rotation[] = null; //for gravity rotational data
                float accels[] = new float[3];
                float mags[] = new float[3];
                float[] values = new float[3];

                @Override
                public void onSensorChanged(SensorEvent event) {
                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            mags = event.values.clone();
                            break;
                        case Sensor.TYPE_ACCELEROMETER:
                            accels = event.values.clone();
                            break;
                    }

                    if (mags != null && accels != null) {
                        rotation = new float[9];
                        SensorManager.getRotationMatrix(rotation, null, accels, mags);

                        float[] outR = new float[9];
                        SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
                        SensorManager.getOrientation(outR, values);

                        mags = null;
                        accels = null;

                        PitchRollObject object = new PitchRollObject(values);

                        pitchRollArray.add(object);
                        SentegrityTrustFactorDatasets.getInstance().setPitchRoll(pitchRollArray);

                        if (pitchRollArray.size() > 3)
                            sensorManager.unregisterListener(this);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(listener, acc, 1000000);
            sensorManager.registerListener(listener, mag, 1000000);
        }

        //MAGNETOMETER DATA
        Sensor magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (magnetometerSensor == null) {
            SentegrityTrustFactorDatasets.getInstance().setMagneticHeadingDNEStatus(DNEStatusCode.UNSUPPORTED);
        } else {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    MagneticObject object = new MagneticObject(event.values);
                    headingsArray.add(object);
                    SentegrityTrustFactorDatasets.getInstance().setMagneticHeading(headingsArray);

                    if (headingsArray.size() > 5)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, magnetometerSensor, 1000000);
        }

        //ACCELEROMETER DATA
        Sensor accelerometerData = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (accelerometerData == null) {
            SentegrityTrustFactorDatasets.getInstance().setAccelMotionDNEStatus(DNEStatusCode.UNSUPPORTED);
        } else {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    AccelRadsObject object = new AccelRadsObject(event.values);
                    accelRadsArray.add(object);
                    SentegrityTrustFactorDatasets.getInstance().setAccelRads(accelRadsArray);

                    if (accelRadsArray.size() > 3)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, accelerometerData, 1000000);
        }
    }

    /**
     * Get signal strength
     */
    //TODO: maybe extend this for 10-20seconds?
    private void startCellularSignal(Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                Log.d("strength", "strength start calc");
                int strength = 0;
                boolean gotValidValue = false;

                //TODO: try another way of getting info --> signalStrength.toString() --> split
                if (signalStrength.isGsm()) {
                    strength = signalStrength.getGsmSignalStrength();

                    //GSM values -> (0-31, 99) valid (TS 27.007)
                    if (strength >= 0 && strength <= 31)
                        gotValidValue = true;

                    if (strength == 99) {
                        try {
                            Method method = SignalStrength.class.getMethod("getLteSignalStrength");
                            strength = (int) method.invoke(signalStrength);

                            //LTE values -> (0-63, 99) valid (TS 36.331)
                            if (strength >= 0 && strength <= 63)
                                gotValidValue = true;

                        } catch (SecurityException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (signalStrength.getCdmaDbm() > 0) {
                    strength = signalStrength.getCdmaDbm();
                } else {
                    strength = signalStrength.getEvdoDbm();
                }

                if (gotValidValue) {
                    SentegrityTrustFactorDatasets.getInstance().setCellularSignalRaw(strength);
                    Log.d("strength", "strength " + strength);
                } else {
                    SentegrityTrustFactorDatasets.getInstance().setCellularSignalDNEStatus(DNEStatusCode.UNAVAILABLE);
                }
                //TODO: have array of values or only one?
                telephonyManager.listen(this, LISTEN_NONE);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void startAmbientLight(Context context) {
        final SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (light == null) {
            SentegrityTrustFactorDatasets.getInstance().setAmbientLightDNEstatus(DNEStatusCode.UNSUPPORTED);
        } else {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    int value = (int) Math.min(event.values[0], event.sensor.getMaximumRange());
                    ambientLightArray.add(0, value);
                    SentegrityTrustFactorDatasets.getInstance().setAmbientLight(ambientLightArray);

                    Log.d("light", "got light " + value);
                    if (ambientLightArray.size() > 5)
                        sensorManager.unregisterListener(this);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, light, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void startRootCheck(){
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                RootDetection rootDetection = new RootDetection();

                rootDetection.isAccessGiven = RootShell.isAccessGiven();
                SentegrityTrustFactorDatasets.getInstance().setRootDetection(rootDetection);

                rootDetection.isRootAvailable = RootShell.isRootAvailable();
                SentegrityTrustFactorDatasets.getInstance().setRootDetection(rootDetection);

                rootDetection.isBusyBoxAvailable = RootShell.isBusyboxAvailable();
                SentegrityTrustFactorDatasets.getInstance().setRootDetection(rootDetection);
            }
        }.start();
    }

    /**
     * Bluetooth helpers
     */

    Set<String> scannedDevices = new HashSet<>();
    Set<String> pairedDevices = new HashSet<>();

    @Override
    public void onDeviceUpdate(BluetoothDevice device) {
        updateBTDevice(device);
    }

    @Override
    public void onStateUpdate(int btAdapterState) {
        if (btAdapterState == BluetoothAdapter.STATE_OFF) {
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDNEStatus(DNEStatusCode.DISABLED);
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDNEStatus(DNEStatusCode.DISABLED);
        } else if (btAdapterState == BluetoothAdapter.STATE_ON) {
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDNEStatus(DNEStatusCode.OK);
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDNEStatus(DNEStatusCode.OK);
        }
    }

    private void updateBTDevice(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.d("bluetooth", "got bonded: " + device.getAddress() + ": " + device.getName());
            pairedDevices.add(device.getAddress());
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDevices(pairedDevices);
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDNEStatus(DNEStatusCode.OK);
        } else {
            Log.d("bluetooth", "got scanned: " + device.getAddress() + ": " + device.getName());
            scannedDevices.add(device.getAddress());
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDevices(scannedDevices);
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDNEStatus(DNEStatusCode.OK);
        }
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

    /**
     * Ambient Light helpers
     */
    private List<Integer> ambientLightArray = new ArrayList<>();
    /**
     * End Ambient Light helpers
     */
}
