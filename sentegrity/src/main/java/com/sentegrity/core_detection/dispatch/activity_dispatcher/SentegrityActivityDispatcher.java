package com.sentegrity.core_detection.dispatch.activity_dispatcher;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.constants.SentegrityConstants;
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
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.trustlook.URLInfo;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.utilities.Helpers;
import com.stericson.RootShell.RootShell;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.cloudscan.ScanResult;
import com.trustlook.sdk.cloudscan.URLScanClient;
import com.trustlook.sdk.data.*;
import com.trustlook.sdk.data.Error;
import com.trustlook.sdk.urlscan.CatType;
import com.trustlook.sdk.urlscan.CategoryType;
import com.trustlook.sdk.urlscan.UrlScanResult;

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

    private Context context;
    private CloudScanClient cloudScanClient;
    private URLScanClient urlScanClient;

    public SentegrityActivityDispatcher(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("we need that context here!");
        }
        this.context = context;
    }

    public void runCoreDetectionActivities(Context context) {
        //we need to restart data
        scannedDevices = new HashSet<>();
        pairedDevices = new HashSet<>();

        accelRadsArray = new ArrayList<>();
        pitchRollArray = new ArrayList<>();
        headingsArray = new ArrayList<>();
        gyroRadsArray = new ArrayList<>();

        ambientLightArray = new ArrayList<>();

        startAmbientLight();
        startNetstat();
        startBluetooth();
        startLocation();
        startMotion();
        startCellularSignal();
        startRootCheck();
        startTrustLookAVScan();
    }

    /**
     * Starts netstat collection.
     * We use {@link SentegrityTrustFactorDatasetNetstat} for collecting TCP data on IPv4 and IPv6.
     * NetstatData status code will be {@link DNEStatusCode#ERROR} if exception occures for both methods.
     */
    public void startNetstat() {
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

                startTrustLookURLScan(tcpNetstatData);
            }
        }.start();
    }


    /**
     * Start searching for nearby bluetooth devices.
     * Bluetooth status code will be {@link DNEStatusCode#UNSUPPORTED} if there is
     * no bluetooth adapter or {@link DNEStatusCode#DISABLED} if bluetooth is disabled.
     *
     * @see BTScanner Bluetooth Scanner
     */
    public void startBluetooth() {

        scannedDevices = new HashSet<>();
        pairedDevices = new HashSet<>();

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDNEStatus(DNEStatusCode.UNSUPPORTED);
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDNEStatus(DNEStatusCode.UNSUPPORTED);
            return;
        } else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            SentegrityTrustFactorDatasets.getInstance().setScannedBTDNEStatus(DNEStatusCode.DISABLED);
            SentegrityTrustFactorDatasets.getInstance().setPairedBTDNEStatus(DNEStatusCode.DISABLED);
            return;
        }

        BTScanner.startScanning(context, this);
    }

    /**
     * Start location listener, if app has required permissions otherwise
     * just updates location status code to {@link DNEStatusCode#UNAUTHORIZED}.
     */
    public void startLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationListener();
            return;
        }

        //we already need to have permission at this moment
        //if there's no permission update DNEStatus as required
        SentegrityTrustFactorDatasets.getInstance().setLocationDNEStatus(DNEStatusCode.UNAUTHORIZED);

        /*final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
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
        Dexter.checkPermissions(new CompositeMultiplePermissionsListener(listener, listener2), permissions);*/
    }

    /**
     * Starts location listener.
     * Once we have location, we will stop collecting to preserve battery.
     */
    @SuppressWarnings("ResourceType") //we checked for permissions before calling this method
    private void startLocationListener() {
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
     * Starts collecting various motion data ({@link AccelRadsObject accel rads},
     * {@link PitchRollObject pitch roll}, {@link MagneticObject magnetic heading}, {@link GyroRadsObject gyro rads}).
     * Uses internal Sensor manager and different sensors.
     */
    public void startMotion() {

        accelRadsArray = new ArrayList<>();
        pitchRollArray = new ArrayList<>();
        headingsArray = new ArrayList<>();
        gyroRadsArray = new ArrayList<>();

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

                        //first values can come as 0.0, 0.0, 0.0 --> we don't need those
                        if (values[0] == 0 && values[1] == 0 && values[2] == 0)
                            return;

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
     * Starts collecting cellular signal data, and stops after first value.
     */
    //TODO: maybe extend this for 10-20seconds?, uses reflection to get LTE data
    public void startCellularSignal() {
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

    /**
     * Starts collecting ambient light data.
     * Value will be registered only when it's updated (no two same values in row).
     * If there is no light sensor we'll set ambient status to {@link DNEStatusCode#UNSUPPORTED}
     */
    public void startAmbientLight() {

        ambientLightArray = new ArrayList<>();

        final SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (light == null) {
            SentegrityTrustFactorDatasets.getInstance().setAmbientLightDNEStatus(DNEStatusCode.UNSUPPORTED);
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

    /**
     * Starts root check.
     * It will check if root access is given to the app, if busy box is available or if there's root available on the device.
     *
     * @see RootShell
     */
    public void startRootCheck() {
        final RootDetection rootDetection = new RootDetection();
        new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                Log.d("rootDetection", "start");
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                //takes around 0.1s
                rootDetection.isAccessGiven = RootShell.isAccessGiven();
                SentegrityTrustFactorDatasets.getInstance().setRootDetection(rootDetection);

                //TODO: set this now, or only when completely over?
                SentegrityTrustFactorDatasets.getInstance().setRootDetectionDNEStatus(DNEStatusCode.OK);

                Logger.INFO("RootDetecion[access] " + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();

                //takes around 0.5s
                rootDetection.isBusyBoxAvailable = RootShell.isBusyboxAvailable();
                SentegrityTrustFactorDatasets.getInstance().setRootDetection(rootDetection);

                Logger.INFO("RootDetecion[BusyBox] " + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();

                //RootChecker.isRooted() is another (maybe faster) way to check this
                //takes around 0.5s
                rootDetection.isRootAvailable = RootShell.isRootAvailable();
                SentegrityTrustFactorDatasets.getInstance().setRootDetection(rootDetection);

                Logger.INFO("RootDetecion[root] " + (System.currentTimeMillis() - start));
            }
        }.start();
    }

    /**
     * Starts TrustLook apps scan
     * It'll first collect installed app packages and create needed data (i.e. md5) or get it from local storage.
     * After that, if there were newly installed apps, we'll check them online with TrustLook scan.
     */
    public void startTrustLookAVScan() {
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                long current = System.currentTimeMillis();

                List<PkgInfo> listToCheck = new ArrayList<>();
                List<PkgInfo> currentCachedList = new ArrayList<>();
                List<PkgInfo> newCheckedList = new ArrayList<>();
                List<AppInfo> cachedBadApps = new ArrayList<>();
                List<AppInfo> newBadApps = new ArrayList<>();

                SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                String cachedListJson = sp.getString("cachedList", null);
                String cachedBadAppsJson = sp.getString("cachedBadApps", null);

                if (cachedListJson != null) {
                    currentCachedList = new Gson().fromJson(cachedListJson, new TypeToken<List<PkgInfo>>() {
                    }.getType());
                }
                if (!TextUtils.isEmpty(cachedBadAppsJson)) {
                    cachedBadApps = new Gson().fromJson(cachedBadAppsJson, new TypeToken<List<AppInfo>>() {
                    }.getType());
                }

                List<PackageInfo> packageInfoList = Helpers.getLocalAppsPkgInfo(SentegrityTrustFactorDatasets.getInstance().context);
                for (PackageInfo pi : packageInfoList) {
                    if (pi != null && pi.applicationInfo != null) {
                        PkgInfo pkgInfo = null;
                        for (PkgInfo cachedInfo : currentCachedList) {
                            if (TextUtils.equals(cachedInfo.getPkgName(), pi.packageName)) {
                                pkgInfo = cachedInfo;
                                //we'll not add cached data, since we already checked it
                                //pkgInfoList.add(pkgInfo);

                                //we'll create new list to get rid of removed apps
                                newCheckedList.add(pkgInfo);
                                break;
                            }
                        }
                        if (pkgInfo == null) {
                            pkgInfo = getCloudScanClient().populatePkgInfo(pi.packageName, pi.applicationInfo.publicSourceDir);
                            listToCheck.add(pkgInfo);
                        } else if (cachedBadApps.size() > 0) {
                            //we're going through known (cached) bad apps, and updating new list in case any of them was removed
                            for (AppInfo appInfo : cachedBadApps) {
                                if (TextUtils.equals(appInfo.getMd5(), pkgInfo.getMd5())) {
                                    newBadApps.add(appInfo);
                                }
                            }
                        }
                    }
                }

                //we'll do online scan only if there's been new apps since last online check
                if (listToCheck.size() > 0) {
                    ScanResult onlineResult = null;

                    onlineResult = getCloudScanClient().cloudScan(listToCheck);

                    if (onlineResult.isSuccess()) {
                        List<AppInfo> appInfoList;
                        appInfoList = onlineResult.getList();
                        for (AppInfo appInfo : appInfoList) {
                            if (appInfo.getScore() >= 8) {
                                //malware app
                                newBadApps.add(appInfo);
                            } else if (appInfo.getScore() == 7) {
                                //high risk app
                                newBadApps.add(appInfo);
                            } else if (appInfo.getScore() == 6) {
                                //non­aggressive risk app, we should skip these ?
                                //newBadApps.add(appInfo);
                            } else {
                                //if score is in [0,5]
                                //the app is safe
                            }
                        }
                        newCheckedList.addAll(listToCheck);
                    } else {
                        //TODO: handle different error codes (maybe UNAVAILABLE if no internet?)
                        int errorCode = onlineResult.getError();
                        if(errorCode == Error.NETWORK_ERROR){

                        }
                        SentegrityTrustFactorDatasets.getInstance().setTrustLookBadPkgListDNEStatus(DNEStatusCode.ERROR);

                        sp.edit().putString("cachedList", new Gson().toJson(newCheckedList)).apply();
                        sp.edit().putString("cachedBadApps", new Gson().toJson(newBadApps)).apply();

                        Log.d("trustlook", "time: " + (System.currentTimeMillis() - current));
                        return;
                    }

                }

                sp.edit().putString("cachedList", new Gson().toJson(newCheckedList)).apply();
                sp.edit().putString("cachedBadApps", new Gson().toJson(newBadApps)).apply();

                SentegrityTrustFactorDatasets.getInstance().setTrustLookBadPkgListDNEStatus(DNEStatusCode.OK);
                SentegrityTrustFactorDatasets.getInstance().setTrustLookBadPkgList(newBadApps);

                Log.d("trustlook", "time: " + (System.currentTimeMillis() - current));
            }
        }.start();
    }

    /**
     * Starts TrustLook URL scan
     */
    public void startTrustLookURLScan(List<ActiveConnection> connections){
        /*new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY);*/

        List<String> ipList = new ArrayList<>();
        for(ActiveConnection c : connections){
            if(c.isListening() || c.isLoopBack() || TextUtils.isEmpty(c.remoteIp) || (TextUtils.equals(c.localIp, "0.0.0.0") || (TextUtils.equals(c.localIp, "::"))))
                continue;
            ipList.add(c.remoteIp);
        }

            List<URLInfo> urlInfos = new ArrayList<>();

            SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            String cachedListJson = sp.getString("cachedURLList", null);

                UrlScanResult result = getURLScanClient().urlScan("https://www.bug.hr/");
                if(result.isSuccess()){
                    if(result.getUrlCategory().getType() == CatType.Malware){

                    }
                }else{
                    int errorCode = result.getError();
                    if(errorCode == Error.NETWORK_ERROR){

                    }
                    SentegrityTrustFactorDatasets.getInstance().setTrustLookBadPkgListDNEStatus(DNEStatusCode.ERROR);
                }
        //    }
        //}.start();
    }

    /**
     * Creates Cloud Scan Client for TrustLook implementation (online antivirus check)
     *
     * @return cloudScanClient instance
     */
    public CloudScanClient getCloudScanClient() {
        if (cloudScanClient == null) {
            cloudScanClient = new CloudScanClient.Builder().setContext(SentegrityTrustFactorDatasets.getInstance().context)
                    .setToken(SentegrityConstants.TRUSTLOOK_CLIENT_ID)
                    .setRegion(Region.INTL)
                    .setConnectionTimeout(2000)
                    .setSocketTimeout(2000)
                    .build();
        }
        return cloudScanClient;
    }

    /**
     * Creates Cloud Scan Client for TrustLook implementation (online antivirus check)
     *
     * @return cloudScanClient instance
     */
    public URLScanClient getURLScanClient() {
        if (urlScanClient == null) {
            urlScanClient = new URLScanClient.Builder().setContext(SentegrityTrustFactorDatasets.getInstance().context)
                    .setToken(SentegrityConstants.TRUSTLOOK_CLIENT_ID)
                    .setConnectionTimeout(2000)
                    .setSocketTimeout(2000)
                    .build();
        }
        return urlScanClient;
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
