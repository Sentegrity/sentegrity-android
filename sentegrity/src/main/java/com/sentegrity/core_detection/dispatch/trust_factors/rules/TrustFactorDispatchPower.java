package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchPower {

    public static SentegrityTrustFactorOutput powerLevelTime(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        float batteryCharge = SentegrityTrustFactorDatasets.getInstance().getBatteryPercent();
        float batteryLevel = 0;

        if(batteryCharge > 0.0f)
            batteryLevel = 100 * batteryCharge;
        else{
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        double powerSizeBlock =  (double) ((LinkedTreeMap)payload.get(0)).get("powerInBlock");
        int blockOfPower = (int) Math.ceil(batteryLevel / (float) powerSizeBlock);

        String blockOfDay = SentegrityTrustFactorDatasets.getInstance().getTimeDateString((double) ((LinkedTreeMap)payload.get(0)).get("hoursInBlock"), false);

        outputList.add(blockOfDay + "-P" + blockOfPower);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput pluggedIn(List<Object> payload){
        //TODO: rework and add all possible states
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String state = SentegrityTrustFactorDatasets.getInstance().getBatteryState();

        if("pluggedFull".equals(state)){
            outputList.add(state);
        }else if("pluggedCharging".equals(state)){
            if(SentegrityTrustFactorDatasets.getInstance().getBatteryPercent() > 0.3){
                outputList.add(state);
            }
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput batteryState(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
