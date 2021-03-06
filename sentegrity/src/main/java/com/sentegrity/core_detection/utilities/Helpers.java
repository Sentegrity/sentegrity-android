package com.sentegrity.core_detection.utilities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.trustlook.sdk.Constants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 23/03/16.
 */
public class Helpers {


    /**
     * Generate Hash (SHA-1) for the given string
     */
    public static String getSHA1Hash(String stringToHash) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // this won't happen for SHA-1
            e.printStackTrace();
            return stringToHash;
        }

        byte[] result;

        try {
            result = digest.digest(stringToHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // this won't happen for UTF-8
            e.printStackTrace();
            return stringToHash;
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public static String intToIP(int ipInt){
        return String.format("%d.%d.%d.%d",
                (ipInt & 0xff),
                (ipInt >> 8 & 0xff),
                (ipInt >> 16 & 0xff),
                (ipInt >> 24 & 0xff));
    }

    public static String getSSIDfromWifiInfo(WifiInfo wifiInfo){
        if(wifiInfo == null) return null;
        String ssid = wifiInfo.getSSID();
        if(TextUtils.isEmpty(ssid)) return null;

        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        return ssid;
    }

    public static String getSystemProperty(String name) throws Exception {
        Class systemPropertyClazz = Class.forName("android.os.SystemProperties");
        return (String) systemPropertyClazz.getMethod("get", new Class[]{String.class}).invoke(systemPropertyClazz, name);
    }


    public static List<PackageInfo> getLocalAppsPkgInfo(Context context) {
        final int MAX_ATTEMPTS = 3;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try {
                return context.getPackageManager().getInstalledPackages(0);
            } catch (RuntimeException re) {
                // Just wait for cooling down
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            }
        }
        return new ArrayList<PackageInfo>();
    }
}
