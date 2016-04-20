package com.sentegrity.core_detection.dispatch.trust_factors;

import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.location.Location;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetApplication;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetMotion;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetRoute;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.application.AppInfo;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.ActiveConnection;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.route.ActiveRoute;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.MagneticObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.PitchRollObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;

/**
 * Created by dmestrov on 23/03/16.
 */
public class SentegrityTrustFactorDatasets {

    private long runTime = -1;

    private int hourOfDay = -1;
    private int dayOfWeek = -1;
    private String batteryState;
    private Boolean tethering = null;
    private String carrierConnectionName;
    private Boolean airplaneMode = null;
    private Boolean wifiEnabled = null;
    private WifiInfo wifiInfo;
    private Location location;
    private Float brightness = null;
    private Integer cellularSignalRaw = null;
    private Float gripMovement = null;
    private String userMovement = null;
    private String deviceOrientation = null;
    private Boolean passcodeSet = null;

    private Set<String> pairedBTDevices;
    private Set<String> scannedBTDevices;

    private static SentegrityTrustFactorDatasets sInstance;
    private final Context context;

    private DNEStatusCode locationDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode pairedBTDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode scannedBTDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode gyroMotionDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode magneticHeadingDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode userMovementDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode accelMotionDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode netstatDataDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode cellularSignalDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode ambientLightDNEstatus = DNEStatusCode.OK;

    private List<MagneticObject> magneticHeading;
    private List<GyroRadsObject> gyroRads;
    private List<PitchRollObject> pitchRoll;
    private List<AccelRadsObject> accelRads;
    private List<ActiveConnection> netstatData;
    private List<ActiveRoute> routeData;
    private List<AppInfo> installedApps;
    private List<Integer> ambientLightData;

    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;
    private KeyguardManager keyguardManager;
    private String carrierConnectionSpeed;

    public SentegrityTrustFactorDatasets(Context context) {
        this.context = context;

        //reset data
        locationDNEStatus = DNEStatusCode.OK;
        pairedBTDNEStatus = DNEStatusCode.OK;
        scannedBTDNEStatus = DNEStatusCode.OK;
        gyroMotionDNEStatus = DNEStatusCode.OK;
        magneticHeadingDNEStatus = DNEStatusCode.OK;
        userMovementDNEStatus = DNEStatusCode.OK;
        accelMotionDNEStatus = DNEStatusCode.OK;
        netstatDataDNEStatus = DNEStatusCode.OK;
        cellularSignalDNEStatus = DNEStatusCode.OK;

        //testMethod();
        updateWifiManager();
        updateTelefonyManager();
        updateKeyguardManager();
        this.runTime = -1;

        magneticHeading = null;
        gyroRads = null;
        pitchRoll = null;
        accelRads = null;
        netstatData = null;
        routeData = null;
        ambientLightData = null;

        pairedBTDevices = null;
        scannedBTDevices = null;

        installedApps = null;

        batteryState = null;
        tethering = null;
        carrierConnectionName = null;
        airplaneMode = null;
        wifiEnabled = null;
        wifiInfo = null;
        location = null;
        brightness = null;
        cellularSignalRaw = null;
        gripMovement = null;
        userMovement = null;
        deviceOrientation = null;
        passcodeSet = null;

    }

    public static synchronized void initialize(Context context) {
        sInstance = new SentegrityTrustFactorDatasets(context);
    }

