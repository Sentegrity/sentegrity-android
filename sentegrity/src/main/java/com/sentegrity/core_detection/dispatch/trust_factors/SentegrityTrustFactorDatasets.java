package com.sentegrity.core_detection.dispatch.trust_factors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.sentegrity.core_detection.constants.SentegrityConstants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    private static SentegrityTrustFactorDatasets sInstance;
    private final Context context;

    private WifiManager wifiManager;

    public SentegrityTrustFactorDatasets(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //reset runtime on initialization
        this.runTime = -1;
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
        if (hourOfDay < 0) {

            this.dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            this.hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minutes = Calendar.getInstance().get(Calendar.MINUTE);

            if (minutes > 30) {
                this.hourOfDay++;
            }

            if (this.hourOfDay == 0) {
                this.hourOfDay = 1;
            }
        }

        int hourBlock = (int) Math.ceil((double) this.hourOfDay / blockSize);

        if (withDayOfWeek) {
            return "DAY_" + dayOfWeek + "_" + "HOUR_" + hourBlock;
        } else {
            return "HOUR_" + hourBlock;
        }
    }

    public String getBatteryState() {
        if (TextUtils.isEmpty(batteryState)) {
            //TODO: make it more bulletproof / what if device doesn't return plugged state
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null)
                return "unknown";

            int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean isUSB = plugged == BatteryManager.BATTERY_PLUGGED_USB;

            if (isUSB) {
                return batteryState = "usbPlugged";
            }

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
            boolean isFull = status == BatteryManager.BATTERY_STATUS_FULL;
            boolean discharging = status == BatteryManager.BATTERY_STATUS_DISCHARGING;
            if (isCharging) {
                return batteryState = "pluggedCharging";
            } else if (isFull) {
                return batteryState = "pluggedFull";
            } else if (discharging) {
                return batteryState = "unplugged";
            } else {
                return batteryState = "unknown";
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

    public Boolean isTethering() {
        if (tethering == null) {
            //TODO: recheck method, make it more bulletproof --> add to separate class / WifiInfo.java
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
            //TODO: recheck method, make it more bulletproof --> add to separate class / CellInfo.java
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return carrierConnectionName = manager.getNetworkOperatorName();
        }
        return carrierConnectionName;
    }

    public Boolean isAirplaneMode() {
        if (airplaneMode == null) {
            //TODO: recheck method, make it more bulletproof --> add to separate class / CellInfo.java
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return airplaneMode = Settings.System.getInt(context.getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON, 0) != 0;
            } else {
                return airplaneMode = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            }
        }
        return airplaneMode;
    }

    public Boolean isWifiEnabled() {
        if (wifiEnabled == null) {
            //TODO: recheck method, make it more bulletproof --> add to separate class / WifiInfo.java
            if (!updateWifiManager()) {
                return null;
            }
            return wifiEnabled = wifiManager.isWifiEnabled();
        }
        return wifiEnabled;
    }

    public WifiInfo getWifiInfo() {
        if (wifiInfo == null) {
            //TODO: recheck method, make it more bulletproof --> add to separate class / WifiInfo.java
            if (!updateWifiManager()) {
                return null;
            }
            return wifiInfo = wifiManager.getConnectionInfo();
        }
        return wifiInfo;
    }

    public List<String> getSSIDList() {
        try {
            AssetManager mg = context.getResources().getAssets();

            List<String> list = new ArrayList<>();
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

    private boolean updateWifiManager() {
        if (wifiManager != null) return true;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null;
    }


    /**
     * Call on reloading login
     */
    public static void destroy() {
        sInstance = null;
    }
}
