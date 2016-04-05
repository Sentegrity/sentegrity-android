package com.sentegrity.core_detection.dispatch.trust_factors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.PitchRollObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    private Set<String> connectedClassicBTDevices;
    private Set<String> discoveredBLEDevices;

    private static SentegrityTrustFactorDatasets sInstance;
    private final Context context;

    private DNEStatusCode connectedClassicDNEStatus;
    private DNEStatusCode discoveredBLEDNEStatus;

    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;

    public SentegrityTrustFactorDatasets(Context context) {
        this.context = context;
        //reset data
        updateWifiManager();
        updateTelefonyManager();
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

            switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    return batteryState = "usbPlugged";
                case BatteryManager.BATTERY_STATUS_FULL:
                    return batteryState = "wirelessPlugged";
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
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
        //TODO: return real orientation
        // fake random orientation for testing purposes
        int i = new Random().nextInt(8);
        switch (i) {
            case 0:
                return "Portrait";
            case 1:
                return "Landscape_Right";
            case 2:
                return "Landscape_Left";
            case 3:
                return "Portrait_Upside_Down";
            case 4:
                return "Face_Up";
            case 5:
                return "Face_Down";
            case 6:
                return "unknown";
            default:
                return "error";
        }
    }

    public String getUserMovement() {
        //TODO: return real movement
        // fake random movement for testing purposes
        int i = new Random().nextInt(5);
        switch (i) {
            case 0:
                return "StandingStill";
            case 1:
                return "Walking";
            case 2:
                return "Running";
            case 3:
                return "ChangingOrientation";
            default:
                return "RotatingOrShaking";
        }
    }

    public float getGripMovement() {
        return new Random().nextInt(10) / 10.0f;
    }

    public List<GyroRadsObject> getGyroRads() {
        GyroRadsObject rand1 = new GyroRadsObject();
        GyroRadsObject rand2 = new GyroRadsObject();
        GyroRadsObject rand3 = new GyroRadsObject();
        GyroRadsObject rand4 = new GyroRadsObject();
        List<GyroRadsObject> list = new ArrayList<>();
        list.add(rand1);
        list.add(rand2);
        list.add(rand3);
        list.add(rand4);
        return list;
    }

    public List<PitchRollObject> getGyroPitch() {
        PitchRollObject rand1 = new PitchRollObject();
        PitchRollObject rand2 = new PitchRollObject();
        PitchRollObject rand3 = new PitchRollObject();
        PitchRollObject rand4 = new PitchRollObject();
        List<PitchRollObject> list = new ArrayList<>();
        list.add(rand1);
        list.add(rand2);
        list.add(rand3);
        list.add(rand4);
        return list;
    }

    public DNEStatusCode getUserMovementDNEStatus() {
        int i = new Random().nextInt(8);
        return DNEStatusCode.getByID(i);
    }

    public DNEStatusCode getGyroMotionDNEStatus() {
        int i = new Random().nextInt(8);
        return DNEStatusCode.getByID(i);
    }

    public DNEStatusCode getLocationDNEStatus() {
        int i = new Random().nextInt(8);
        return DNEStatusCode.getByID(i);
    }

    public DNEStatusCode getConnectedClassicDNEStatus() {
        return connectedClassicDNEStatus;
    }

    public DNEStatusCode getDiscoveredBLEDNEStatus() {
        return discoveredBLEDNEStatus;
    }

    public void setConnectedClassicDNEStatus(DNEStatusCode connectedClassicDNEStatus) {
        this.connectedClassicDNEStatus = connectedClassicDNEStatus;
    }

    public void setDiscoveredBLEDNEStatus(DNEStatusCode discoveredBLEDNEStatus) {
        this.discoveredBLEDNEStatus = discoveredBLEDNEStatus;
    }

    public void setDiscoveredBLEDevices(Set<String> discoveredBLEDevices) {
        this.discoveredBLEDevices = discoveredBLEDevices;
    }

    public void setConnectedClassicBTDevices(Set<String> connectedClassicBTDevices) {
        this.connectedClassicBTDevices = connectedClassicBTDevices;
    }

    public Set<String> getClassicBTInfo() {
        if(connectedClassicBTDevices == null || connectedClassicBTDevices.size() == 0){
            if(connectedClassicDNEStatus == DNEStatusCode.EXPIRED)
                return connectedClassicBTDevices;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 50;

            while((currentTime - startTime) < waitTime){
                if(connectedClassicBTDevices != null && connectedClassicBTDevices.size() > 0)
                    return connectedClassicBTDevices;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setConnectedClassicDNEStatus(DNEStatusCode.NO_DATA);
            return connectedClassicBTDevices;
        }
        return connectedClassicBTDevices;
//        Random r = new Random();
//        int rand = r.nextInt(4);
//        List<String> listOfDevices = new ArrayList<>();
//        for (int i = 0; i < rand; i++) {
//            listOfDevices.add("device" + r.nextInt(20));
//        }
//        return listOfDevices;
    }

    public Set<String> getDiscoveredBLEInfo() {
        if(discoveredBLEDevices == null || discoveredBLEDevices.size() == 0){
            if(discoveredBLEDNEStatus == DNEStatusCode.EXPIRED)
                return discoveredBLEDevices;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 250;

            while((currentTime - startTime) < waitTime){
                if(discoveredBLEDevices != null && discoveredBLEDevices.size() > 0)
                    return discoveredBLEDevices;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                currentTime = System.currentTimeMillis();
            }

            setDiscoveredBLEDNEStatus(DNEStatusCode.NO_DATA);
            return discoveredBLEDevices;
        }
        return discoveredBLEDevices;
//        Random r = new Random();
//        int rand = r.nextInt(4);
//        List<String> listOfDevices = new ArrayList<>();
//        for (int i = 0; i < rand; i++) {
//            listOfDevices.add("device" + r.nextInt(20));
//        }
//        return listOfDevices;
    }

    public Location getLocationInfo() {
        Random r = new Random();
        Location l = new Location("random_provider");
        l.setLatitude((double) (r.nextInt(500) + 1000) / 1000.0f);
        l.setLongitude((double) (r.nextInt(500) + 1000) / 1000.0f);
        return l;
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


    /**
     * Call on reloading login
     */
    public static void destroy() {
        sInstance = null;
    }
}
