package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.net.wifi.WifiInfo;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;
import com.sentegrity.core_detection.utilities.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchWifi {

    public static SentegrityTrustFactorOutput consumerAP(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (!SentegrityTrustFactorDatasets.getInstance().isWifiEnabled()) {
            output.setStatusCode(DNEStatusCode.DISABLED);
            return output;
        } else if (SentegrityTrustFactorDatasets.getInstance().isTethering()) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        WifiInfo wifiInfo = SentegrityTrustFactorDatasets.getInstance().getWifiInfo();

        if (wifiInfo == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String bssid = wifiInfo.getBSSID();

        if (TextUtils.isEmpty(bssid)) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String bssidLowerCase = bssid.toLowerCase();

        List<String> ouiList = SentegrityTrustFactorDatasets.getInstance().getOUIList();

        boolean OUImatch = false;
        if (ouiList != null) {
            for (String oui : ouiList) {
                if (bssidLowerCase.contains(oui.toLowerCase())) {
                    OUImatch = true;
                    break;
                }
            }
        }

        //WifiInfo.getIpAddress() returns ip in integer form, so we need to get it back to string
        String gatewayIP = Helpers.intToIP(wifiInfo.getIpAddress());

        boolean IPmatch = false;
        for (int i = 0; i < payload.size(); i++) {
            String ip = (String) payload.get(i);
            if (gatewayIP.contains(ip)) {
                IPmatch = true;
                break;
            }
        }

        if (OUImatch && IPmatch) {
            String ssid = Helpers.getSSIDfromWifiInfo(wifiInfo);
            outputList.add(ssid);
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput hotspotEnabled(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().isTethering()) {
            outputList.add("hotspotOn");
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput defaultSSID(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if (!SentegrityTrustFactorDatasets.getInstance().isWifiEnabled()) {
            output.setStatusCode(DNEStatusCode.DISABLED);
            return output;
        } else if (SentegrityTrustFactorDatasets.getInstance().isTethering()) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        WifiInfo wifiInfo = SentegrityTrustFactorDatasets.getInstance().getWifiInfo();

        if (wifiInfo == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String ssid = Helpers.getSSIDfromWifiInfo(wifiInfo);

        if (TextUtils.isEmpty(ssid)) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        List<String> ssidList = SentegrityTrustFactorDatasets.getInstance().getSSIDList();

        if (ssidList == null || ssidList.size() == 0) {
            if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
                output.setStatusCode(DNEStatusCode.ERROR);
                return output;
            } else {
                ssidList = new ArrayList<>();
                for (int i = 0; i < payload.size(); i++) {
                    ssidList.add((String) payload.get(0));
                }
            }
        }

        for (int i = 0; i < ssidList.size(); i++) {
            if (ssid.matches(ssidList.get(i))) {
                outputList.add(ssid);
                break;
            }
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput hotspot(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if (!SentegrityTrustFactorDatasets.getInstance().isWifiEnabled()) {
            output.setStatusCode(DNEStatusCode.DISABLED);
            return output;
        } else if (SentegrityTrustFactorDatasets.getInstance().isTethering()) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        WifiInfo wifiInfo = SentegrityTrustFactorDatasets.getInstance().getWifiInfo();

        if (wifiInfo == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String ssid = Helpers.getSSIDfromWifiInfo(wifiInfo);

        if (TextUtils.isEmpty(ssid)) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String ssidLowerCase = ssid.toLowerCase();

        List<String> hotspotList = SentegrityTrustFactorDatasets.getInstance().getHotspotList();

        boolean hotspotListMatch = false;

        if (hotspotList != null) {
            for (String hotspot : hotspotList) {
                if (ssidLowerCase.contains(hotspot.toLowerCase())) {
                    hotspotListMatch = true;
                    break;
                }
            }
        }

        boolean hotspotDynamicMatch = false;

        if (!hotspotListMatch) {
            if (ssidLowerCase.contains("wifi") || ssid.contains("wi-fi")) {
                if (ssidLowerCase.contains("free"))
                    hotspotDynamicMatch = true;
                if (ssidLowerCase.contains("guest"))
                    hotspotDynamicMatch = true;
                if (ssidLowerCase.contains("public"))
                    hotspotDynamicMatch = true;
            } else if (ssidLowerCase.contains("hotspot")) {
                hotspotDynamicMatch = true;
            } else if (ssid.contains("guest")) {
                if (ssidLowerCase.contains("net"))
                    hotspotDynamicMatch = true;
                if (ssidLowerCase.contains("_"))
                    hotspotDynamicMatch = true;
                if (ssidLowerCase.contains("-"))
                    hotspotDynamicMatch = true;
            }
        }

        if (hotspotDynamicMatch || hotspotListMatch) {
            outputList.add(ssid);
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput SSIDBSSID(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (!SentegrityTrustFactorDatasets.getInstance().isWifiEnabled()) {
            output.setStatusCode(DNEStatusCode.DISABLED);
            return output;
        } else if (SentegrityTrustFactorDatasets.getInstance().isTethering()) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        WifiInfo wifiInfo = SentegrityTrustFactorDatasets.getInstance().getWifiInfo();

        if (wifiInfo == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String ssid = Helpers.getSSIDfromWifiInfo(wifiInfo);

        String bssid = wifiInfo.getBSSID();

        int macLength = (int) ((double) ((LinkedTreeMap) payload.get(0)).get("MACAddresslength"));

        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(bssid)) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String trimmedBSSID = bssid.substring(0, macLength);

        outputList.add(ssid + "_" + trimmedBSSID);

        output.setOutput(outputList);

        return output;
    }
}
