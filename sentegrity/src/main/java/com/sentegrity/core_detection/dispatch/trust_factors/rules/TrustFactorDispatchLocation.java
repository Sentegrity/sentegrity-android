package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.location.Location;
import android.net.wifi.WifiInfo;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.utilities.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchLocation {

    public static SentegrityTrustFactorOutput locationGPS(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != null &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus());
            return output;
        }

        Location currentLocation = SentegrityTrustFactorDatasets.getInstance().getLocationInfo();

        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != null &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus());
            return output;
        }

        if (currentLocation == null) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        int decimalPlaces = (int) (double) ((LinkedTreeMap) payload.get(0)).get("rounding");

        String locationBuilder = "LO_%." + decimalPlaces + "f_LT_%." + decimalPlaces + "f";
        String roundedLocation = String.format(locationBuilder, currentLocation.getLongitude(), currentLocation.getLatitude());

        outputList.add(roundedLocation);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput locationApprox(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        String anomalyString = "";
        Location currentLocation;
        boolean locationAvailable = true;

        /**
         * Location
         */
        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != null &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.EXPIRED) {
            locationAvailable = false;
        } else {
            currentLocation = SentegrityTrustFactorDatasets.getInstance().getLocationInfo();

            if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() == DNEStatusCode.OK) {
                if (currentLocation != null) {
                    int decimalPlaces = (int) (double) ((LinkedTreeMap) payload.get(0)).get("locationRounding");
                    String locationBuilder = "LO_%." + decimalPlaces + "f_LT_%." + decimalPlaces + "f";
                    String roundedLocation = String.format(locationBuilder, currentLocation.getLongitude(), currentLocation.getLatitude());

                    anomalyString += roundedLocation;
                }
            } else {
                locationAvailable = false;
            }
        }

        /**
         * Wifi signal strength
         */
        WifiInfo wifiInfo = SentegrityTrustFactorDatasets.getInstance().getWifiInfo();

        if (wifiInfo == null) {
            anomalyString += "_WIFI:NOCON";
        } else {
            String ssid = Helpers.getSSIDfromWifiInfo(wifiInfo);

            int wifiSignal = wifiInfo.getRssi();

            if (wifiSignal == 0) {
                if (!SentegrityTrustFactorDatasets.getInstance().isWifiEnabled()) {
                    anomalyString += "_WIFI:DISABLED";
                } else if (SentegrityTrustFactorDatasets.getInstance().isTethering()) {
                    anomalyString += "_WIFI:TETHERING";
                } else {
                    anomalyString += "_WIFI:NOCON";
                }
            } else {
                int blockSize = 0;

                if (!locationAvailable) {
                    blockSize = (int) (double) ((LinkedTreeMap) payload.get(0)).get("wifiSignalBlocksizeNoLocation");
                } else {
                    blockSize = (int) (double) ((LinkedTreeMap) payload.get(0)).get("wifiSignalBlocksizeWithLocation");
                }

                int blockOfWifi = Math.round(Math.abs(wifiSignal) / (float) blockSize);

                anomalyString += "_WIFI:" + ssid + "_" + blockOfWifi;
            }
        }

        //TODO: magnetic field
        /**
         * Magnetic field
         */


        /**
         * Screen brightness
         */

        //TODO: if auto brightness
        float screenLevel = SentegrityTrustFactorDatasets.getInstance().getSystemBrightness();
        float brightnessBlockSize = 0;

        if(!locationAvailable){
            brightnessBlockSize = (float) (double) ((LinkedTreeMap) payload.get(0)).get("brightnessBlocksizeNoLocation");
        }else{
            brightnessBlockSize = (float) (double) ((LinkedTreeMap) payload.get(0)).get("brightnessBlocksizeWithLocation");
        }

        if(screenLevel < 0){
            screenLevel = 0.1f;
        }

        int blockOfBrightness = Math.round(screenLevel * brightnessBlockSize);

        anomalyString += "_LIGHT:" + blockOfBrightness;


        //TODO:
        /**
         * Cellular signal and carrier name/speed
         */


        outputList.add(anomalyString);

        output.setOutput(outputList);

        return output;
    }
}
