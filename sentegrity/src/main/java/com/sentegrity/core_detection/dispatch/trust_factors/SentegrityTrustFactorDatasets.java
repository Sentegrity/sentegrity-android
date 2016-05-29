package com.sentegrity.core_detection.dispatch.trust_factors;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.location.Location;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
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
import com.sentegrity.core_detection.dispatch.activity_dispatcher.SentegrityActivityDispatcher;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetMotion;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.MagneticObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.PitchRollObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.ActiveConnection;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.root.RootDetection;
import com.sentegrity.core_detection.utilities.Helpers;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.PkgInfo;
import com.trustlook.sdk.data.Region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;

/**
 * Storage for all trustfactors data.
 * First call to one method will calculate / obtain data, and later we will use that values.
 * To obtain instance we simply call {@link SentegrityTrustFactorDatasets#getInstance()}.
 *
 * @see SentegrityActivityDispatcher Sentegrity activity dispatcher
 */
public class SentegrityTrustFactorDatasets {

    private long runTime = -1;

    private int hourOfDay = -1;
    private int dayOfWeek = -1;
    private String batteryState = null;
    private Boolean tethering = null;
    private String carrierConnectionName = null;
    private Boolean airplaneMode = null;
    private Boolean wifiEnabled = null;
    private WifiInfo wifiInfo = null;
    private Location location = null;
    private Float brightness = null;
    private Integer cellularSignalRaw = null;
    private Float gripMovement = null;
    private String userMovement = null;
    private String deviceOrientation = null;
    private Boolean passcodeSet = null;
    private Boolean wifiUnencrypted = null;
    private Integer backupEnabled = null;
    private Boolean hasInternetConnection = null;
    private String carrierConnectionSpeed = null;
    private RootDetection rootDetection = null;
    private Boolean fromPlayStore = null;
    private Boolean onEmulator = null;
    private Boolean debuggable = null;
    private Boolean signatureOK = null;
    private Boolean notDisturbeMode = null;
    private Boolean onCall = null;
    private Boolean orientationLock = null;

    private Set<String> pairedBTDevices;
    private Set<String> scannedBTDevices;

    private static SentegrityTrustFactorDatasets sInstance;
    public final Context context;

    private DNEStatusCode locationDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode pairedBTDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode scannedBTDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode gyroMotionDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode magneticHeadingDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode userMovementDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode accelMotionDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode netstatDataDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode cellularSignalDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode ambientLightDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode rootDetectionDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode trustLookBadPkgListDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode trustLookBadURLListDNEStatus = DNEStatusCode.OK;

    private List<MagneticObject> magneticHeading;
    private List<GyroRadsObject> gyroRads;
    private List<PitchRollObject> pitchRoll;
    private List<AccelRadsObject> accelRads;
    private List<ActiveConnection> netstatData;
    private List<NetworkInterface> routeData;
    private List<ApplicationInfo> installedApps;
    private List<Integer> ambientLightData;
    private List<AppInfo> trustLookBadPkgList;
    private List<String> trustLookBadURLList;

    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;
    private KeyguardManager keyguardManager;
    private ConnectivityManager connectivityManager;
    private AudioManager audioManager;

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
        rootDetectionDNEStatus = DNEStatusCode.OK;
        trustLookBadPkgListDNEStatus = DNEStatusCode.OK;
        trustLookBadURLListDNEStatus = DNEStatusCode.OK;

        //testMethod();
        updateWifiManager();
        updateTelefonyManager();
        updateKeyguardManager();
        updateConnectivityManager();
        updateAudioManager();
        this.runTime = -1;

