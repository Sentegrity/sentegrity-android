package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.text.TextUtils;

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

        String carrierName = SentegrityTrustFactorDatasets.getInstance().getCarrierConnectionName();

        if (TextUtils.isEmpty(carrierName) || true) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        outputList.add(carrierName);

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
            outputList.add("airplane");
        }

        output.setOutput(outputList);

        return output;
    }
}
