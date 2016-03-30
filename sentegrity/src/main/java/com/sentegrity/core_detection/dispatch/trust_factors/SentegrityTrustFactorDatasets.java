package com.sentegrity.core_detection.dispatch.trust_factors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private Boolean airplaneMode;

    private static SentegrityTrustFactorDatasets sInstance;
    private final Context context;

    public SentegrityTrustFactorDatasets(Context context) {
        this.context = context;
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
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null)
                return "unknown";

            //TODO: check other states // implement real USB connection status
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
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            try {
                //TODO: recheck method, make it more bulletproof --> add to separate class / WifiInfo.java
                Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
                return tethering = (Boolean) method.invoke(wifiManager);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
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


    /**
     * Call on reloading login
     */
    public static void destroy() {
        sInstance = null;
    }
}
