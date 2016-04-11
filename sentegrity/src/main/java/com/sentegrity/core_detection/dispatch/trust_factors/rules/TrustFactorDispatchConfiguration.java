package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchConfiguration {

    public static SentegrityTrustFactorOutput backupEnabled(List<Object> payload) {
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput passcodeSet(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        Boolean isPasscodeSet = SentegrityTrustFactorDatasets.getInstance().isPasscodeSet();

        if(isPasscodeSet == null){
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }
        if(!isPasscodeSet)
            outputList.add("passcodeNotSet");

        output.setOutput(outputList);

        return output;
    }
}
