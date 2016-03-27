package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchTime {

    public static SentegrityTrustFactorOutput accessTime(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        List<String> outputList = new ArrayList<>();
        outputList.add("randomTime-" + new Random().nextInt(2));
        output.setOutput(outputList);

        return output;
    }
}
