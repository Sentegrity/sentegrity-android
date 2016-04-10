package com.sentegrity.core_detection.dispatch.trust_factors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetMotion;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.MagneticObject;
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
    private Location location;
    private Float brightness = null;
    private Integer celluarSignalRaw = null;
    private Float gripMovement = null;
    private String userMovement = null;
    private String deviceOrientation = null;

    private Set<String> connectedClassicBTDevices;
    private Set<String> discoveredBLEDevices;

    private static SentegrityTrustFactorDatasets sInstance;
    private final Context context;

    private DNEStatusCode locationDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode connectedClassicDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode discoveredBLEDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode gyroMotionDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode magneticHeadingDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode userMovementDNEStatus = DNEStatusCode.OK;
    private DNEStatusCode accelMotionDNEStatus = DNEStatusCode.OK;

    private List<MagneticObject> magneticHeading;
    private List<GyroRadsObject> gyroRads;
    private List<PitchRollObject> pitchRoll;
    private List<AccelRadsObject> accelRads;

    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;
    private String carrierConnectionSpeed;
    private PhoneStateListener phoneStateListener;

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

    public Integer getCelluarSignalRaw() {
        //TODO: not really a good one! move data to some static place -> maybe implement same as location!
        //takes about 10-15ms
        //also looper stops working after 3, 4 runs.
        if (celluarSignalRaw == null) {
            if (!updateTelefonyManager()) {
                return celluarSignalRaw = null;
            }

            Looper.prepare();
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    super.onSignalStrengthsChanged(signalStrength);

                    Log.d("strength", "strength start calc");
                    int strength = 0;
                    boolean gotValidValue = false;

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
                        celluarSignalRaw = strength;
                        Log.d("strength", "strength " + strength);
                    } else {
                        celluarSignalRaw = null;
                    }
                    telephonyManager.listen(this, LISTEN_NONE);
                    if (Looper.myLooper() != null)
                        Looper.myLooper().quit();
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
            Looper.loop();

            return celluarSignalRaw;

        }
        return celluarSignalRaw;
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

    public List<AccelRadsObject> getAccelRads(){
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

    public List<MagneticObject> getMagneticHeading(){
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

    public DNEStatusCode getConnectedClassicDNEStatus() {
        return connectedClassicDNEStatus;
    }

    public DNEStatusCode getDiscoveredBLEDNEStatus() {
        return discoveredBLEDNEStatus;
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

    public void setConnectedClassicDNEStatus(DNEStatusCode connectedClassicDNEStatus) {
        this.connectedClassicDNEStatus = connectedClassicDNEStatus;
    }

    public void setDiscoveredBLEDNEStatus(DNEStatusCode discoveredBLEDNEStatus) {
        this.discoveredBLEDNEStatus = discoveredBLEDNEStatus;
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


    public void setDiscoveredBLEDevices(Set<String> discoveredBLEDevices) {
        this.discoveredBLEDevices = discoveredBLEDevices;
    }

    public void setConnectedClassicBTDevices(Set<String> connectedClassicBTDevices) {
        this.connectedClassicBTDevices = connectedClassicBTDevices;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<String> getClassicBTInfo() {
        if (connectedClassicBTDevices == null || connectedClassicBTDevices.size() == 0) {
            if (getConnectedClassicDNEStatus() == DNEStatusCode.EXPIRED)
                return connectedClassicBTDevices;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 50;

            while ((currentTime - startTime) < waitTime) {
                if (connectedClassicBTDevices != null && connectedClassicBTDevices.size() > 0)
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
        if (discoveredBLEDevices == null || discoveredBLEDevices.size() == 0) {
            if (getDiscoveredBLEDNEStatus() == DNEStatusCode.EXPIRED)
                return discoveredBLEDevices;

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            float waitTime = 250;

            while ((currentTime - startTime) < waitTime) {
                if (discoveredBLEDevices != null && discoveredBLEDevices.size() > 0)
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
//        Random r = new Random();
//        Location l = new Location("random_provider");
//        l.setLatitude((double) (r.nextInt(500) + 1000) / 1000.0f);
//        l.setLongitude((double) (r.nextInt(500) + 1000) / 1000.0f);
//        return l;
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

    public Float getSystemBrightness() {
        if (brightness == null) {
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
            //TODO: this will return same old value if set to auto mode. cannot get real current if in auto!
            float current = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
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


    /**
     * Call on reloading login
     */
    public static void destroy() {
        sInstance = null;
    }
}
