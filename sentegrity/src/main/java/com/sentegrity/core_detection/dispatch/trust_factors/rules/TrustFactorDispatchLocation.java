package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.location.Location;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchLocation {

    public static SentegrityTrustFactorOutput locationGPS(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.EXPIRED){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus());
            return output;
        }

        Location currentLocation = SentegrityTrustFactorDatasets.getInstance().getLocationInfo();

        if(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus() != DNEStatusCode.OK){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getLocationDNEStatus());
            return output;
        }

        if(currentLocation == null){
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        int decimalPlaces = (int) (double) ((LinkedTreeMap)payload.get(0)).get("rounding");

        String locationBuilder = "LO_%." + decimalPlaces + "f_LT_%." + decimalPlaces + "f";
        String roundedLocation = String.format(locationBuilder, currentLocation.getLongitude(), currentLocation.getLatitude());

        outputList.add(roundedLocation);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput locationApprox(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
