package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.location.Location;
import android.text.TextUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchCelluar {

    public static SentegrityTrustFactorOutput cellConnectionChange(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String cell;

        String carrierName = SentegrityTrustFactorDatasets.getInstance().getCarrierConnectionName();

        if (TextUtils.isEmpty(carrierName)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        String connectionSpeed = SentegrityTrustFactorDatasets.getInstance().getCarrierConnectionSpeed();

        if (TextUtils.isEmpty(connectionSpeed)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        cell = connectionSpeed + "_" + carrierName;

        Location currentLocation;

        if (SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() == DNEStatusCode.OK ||
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() == DNEStatusCode.EXPIRED) {

            currentLocation = SentegrityTrustFactorDatasets.getInstance().getLocationInfo();

            if (currentLocation != null) {
                int decimalPlaces = (int) (double) ((LinkedTreeMap) payload.get(0)).get("rounding");

                String locationBuilder = "LO_%." + decimalPlaces + "f_LT_%." + decimalPlaces + "f";
                String roundedLocation = String.format(locationBuilder, currentLocation.getLongitude(), currentLocation.getLatitude());
                cell += "_" + roundedLocation;
            }
        }

        outputList.add(cell);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput airplaneMode(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        Boolean airplaneMode = SentegrityTrustFactorDatasets.getInstance().isAirplaneMode();

        if (airplaneMode == null) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        if (airplaneMode) {
            outputList.add("airplane-enabled");
        }

        output.setOutput(outputList);

        return output;
    }
}
