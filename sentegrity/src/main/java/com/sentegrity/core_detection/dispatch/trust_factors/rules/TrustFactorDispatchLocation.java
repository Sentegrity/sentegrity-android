package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.location.Location;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.MagneticObject;
import com.sentegrity.core_detection.utilities.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchLocation {

    //TODO: agree on the values --> put them in policy
    private static Map<Integer, Integer> alignedLightNoLoc;
    private static Map<Integer, Integer> alignedLightLoc;
    static {
        alignedLightNoLoc = new ArrayMap<>();
        alignedLightNoLoc.put(1, 5);
        alignedLightNoLoc.put(2, 15);
        alignedLightNoLoc.put(3, 40);
        alignedLightNoLoc.put(4, 80);
        alignedLightNoLoc.put(5, 150);
        alignedLightNoLoc.put(6, 350);
        alignedLightNoLoc.put(7, 600);
        alignedLightNoLoc.put(8, Integer.MAX_VALUE/*1000*/);
    }
    static {
        alignedLightLoc = new ArrayMap<>();
        alignedLightLoc.put(1, 7);
        alignedLightLoc.put(2, 45);
        alignedLightLoc.put(3, 100);
        alignedLightLoc.put(4, 250);
        alignedLightLoc.put(5, 500);
        alignedLightLoc.put(6, Integer.MAX_VALUE/*1000*/);
    }
    public static SentegrityTrustFactorOutput locationGPS(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus());
            return output;
        }

        Location currentLocation = SentegrityTrustFactorDatasets.getInstance().getLocationInfo();

        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK) {
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
        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK &&
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


        /**
         * Magnetic field
         */

        if (!locationAvailable) {
            int magneticBlockSize = (int) (double) ((LinkedTreeMap) payload.get(0)).get("magneticBlockSize");

            if (SentegrityTrustFactorDatasets.getInstance().getMagneticHeadingDNEStatus() != DNEStatusCode.OK &&
                    SentegrityTrustFactorDatasets.getInstance().getMagneticHeadingDNEStatus() != DNEStatusCode.EXPIRED) {
                output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getMagneticHeadingDNEStatus());
                return output;
            }

            List<MagneticObject> headings = SentegrityTrustFactorDatasets.getInstance().getMagneticHeading();

            if (SentegrityTrustFactorDatasets.getInstance().getMagneticHeadingDNEStatus() == DNEStatusCode.OK) {
                if (headings != null) {
                    float magnitudeAverage;
                    float magnitudeTotal = 0.0f;
                    float magnitude;

                    int counter = 0;

                    for (MagneticObject heading : headings) {
                        magnitude = (float) Math.sqrt(Math.pow(heading.x, 2) + Math.pow(heading.y, 2) + Math.pow(heading.z, 2));
                        magnitudeTotal += magnitude;
                        counter++;
                    }

                    magnitudeAverage = magnitudeTotal / counter;

                    int blockOfMagnetic = (int) Math.ceil(Math.abs(magnitudeAverage) / magneticBlockSize);

                    String magnitudeString = "_MAGNET:" + blockOfMagnetic;

                    anomalyString += magnitudeString;

                } else {
                    output.setStatusCode(DNEStatusCode.UNAUTHORIZED);
                    return output;
                }
            } else {
                output.setStatusCode(DNEStatusCode.UNAUTHORIZED);
                return output;
            }
        }


        /**
         * Screen brightness
         */
        /*
        // check if auto brightness
        Float screenLevel = SentegrityTrustFactorDatasets.getInstance().getSystemBrightness();
        float brightnessBlockSize = 0;

        if (!locationAvailable) {
            brightnessBlockSize = (float) (double) ((LinkedTreeMap) payload.get(0)).get("brightnessBlocksizeNoLocation");
        } else {
            brightnessBlockSize = (float) (double) ((LinkedTreeMap) payload.get(0)).get("brightnessBlocksizeWithLocation");
        }

        if (screenLevel < 0) {
            screenLevel = 0.1f;
        }

        int blockOfBrightness = Math.round(screenLevel * brightnessBlockSize);
        */

        //we'll be using lux value (from light_sensor)
        //it returns ambient light value
        List<Integer> ambientLightData = SentegrityTrustFactorDatasets.getInstance().getAmbientLightData();

        if(ambientLightData == null || ambientLightData.size() == 0) {
            //TODO:do we need some penalization? do we really have this data always - check for "unsupported" status
            anomalyString += "_LIGHT:" + "NODATA";
        }else{

            int ambientLightLevel = 0;

            //we'll take an average from all the values we have
            for(Integer level : ambientLightData){
                ambientLightLevel += level;
            }
            ambientLightLevel = ambientLightLevel / ambientLightData.size();

            //set aligned value to highest possible
            int alignedAmbientLight = 9;

            Map<Integer, Integer> alignedValues;
            if (!locationAvailable) {
                alignedValues = alignedLightNoLoc;
            } else {
                alignedValues = alignedLightLoc;
            }

            for(Map.Entry<Integer, Integer> entry : alignedValues.entrySet()){
                if(ambientLightLevel < entry.getValue()){
                    alignedAmbientLight = entry.getKey();
                    break;
                }
            }

            anomalyString += "_LIGHT:" + alignedAmbientLight;
        }


        /**
         * Cellular signal and carrier name/speed
         */

        String carrierConnectionInfo = "";

        String carrierName = SentegrityTrustFactorDatasets.getInstance().getCarrierConnectionName();

        if (TextUtils.isEmpty(carrierName)) {
            carrierName = "None";
        }

        String carrierConnectionSpeed = SentegrityTrustFactorDatasets.getInstance().getCarrierConnectionSpeed();

        if (TextUtils.isEmpty(carrierConnectionSpeed)) {
            carrierConnectionSpeed = "None";
        }

        carrierConnectionInfo = carrierName + carrierConnectionSpeed;

        int cellularBlockSize;

        //TODO: should we have different blocksizes for different connection speeds?
        if (!locationAvailable) {
            cellularBlockSize = (int) (double) ((LinkedTreeMap) payload.get(0)).get("cellSignalBlocksizeNoLocation");
        } else {
            cellularBlockSize = (int) (double) ((LinkedTreeMap) payload.get(0)).get("cellSignalBlocksizeWithLocation");
        }

        //TODO: check if we have it at all? no data or simply nosignal?
        Integer signal = SentegrityTrustFactorDatasets.getInstance().getCellularSignalRaw();
        String cellular;

        if (signal == null) {
            Boolean enabled = SentegrityTrustFactorDatasets.getInstance().isAirplaneMode();
            if (enabled == null || !enabled) {
                cellular = "_CELL:NOSIGNAL";
            } else {
                cellular = "_CELL:AIRPLANE";
            }
        } else {
            int blockOfSignal = Math.round(Math.abs(signal / (float) cellularBlockSize));
            cellular = "_CELL:" + carrierConnectionInfo + "_" + blockOfSignal;
        }

        anomalyString += cellular;

        outputList.add(anomalyString);

        output.setOutput(outputList);

        return output;
    }
}