        magneticHeading = null;
        gyroRads = null;
        pitchRoll = null;
        accelRads = null;
        netstatData = null;
        routeData = null;
        ambientLightData = null;
        trustLookBadPkgList = null;
        trustLookBadURLList = null;

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
        wifiUnencrypted = null;
        backupEnabled = null;
        hasInternetConnection = null;
        rootDetection = null;
        notDisturbeMode = null;
        onCall = null;

    }

    public static synchronized void initialize(Context context) {
        sInstance = new SentegrityTrustFactorDatasets(context);
    }

    /**
     * If instance is not already available, method throws illegal state exception. Call {@link com.sentegrity.core_detection.CoreDetection#initialize(Context)}.
     * First time we run it sets run time - this way all trustfactors will have same run time.
     *
     * @return current instance of the sentegrity trust factor dataset
     */
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

    public DNEStatusCode getAmbientLightDNEStatus() {
        return ambientLightDNEStatus;
    }

    public DNEStatusCode getRootDetectionDNEStatus() {
        return rootDetectionDNEStatus;
    }

    public DNEStatusCode getTrustLookBadPkgListDNEStatus() {
        return trustLookBadPkgListDNEStatus;
    }

    public DNEStatusCode getTrustLookBadURLListDNEStatus(){
        return trustLookBadURLListDNEStatus;
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

    public void setAmbientLightDNEStatus(DNEStatusCode ambientLightDNEStatus) {
        this.ambientLightDNEStatus = ambientLightDNEStatus;
    }

    public void setRootDetectionDNEStatus(DNEStatusCode rootDetectionDNEStatus) {
        this.rootDetectionDNEStatus = rootDetectionDNEStatus;
    }

    public void setTrustLookBadPkgListDNEStatus(DNEStatusCode trustLookBadPkgListDNEStatus){
        this.trustLookBadPkgListDNEStatus = trustLookBadPkgListDNEStatus;
    }

    public void setTrustLookBadURLListDNEStatus(DNEStatusCode trustLookBadURLListDNEStatus){
        this.trustLookBadURLListDNEStatus = trustLookBadURLListDNEStatus;
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

    public void setAmbientLight(List<Integer> ambientLightData) {
        this.ambientLightData = ambientLightData;
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

    public void setTrustLookBadPkgList(List<AppInfo> trustLookBadPkgList){
        this.trustLookBadPkgList = trustLookBadPkgList;
    }

    public void setTrustLookBadURLList(List<String> trustLookBadURLList){
        this.trustLookBadURLList = trustLookBadURLList;
    }

    public void setRootDetection(RootDetection rootDetection) {
        this.rootDetection = rootDetection;
    }

    /**
     * Validates trustfactors payload
     *
     * @param payload trustfactor rule payload
     * @return {@code false} if payload is null or empty, otherwise {@code true}
     */
    public static boolean validatePayload(List<Object> payload) {
        return !(payload == null || payload.size() < 1);
    }

    /**
     * Calculates current hour and day from calendar instance.
     *
     * @param blockSize     hours block size for aligning current hours value --> if zero, return only day
     * @param withDayOfWeek if day should be returned or not
     * @return string with aligned hours and day (numerical value)
     */
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

    /**
     * Gets current battery state based on internal ACTION_BATTERY_CHANGED filter.
     * First we'll check if battery is plugged and return state, otherwise we'll check status.
     *
     * @return string representing current battery state
     */
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

    /**
     * Gets current battery percentage based on internal ACTION_BATTERY_CHANGED filter.
     *
     * @return float representing current battery percentage (0.0 -> 1.0), -1 if not available
     */
    public float getBatteryPercent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null)
            return 0;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float) scale;
    }

    /**
     * Gets all installed apps from package manager.
     *
     * @return list of installed application info
     */
    public List<ApplicationInfo> getInstalledAppInfo() {
        if (installedApps == null || installedApps.size() == 0) {
            PackageManager packageManager = context.getPackageManager();
            return installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        }
        return installedApps;
    }

    /**
     * Checks if device is in tethering mode at the moment.
     * Uses reflection for current status.
     *
     * @return {@code true/false} if in tethering mode, or {@code null} if not available
     */
    //TODO: uses reflection
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

    /**
     * Checks wifi encryption status based on wifi manager configured networks and current bssid.
     * Checks for WPA_PSK, WPA_EAP, IEEE8021X or WEP.
     *
     * @return {@code true/false} if (un)encrypted, or {@code null} if not available
     */
    public Boolean isWifiUnencrypted() {
        if (wifiUnencrypted == null) {
            if (!updateWifiManager()) {
                return null;
            }
            final WifiInfo wifiInfo = getWifiInfo();

            for (WifiConfiguration configuration : wifiManager.getConfiguredNetworks()) {
                if (wifiInfo.getBSSID().equals(configuration.BSSID)) {
                    boolean unencrypted = false;
                    if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                        unencrypted = true;
                    } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)) {
                        unencrypted = true;
                    } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                        unencrypted = true;
                    } else if (configuration.wepKeys.length > 0 && configuration.wepKeys[0] != null) {
                        unencrypted = true;
                    }
                    return wifiUnencrypted = unencrypted;
                }
            }
            return null;
        }
        return wifiUnencrypted;
    }

    /**
     * Checks if device has working internet connection.
     *
     * @return {@code true/false} if there is active internet connections, or {@code null} if not available
     */
    public Boolean hasInternetConnection() {
        if (hasInternetConnection == null) {
            if (!updateConnectivityManager()) {
                return null;
            }
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return hasInternetConnection = netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return hasInternetConnection;
    }

    /**
     * Gets current carrier name.
     *
     * @return string representing current operator
     */
    public String getCarrierConnectionName() {
        if (TextUtils.isEmpty(carrierConnectionName)) {
            if (!updateWifiManager()) {
                return null;
            }
            return carrierConnectionName = telephonyManager.getNetworkOperatorName();
        }
        return carrierConnectionName;
    }

    /**
     * Checks if device is in airplane mode at the moment.
     * Settings.Global for newer versions, and Setting.System for API < 17
     *
     * @return {@code true/false} if in airplane mode
     */
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

    /**
     * Checks if wifi is currently enabled on device.
     *
     * @return {@code true/false} if wifi is enabled, or {@code null} if not available
     */
    public Boolean isWifiEnabled() {
        if (wifiEnabled == null) {
            if (!updateWifiManager()) {
                return null;
            }
            return wifiEnabled = wifiManager.isWifiEnabled();
        }
        return wifiEnabled;
    }

    /**
     * Collects current wifi info from wifi manager.
     *
     * @return wifi info, or {@code null} if not available
     */
    public WifiInfo getWifiInfo() {
        if (wifiInfo == null) {
            if (!updateWifiManager()) {
                return null;
            }
            return wifiInfo = wifiManager.getConnectionInfo();
        }
        return wifiInfo;
    }

    /**
     * @return string representing current device orientation
     */
    public String getDeviceOrientation() {
        if (deviceOrientation == null) {
            return deviceOrientation = SentegrityTrustFactorDatasetMotion.getOrientation(context);
        }
        return deviceOrientation;
    }

    /**
     * @return string representing current user movement
     */
    public String getUserMovement() {
        if (userMovement == null) {
            return userMovement = SentegrityTrustFactorDatasetMotion.getUserMovement(context);
        }
        return userMovement;
    }

    /**
     * @return float representing current grip movement
     */
    public float getGripMovement() {
        if (gripMovement == null) {
            return gripMovement = SentegrityTrustFactorDatasetMotion.getGripMovement();
        }
        return gripMovement;
    }

    /**
     * Groups connection speed into 2G, 3G, 4G.
     *
     * @return string representing current connection speed, or "unknown" if not applicable
     */
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

    /**
     * Waits for cellular signal data populated from {@link SentegrityActivityDispatcher#startCellularSignal()}.
     *
     * @return list of cellular signal, {@code null} if expired or not available
     */
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

    public String getLastApplication() {
        //NOT WORKING ON ANDROID API >= 21
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++) {
        }
        return null;
    }

    /**
     * Check if device is currently in orientation locked mode.
     *
     * @return {@code true} current state is offhook, or {@code false} if ringing or idle
     */
    public Boolean hasOrientationLock() {
        if (orientationLock == null) {
            int lock = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1);
            if (lock == -1)
                return orientationLock = true;
            else if (lock == 0)
                return orientationLock = false;
        }
        return orientationLock;
    }

    /**
     * Check if user is currently on call. Doesn't take incoming (ringing) call in account.
     *
     * @return {@code true} current state is offhook, or {@code false} if ringing or idle
     */
    public Boolean isOnCall() {
        if (onCall == null) {
            if (!updateTelefonyManager()) {
                return null;
            }
            switch (telephonyManager.getCallState()) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    return onCall = true;
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_IDLE:
                    return onCall = false;
            }
            return null;
        }
        return onCall;
    }

    /**
     * Check if device is currently in do not disturb mode.
     *
     * @return {@code true} if device is set on silent or vibrate, or {@code false} if in normal mode
     */
    public Boolean isNotDisturbMode() {
        if (notDisturbeMode == null) {
            if (!updateAudioManager()) {
                return null;
            }
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                case AudioManager.RINGER_MODE_VIBRATE:
                    return notDisturbeMode = true;
                case AudioManager.RINGER_MODE_NORMAL:
                    return notDisturbeMode = false;
            }
            return null;
        }
        return notDisturbeMode;
    }

    /**
     * Waits for gyro rads data populated from {@link SentegrityActivityDispatcher#startMotion()}.
     *
     * @return list of gyro rads data, {@code null} if expired or not available
     */
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

    /**
     * Waits for gyro pitch roll data populated from {@link SentegrityActivityDispatcher#startMotion()}.
     *
     * @return list of gyro pitch roll data, {@code null} if expired or not available
     */
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

    /**
     * Waits for accelerometer rads data populated from {@link SentegrityActivityDispatcher#startMotion()}.
     *
     * @return list of accelerometer rads data, {@code null} if expired or not available
     */
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

    /**
     * Waits for magnetic heading data populated from {@link SentegrityActivityDispatcher#startMotion()}.
     *
     * @return list of magnetic heading data, {@code null} if expired or not available
     */
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

    /**
     * Waits for paired bluetooth devices populated from {@link SentegrityActivityDispatcher#startBluetooth()}.
     *
     * @return list of paired bluetooth devices, {@code null} if expired or not available
     */
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

    /**
     * Waits for found bluetooth devices populated from {@link SentegrityActivityDispatcher#startBluetooth()}.
     *
     * @return list of found bluetooth devices, {@code null} if expired or not available
     */
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

    /**
     * Waits for netstat data populated from {@link SentegrityActivityDispatcher#startNetstat()}.
     *
     * @return list of netstat data, {@code null} if expired or not available
     */
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

    /**
     * Gets total transmitted bytes since last boot.
     *
     * @return total transmitted data or @UNSUPPORTED (for some devices with API < 17)
     */
    public Long getDataXferInfo() {
        long trafficBytesSent = TrafficStats.getTotalTxBytes();
        if (trafficBytesSent == TrafficStats.UNSUPPORTED) {
            return null;
        }
        return trafficBytesSent;
    }

    /**
     * Gets all network interfaces.
     *
     * @return list of currently active network interfaces, or {@code null} if not available
     */
    public List<NetworkInterface> getRouteInfo() {
        if (routeData == null) {
            try {
                return routeData = Collections.list(NetworkInterface.getNetworkInterfaces());
            } catch (IOException e) {
                return null;
            }
        } else {
            return routeData;
        }
    }

    /**
     * Waits for location data populated from {@link SentegrityActivityDispatcher#startLocation()}.
     *
     * @return list of location data, {@code null} if expired or not available
     */
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

    /**
     * Check whether device has some passcode set or not.
     * For Android API >= 23 there is class/method for this, but for previous versions we are using
     * couple of different techniques all using reflection.
     *
     * @return {@code true/false} if there is some passcode set, {@code null} if not available
     */
    //TODO: uses reflection
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

    /**
     * Checks whether we have backup enabled.
     *
     * @return {@code true/false} if there is backup enabled
     */
    public Integer isBackupEnabled() {
        if (backupEnabled == null) {
            //only tested for nexus 5 and nexus 5x, android version 6.0.1
            //TODO: "backup_enabled" is hidden secure setting, this should be checked
            return backupEnabled = Settings.Secure.getInt(context.getContentResolver(), "backup_enabled", -1);
        }
        return backupEnabled;
    }

    /**
     * Collects malicious application list from internal application file.
     *
     * @return arraylist of malicious apps, or {@code null} if not available
     */
    public ArrayList<String> getMaliciousAppsList() {
        try {
            AssetManager mg = context.getResources().getAssets();

            ArrayList<String> list = new ArrayList<>();
            String line;
            InputStream is = mg.open(SentegrityConstants.MALICIOUS_APP_LIST_FILE_NAME);
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

    /**
     * Collects high risk application list from internal application file.
     *
     * @return arraylist of high risk apps, or {@code null} if not available
     */
    public ArrayList<String> getHighRiskAppsList() {
        try {
            AssetManager mg = context.getResources().getAssets();

            ArrayList<String> list = new ArrayList<>();
            String line;
            InputStream is = mg.open(SentegrityConstants.HIGH_RISK_APP_LIST_FILE_NAME);
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

    /**
     * Collects SSID list from internal application file.
     *
     * @return arraylist of SSIDs, or {@code null} if not available
     */
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

    /**
     * Collects OUI list from internal application file.
     *
     * @return list of OUIs, or {@code null} if not available
     */
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

    /**
     * Collects hotspot names list from internal application file.
     *
     * @return list of hotspot names, or {@code null} if not available
     */
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

    /**
     * Waits for ambient light data.
     *
     * @return list of ambient light data, {@code null} if expired or not available
     */
    public List<Integer> getAmbientLightData() {
        if (ambientLightData == null) {
            if (getAmbientLightDNEStatus() == DNEStatusCode.EXPIRED) {
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

            setAmbientLightDNEStatus(DNEStatusCode.EXPIRED);
            return ambientLightData;
        }
        return ambientLightData;
    }

    /**
     * @return current root detection object that is populated from {@link SentegrityActivityDispatcher#startRootCheck()}
     */
    public RootDetection getRootDetection() {
        if (rootDetection == null) {
            if (getRootDetectionDNEStatus() == DNEStatusCode.EXPIRED) {
                return rootDetection;
            }

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 100;

            while ((currentTime - startTime) < waitTime) {
                if (rootDetection != null)
                    return rootDetection;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }

                currentTime = System.currentTimeMillis();
            }

            setRootDetectionDNEStatus(DNEStatusCode.EXPIRED);
            return rootDetection;
        }
        return rootDetection;
    }

    /**
     * Gets current application signature, hashes it using MD5, and compares with expected signature {@link SentegrityConstants#APK_SIGNATURE}.
     * We need to check all the available signatures since there could be multiple ones (fake + real).
     * Only return {@code true} if all signatures are ok (in practice that should be only one REAL signature)
     *
     * @return {@code true/false} if app signature is ok, or {@code null} if there were some problems getting and hashing signature
     */
    public Boolean isAppSignatureOk() {
        if (signatureOK == null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
                boolean signatureOk = false;
                for (Signature signature : packageInfo.signatures) {
                    MessageDigest mdMd5 = MessageDigest.getInstance("MD5");

                    mdMd5.update(signature.toCharsString().getBytes());
                    byte byteData[] = mdMd5.digest();

                    StringBuilder hexString = new StringBuilder();
                    for (byte aByteData : byteData) {
                        String hex = Integer.toHexString(0xff & aByteData);
                        if (hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                    }

                    if (SentegrityConstants.APK_SIGNATURE.equals(hexString.toString())) {
                        signatureOk = true;
                    } else {
                        return signatureOK = false;
                    }
                }
                return signatureOK = signatureOk;
            } catch (Exception e) {
                return null;
            }
        }
        return signatureOK;
    }

    /**
     * Checks if this app is installed from play store (otherwise it could be fake).
     *
     * @return {@code true/false} if app is installed from play store
     */
    public Boolean isFromPlayStore() {
        if (fromPlayStore == null) {
            final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
            return fromPlayStore = (installer != null && installer.startsWith("com.android.vending"));
        }
        return fromPlayStore;
    }

    /**
     * Checks if app is running on the emulator.
     * This shouldn't be the case - it might indicate that someone is using app for "fishy" stuff.
     *
     * @return {@code true/false} if app is running on emulator
     */
    public Boolean isOnEmulator() {
        if (onEmulator == null) {
            try {

                return onEmulator = (Helpers.getSystemProperty("ro.hardware").contains("goldfish")
                        || Helpers.getSystemProperty("ro.kernel.qemu").length() > 0
                        || Helpers.getSystemProperty("ro.product.model").equals("sdk"));

            } catch (Exception e) {

            }

            return onEmulator = (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk".equals(Build.PRODUCT));
        }
        return onEmulator;
    }

    /**
     * Checks whether current running app is debuggable. This shouldn't happen once it's in production.
     *
     * @return {@code true/false} if app is debuggable
     */
    public Boolean checkDebuggable() {
        if (debuggable == null) {
            return debuggable = ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
        }
        return debuggable;
    }

    /**
     * Gets system brightness.
     * It only works when brightness is on Manual mode.
     *
     * @return float representing current brightness (0.0 - 1.0)
     */
    @Deprecated
    public Float getSystemBrightness() {
        if (brightness == null) {
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

    /**
     * Private method for updating wifi manager.
     *
     * @return {@code false} if there is no {@link Context#WIFI_SERVICE WIFI_SERVICE}, {@code true} if everything went fine
     */
    private boolean updateWifiManager() {
        if (wifiManager != null) return true;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null;
    }

    /**
     * Private method for updating telephony manager.
     *
     * @return {@code false} if there is no {@link Context#TELEPHONY_SERVICE TELEPHONY_SERVICE}, {@code true} if everything went fine
     */
    private boolean updateTelefonyManager() {
        if (telephonyManager != null) return true;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null;
    }

    /**
     * Private method for updating keyguard manager.
     *
     * @return {@code false} if there is no {@link Context#KEYGUARD_SERVICE KEYGUARD_SERVICE}, {@code true} if everything went fine
     */
    private boolean updateKeyguardManager() {
        if (keyguardManager != null) return true;
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager != null;
    }

    /**
     * Private method for updating connectivity manager.
     *
     * @return {@code false} if there is no {@link Context#CONNECTIVITY_SERVICE CONNECTIVITY_SERVICE}, {@code true} if everything went fine
     */
    private boolean updateConnectivityManager() {
        if (connectivityManager != null) return true;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager != null;
    }

    /**
     * Private method for updating audio manager.
     *
     * @return {@code false} if there is no {@link Context#AUDIO_SERVICE AUDIO_SERVICE}, {@code true} if everything went fine
     */
    private boolean updateAudioManager() {
        if (audioManager != null) return true;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager != null;
    }

    /**
     * Waits for TrustLook bad package AppInfo list
     * This list can be empty
     *
     * @return list of App info data, {@code null} if expired or not available
     */
    public List<AppInfo> getTrustLookBadPkgList() {
        if (trustLookBadPkgList == null) {
            if (getTrustLookBadPkgListDNEStatus() == DNEStatusCode.EXPIRED)
                return trustLookBadPkgList;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 2000;

            while ((currentTime - startTime) < waitTime) {
                if (trustLookBadPkgList != null)
                    return trustLookBadPkgList;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setTrustLookBadPkgListDNEStatus(DNEStatusCode.NO_DATA);
            return trustLookBadPkgList;
        }
        return trustLookBadPkgList;
    }

    /**
     * Waits for TrustLook URL list
     * This list can be empty
     *
     * @return list of bad URL data, {@code null} if expired or not available
     */
    public List<String> getTrustLookBadURLList() {
        if (trustLookBadURLList == null) {
            if (getTrustLookBadURLListDNEStatus() == DNEStatusCode.EXPIRED)
                return trustLookBadURLList;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 200;

            while ((currentTime - startTime) < waitTime) {
                if (trustLookBadURLList != null)
                    return trustLookBadURLList;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setTrustLookBadURLListDNEStatus(DNEStatusCode.NO_DATA);
            return trustLookBadURLList;
        }
        return trustLookBadURLList;
    }

    public SharedPreferences getSharedPrefs(){
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
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
