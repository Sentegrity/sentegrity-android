package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchWifi {

    public static SentegrityTrustFactorOutput consumerAP(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput hotspot(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput hotspotEnabled(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if(SentegrityTrustFactorDatasets.getInstance().isTethering()){
            outputList.add("hotspotOn");
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput defaultSSID(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput SSIDBSSID(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