    public static SentegrityTrustFactorDatasets getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            if (sInstance.runTime < 0) {
                sInstance.runTime = System.currentTimeMillis();
            }
            return sInstance;
        }
    }

    public long getRunTime() {
        return runTime;
    }

    public static boolean validatePayload(List<Object> payload) {
        return !(payload == null || payload.size() < 1);
    }

    public String getTimeDateString(double blockSize, boolean withDayOfWeek) {
        if (hourOfDay < 0 || dayOfWeek < 0) {

            this.dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            this.hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minutes = Calendar.getInstance().get(Calendar.MINUTE);

            if (minutes > 30) {
                this.hourOfDay++;
            }

            if (this.hourOfDay == 0) {
                this.hourOfDay = 1;
            }

            if (blockSize == 0 && withDayOfWeek) {
                return "DAY_" + dayOfWeek;
            } else {
                int hourBlock = (int) Math.ceil((double) this.hourOfDay / blockSize);

                if (withDayOfWeek) {
                    return "DAY_" + dayOfWeek + "_" + "HOUR_" + hourBlock;
                } else {
                    return "HOUR_" + hourBlock;
                }
            }
        } else {
            if (blockSize == 0 && withDayOfWeek) {
                return "DAY_" + dayOfWeek;
            } else {
                int hourBlock = (int) Math.ceil((double) this.hourOfDay / blockSize);

                if (withDayOfWeek) {
                    return "DAY_" + dayOfWeek + "_" + "HOUR_" + hourBlock;
                } else {
                    return "HOUR_" + hourBlock;
                }
            }
        }

    }

    public String getBatteryState() {
        if (TextUtils.isEmpty(batteryState)) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null)
                return "unknown";

            switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                case BatteryManager.BATTERY_PLUGGED_USB:
                    return batteryState = "usbPlugged";
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    return batteryState = "wirelessPlugged";
                case BatteryManager.BATTERY_PLUGGED_AC:
                    return batteryState = "ACPlugged";
            }

            switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    return batteryState = "pluggedCharging";
                case BatteryManager.BATTERY_STATUS_FULL:
                    return batteryState = "pluggedFull";
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    return batteryState = "unplugged";
                default:
                    return "unknown";
            }
        } else {
            return batteryState;
        }
    }

    public float getBatteryPercent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null)
            return 0;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float) scale;
    }

    public List<AppInfo> getInstalledAppInfo() {
        if (installedApps == null || installedApps.size() == 0) {
            return installedApps = SentegrityTrustFactorDatasetApplication.getUserAppInfo(context);
        }
        return installedApps;
    }

    public Boolean isTethering() {
        if (tethering == null) {
            if (!updateWifiManager()) {
                return null;
            }
            try {
                Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
                return tethering = (Boolean) method.invoke(wifiManager);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        return tethering;
    }

    public String getCarrierConnectionName() {
        if (TextUtils.isEmpty(carrierConnectionName)) {
            if (!updateWifiManager()) {
                return null;
            }
            return carrierConnectionName = telephonyManager.getNetworkOperatorName();
        }
        return carrierConnectionName;
    }

    public Boolean isAirplaneMode() {
        if (airplaneMode == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return airplaneMode = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            } else {
                return airplaneMode = Settings.System.getInt(context.getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON, 0) != 0;
            }
        }
        return airplaneMode;
    }

    public Boolean isWifiEnabled() {
        if (wifiEnabled == null) {
            if (!updateWifiManager()) {
                return null;
            }
            return wifiEnabled = wifiManager.isWifiEnabled();
        }
        return wifiEnabled;
    }

    public WifiInfo getWifiInfo() {
        if (wifiInfo == null) {
            if (!updateWifiManager()) {
                return null;
            }
            return wifiInfo = wifiManager.getConnectionInfo();
        }
        return wifiInfo;
    }

    public String getDeviceOrientation() {
        if (deviceOrientation == null) {
            return deviceOrientation = SentegrityTrustFactorDatasetMotion.getOrientation(context);
        }
        return deviceOrientation;
    }

    public String getUserMovement() {
        if (userMovement == null) {
            return userMovement = SentegrityTrustFactorDatasetMotion.getUserMovement();
        }
        return userMovement;
    }

    public float getGripMovement() {
        if (gripMovement == null) {
            return gripMovement = SentegrityTrustFactorDatasetMotion.getGripMovement();
        }
        return gripMovement;
    }

    public String getCarrierConnectionSpeed() {
        if (carrierConnectionSpeed == null) {
            if (!updateTelefonyManager()) {
                return carrierConnectionSpeed = null;
            }

            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return carrierConnectionSpeed = "2G";

                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return carrierConnectionSpeed = "3G";

                case TelephonyManager.NETWORK_TYPE_LTE:
                    return carrierConnectionSpeed = "4G";

                default:
                    return "Unknown";
            }
        }
        return carrierConnectionSpeed;
    }

    public Integer getCellularSignalRaw() {
        if (cellularSignalRaw == null) {
            if (getCellularSignalDNEStatus() == DNEStatusCode.EXPIRED) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 100;

            while ((currentTime - startTime) < waitTime) {
                if (cellularSignalRaw != null)
                    return cellularSignalRaw;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }

                currentTime = System.currentTimeMillis();
            }

            setCellularSignalDNEStatus(DNEStatusCode.EXPIRED);
            return cellularSignalRaw;
        }
        return cellularSignalRaw;
    }

    public void setMagneticHeading(List<MagneticObject> magneticHeading) {
        this.magneticHeading = magneticHeading;
    }

    public void setGyroRads(List<GyroRadsObject> gyroRads) {
        this.gyroRads = gyroRads;
    }

    public void setPitchRoll(List<PitchRollObject> pitchRoll) {
        this.pitchRoll = pitchRoll;
    }

    public void setAccelRads(List<AccelRadsObject> accelRads) {
        this.accelRads = accelRads;
    }

    public List<GyroRadsObject> getGyroRads() {
        //TODO: what? doesn't make sense, we're using accelerometer and magnetometer for pitch and roll. check again
        if (gyroRads == null || gyroRads.size() == 0) {
            if (getGyroMotionDNEStatus() == DNEStatusCode.EXPIRED)
                return gyroRads;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 200;

            while ((currentTime - startTime) < waitTime) {
                if (gyroRads != null && gyroRads.size() > 0)
                    return gyroRads;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setGyroMotionDNEStatus(DNEStatusCode.NO_DATA);
            return gyroRads;
        }
        return gyroRads;
    }

    public List<PitchRollObject> getGyroPitchRoll() {
        if (pitchRoll == null || pitchRoll.size() == 0) {
            if (getGyroMotionDNEStatus() == DNEStatusCode.EXPIRED)
                return pitchRoll;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 250;

            while ((currentTime - startTime) < waitTime) {
                if (pitchRoll != null && pitchRoll.size() > 0)
                    return pitchRoll;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setGyroMotionDNEStatus(DNEStatusCode.NO_DATA);
            return pitchRoll;
        }
        return pitchRoll;
    }

    public List<AccelRadsObject> getAccelRads() {
        if (accelRads == null || accelRads.size() == 0) {
            if (getAccelMotionDNEStatus() == DNEStatusCode.EXPIRED)
                return accelRads;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 100;

            while ((currentTime - startTime) < waitTime) {
                if (accelRads != null && accelRads.size() > 0)
                    return accelRads;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setAccelMotionDNEStatus(DNEStatusCode.NO_DATA);
            return accelRads;
        }
        return accelRads;
    }

    public List<MagneticObject> getMagneticHeading() {
        if (magneticHeading == null || magneticHeading.size() == 0) {
            if (getAccelMotionDNEStatus() == DNEStatusCode.EXPIRED)
                return magneticHeading;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 500;

            while ((currentTime - startTime) < waitTime) {
                if (magneticHeading != null && magneticHeading.size() > 0)
                    return magneticHeading;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setAccelMotionDNEStatus(DNEStatusCode.NO_DATA);
            return magneticHeading;
        }
        return magneticHeading;
    }


    public DNEStatusCode getLocationDNEStatus() {
        return locationDNEStatus;
    }

    public DNEStatusCode getCellularSignalDNEStatus() {
        return cellularSignalDNEStatus;
    }

    public DNEStatusCode getPairedBTDNEStatus() {
        return pairedBTDNEStatus;
    }

    public DNEStatusCode getScannedBTDNEStatus() {
        return scannedBTDNEStatus;
    }

    public DNEStatusCode getUserMovementDNEStatus() {
        return userMovementDNEStatus;
    }

    public DNEStatusCode getGyroMotionDNEStatus() {
        return gyroMotionDNEStatus;
    }

    public DNEStatusCode getMagneticHeadingDNEStatus() {
        return magneticHeadingDNEStatus;
    }

    public DNEStatusCode getAccelMotionDNEStatus() {
        return accelMotionDNEStatus;
    }

    public DNEStatusCode getNetstatDataDNEStatus() {
        return netstatDataDNEStatus;
    }

    public DNEStatusCode getAmbientLightDNEstatus() {
        return ambientLightDNEstatus;
    }

    public void setPairedBTDNEStatus(DNEStatusCode pairedBTDNEStatus) {
        this.pairedBTDNEStatus = pairedBTDNEStatus;
    }

    public void setScannedBTDNEStatus(DNEStatusCode scannedBTDNEStatus) {
        this.scannedBTDNEStatus = scannedBTDNEStatus;
    }

    public void setLocationDNEStatus(DNEStatusCode locationDNEStatus) {
        this.locationDNEStatus = locationDNEStatus;
    }

    public void setGyroMotionDNEStatus(DNEStatusCode gyroMotionDNEStatus) {
        this.gyroMotionDNEStatus = gyroMotionDNEStatus;
    }

    public void setMagneticHeadingDNEStatus(DNEStatusCode magneticHeadingDNEStatus) {
        this.magneticHeadingDNEStatus = magneticHeadingDNEStatus;
    }

    public void setUserMovementDNEStatus(DNEStatusCode userMovementDNEStatus) {
        this.userMovementDNEStatus = userMovementDNEStatus;
    }

    public void setAccelMotionDNEStatus(DNEStatusCode accelMotionDNEStatus) {
        this.accelMotionDNEStatus = accelMotionDNEStatus;
    }

    public void setNetstatDataDNEStatus(DNEStatusCode netstatDataDNEStatus) {
        this.netstatDataDNEStatus = netstatDataDNEStatus;
    }

    public void setCellularSignalDNEStatus(DNEStatusCode cellularSignalDNEStatus) {
        this.cellularSignalDNEStatus = cellularSignalDNEStatus;
    }

    public void setAmbientLightDNEstatus(DNEStatusCode ambientLightDNEstatus) {
        this.ambientLightDNEstatus = ambientLightDNEstatus;
    }

    public void setScannedBTDevices(Set<String> scannedBTDevices) {
        this.scannedBTDevices = scannedBTDevices;
    }

    public void setPairedBTDevices(Set<String> pairedBTDevices) {
        this.pairedBTDevices = pairedBTDevices;
    }

    public void setNetstatData(List<ActiveConnection> netstatData) {
        this.netstatData = netstatData;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCellularSignalRaw(Integer cellularSignalRaw) {
        this.cellularSignalRaw = cellularSignalRaw;
    }

    public void setAmbientLight(List<Integer> ambientLightData){
        this.ambientLightData = ambientLightData;
    }

    public Set<String> getPairedBTDevices() {
        if (pairedBTDevices == null || pairedBTDevices.size() == 0) {
            if (getPairedBTDNEStatus() == DNEStatusCode.EXPIRED)
                return pairedBTDevices;

            //TODO: this waiting period should probably be removed
            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 50;

            while ((currentTime - startTime) < waitTime) {
                if (pairedBTDevices != null && pairedBTDevices.size() > 0)
                    return pairedBTDevices;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setPairedBTDNEStatus(DNEStatusCode.NO_DATA);
            return pairedBTDevices;
        }
        return pairedBTDevices;
    }

    public Set<String> getScannedBTDevices() {
        if (scannedBTDevices == null || scannedBTDevices.size() == 0) {
            if (getScannedBTDNEStatus() == DNEStatusCode.EXPIRED)
                return scannedBTDevices;

            //TODO: this waiting period should probably be removed
            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 50;

            while ((currentTime - startTime) < waitTime) {
                if (scannedBTDevices != null && scannedBTDevices.size() > 0)
                    return scannedBTDevices;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setScannedBTDNEStatus(DNEStatusCode.NO_DATA);
            return scannedBTDevices;
        }
        return scannedBTDevices;
    }

    public List<ActiveConnection> getNetstatData() {
        if (netstatData == null || netstatData.size() == 0) {
            if (getNetstatDataDNEStatus() == DNEStatusCode.EXPIRED)
                return netstatData;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 250;

            while ((currentTime - startTime) < waitTime) {
                if (netstatData != null && netstatData.size() > 0)
                    return netstatData;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setNetstatDataDNEStatus(DNEStatusCode.NO_DATA);
        }
        return netstatData;
    }

    public Long getDataXferInfo() {
        long trafficBytesSent = TrafficStats.getTotalTxBytes();
        if (trafficBytesSent == TrafficStats.UNSUPPORTED) {
            return null;
        }
        return trafficBytesSent;
    }

    public List<ActiveRoute> getRouteInfo() {
        if (routeData == null) {
            try {
                return routeData = SentegrityTrustFactorDatasetRoute.getAllRoutes();
            } catch (IOException e) {
                return null;
            }
        } else {
            return routeData;
        }
    }

    public Location getLocationInfo() {
        if (location == null) {
            if (getLocationDNEStatus() == DNEStatusCode.EXPIRED) {
                return location;
            }
            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 250;

            while ((currentTime - startTime) < waitTime) {
                if (location != null)
                    return location;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }

                currentTime = System.currentTimeMillis();
            }

            setLocationDNEStatus(DNEStatusCode.EXPIRED);
            return location;
        }
        return location;
    }

    public Boolean isPasscodeSet() {
        if (passcodeSet == null) {
            //version 23+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!updateKeyguardManager())
                    return null;
                return keyguardManager.isDeviceSecure();
            }

            int exceptions = 0;
            //should work on all previous versions
            try {
                Class<?> lockUtilsClass = Class.forName("com.android.internal.widget.LockPatternUtils");
                Object utils = lockUtilsClass.getConstructor(Context.class).newInstance(context);
                //on 6.0+ this method requires user id parameter
                Method method = lockUtilsClass.getMethod("isSecure");
                Boolean isSecure = (Boolean) method.invoke(utils);
                if (isSecure != null && isSecure)
                    return passcodeSet = true;
            } catch (Exception e) {
                exceptions++;
            }

            //just to be sure? maybe returns something
            try {
                ContentResolver cr = context.getContentResolver();
                int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
                if (lockPatternEnable == 1)
                    return passcodeSet = true;
            } catch (Exception e) {
                exceptions++;
            }

            if (exceptions >= 2)
                return passcodeSet = null;
            return passcodeSet = false;
        }
        return passcodeSet;
    }

    public ArrayList<String> getSSIDList() {
        try {
            AssetManager mg = context.getResources().getAssets();

            ArrayList<String> list = new ArrayList<>();
            String line;
            InputStream is = mg.open(SentegrityConstants.DEFAULT_SSID_LIST_FILE_NAME);
            InputStreamReader inputReader = new InputStreamReader(is);
            BufferedReader buffReader = new BufferedReader(inputReader);
            /*int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            list = new String(buffer, "UTF-8");*/

            while ((line = buffReader.readLine()) != null) {
                list.add(line);
            }
            return list;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> getOUIList() {
        try {
            AssetManager mg = context.getResources().getAssets();

            List<String> list = new ArrayList<>();
            String line;
            InputStream is = mg.open(SentegrityConstants.OUI_LIST_FILE_NAME);
            InputStreamReader inputReader = new InputStreamReader(is);
            BufferedReader buffReader = new BufferedReader(inputReader);

            while ((line = buffReader.readLine()) != null) {
                list.add(line);
            }
            return list;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> getHotspotList() {
        try {
            AssetManager mg = context.getResources().getAssets();

            List<String> list = new ArrayList<>();
            String line;
            InputStream is = mg.open(SentegrityConstants.HOTSPOT_SSID_LIST_FILE_NAME);
            InputStreamReader inputReader = new InputStreamReader(is);
            BufferedReader buffReader = new BufferedReader(inputReader);

            while ((line = buffReader.readLine()) != null) {
                list.add(line);
            }
            return list;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Integer> getAmbientLightData(){
        if(ambientLightData == null){
            if (getAmbientLightDNEstatus() == DNEStatusCode.EXPIRED) {
                return ambientLightData;
            }

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 50;

            while ((currentTime - startTime) < waitTime) {
                if (ambientLightData != null)
                    return ambientLightData;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }

                currentTime = System.currentTimeMillis();
            }

            setAmbientLightDNEstatus(DNEStatusCode.EXPIRED);
            return ambientLightData;
        }
        return ambientLightData;
    }

    public Float getSystemBrightness() {
        if (brightness == null) {
            //TODO: this is the way to go. android internally uses light sensor. move this to activity dispatcher and we're good to go!
//            SensorManager mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
//            Sensor mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
//            mSensorManager.registerListener(new SensorEventListener() {
//                @Override
//                public void onSensorChanged(SensorEvent event) {
//                    if( event.sensor.getType() == Sensor.TYPE_LIGHT) {
//                    }
//                }
//
//                @Override
//                public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//                }
//            }, mLight, 1000);
            float current = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);

            //if we're in auto mode, then get adjusted brightness level
            //screen_auto_brightness_adj has values [-1.0, 1.0]
            //this returns only user adjusted value
//            if(Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
//                float adjusted = Settings.System.getFloat(context.getContentResolver(), "screen_auto_brightness_adj", -99);
//                if (adjusted != -99)
//                    current = (adjusted + 1.0f) / 2.0f;
//            }

            if (current == -1)
                return brightness = null;
            return brightness = current / 255.0f;
        }
        return brightness;
    }

    private boolean updateWifiManager() {
        if (wifiManager != null) return true;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null;
    }

    private boolean updateTelefonyManager() {
        if (telephonyManager != null) return true;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null;
    }

    private boolean updateKeyguardManager() {
        if (keyguardManager != null) return true;
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager != null;
    }

    /**
     * Call on reloading login
     */
    public static void destroy() {
        sInstance = null;
    }

    GoogleApiClient mCredentialsApiClient;

    public void testMethod() {
        mCredentialsApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        CredentialRequest mCredentialRequest = new CredentialRequest.Builder()
                                .setPasswordLoginSupported(true)
                                .build();

                        Auth.CredentialsApi.request(mCredentialsApiClient, mCredentialRequest).setResultCallback(new ResultCallback<CredentialRequestResult>() {
                            @Override
                            public void onResult(CredentialRequestResult credentialRequestResult) {
                                // check if smart lock is enabled
                                // if -> credentialRequestResult.getStatus().getStatusCode() == CommonStatusCodes.CANCELED;
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addApi(Auth.CREDENTIALS_API)
                .build();
        mCredentialsApiClient.connect();
    }
}
