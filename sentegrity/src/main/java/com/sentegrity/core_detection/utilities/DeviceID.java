package com.sentegrity.core_detection.utilities;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Random;

/**
 * Created by dmestrov on 23/03/16.
 */
public class DeviceID {
    private static final String STORAGE_DEVICE_ID_KEY = "device_id";

    private static String mID = null;

    /**
     *  Return a cached unique ID for each device
     */
    public static String getID(Context context, KeyValueStorage storage) {
        // if the ID isn't cached inside the class itself
        if (null == mID) {
            // get it from storage
            mID = storage.readString(STORAGE_DEVICE_ID_KEY, "0");
        }

        // if the saved value was incorrect
        if (TextUtils.equals(mID, "0")) {
            // generate a new ID
            mID = generateID(context);

            if (mID != null) {
                // save it to storage
                storage.storeString(STORAGE_DEVICE_ID_KEY, mID);
            }
        }

        return mID;
    }

    /**
     *  Generate a unique ID for each device
     */
    private static String generateID(Context context) {

        // use the ANDROID_ID constant, generated at the first device boot
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // in case known problems are occured
        if (TextUtils.equals("9774d56d682e549c", deviceId) || deviceId == null) {

            // get a unique deviceID like IMEI for GSM or ESN for CDMA phones
            deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

            // if nothing else works, just generate a random number
            if (deviceId == null) {
                deviceId = String.valueOf(new Random().nextLong());
            }
        }

        // any value is hashed to have consistent format
        return deviceId;
    }
}