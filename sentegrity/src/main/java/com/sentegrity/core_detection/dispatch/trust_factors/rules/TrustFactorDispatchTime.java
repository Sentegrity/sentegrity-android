package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchTime {

    public static SentegrityTrustFactorOutput accessTimeHour(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        double hoursInBlock = (double) ((LinkedTreeMap)payload.get(0)).get("hoursInBlock");

        List<String> outputList = new ArrayList<>();

        String time = SentegrityTrustFactorDatasets.getInstance().getTimeDateString(hoursInBlock, false);

        outputList.add(time);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput accessTimeDay(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String day = SentegrityTrustFactorDatasets.getInstance().getTimeDateString(0, true);

        outputList.add(day);

        output.setOutput(outputList);

        return output;
    }
}
