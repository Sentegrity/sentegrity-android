package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.os.SystemClock;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchPlatform {

    public static SentegrityTrustFactorOutput vulnerableVersion(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput versionAllowed(List<Object> payload){
        //TODO: maybe disable this one for android?
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput shortUptime(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        long uptime = SystemClock.elapsedRealtime();

        if(uptime <= 0) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        int secondsInHour = 3600;

        int hoursUp = (int) ((uptime / 1000.0f) / secondsInHour);

        String hoursUpString;

        if(hoursUp < (double) ((LinkedTreeMap)payload.get(0)).get("minimumHoursUp")){
            hoursUpString = "up" + hoursUp;
            outputList.add(hoursUpString);
        }

        output.setOutput(outputList);

        return output;
    }
}
