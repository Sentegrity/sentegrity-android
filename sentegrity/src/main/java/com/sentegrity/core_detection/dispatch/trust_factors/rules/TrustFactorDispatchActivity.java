package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchActivity {

    public static SentegrityTrustFactorOutput previous(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String userMovement = SentegrityTrustFactorDatasets.getInstance().getPreviousUserMovement();

        Log.d("Movement", "previous: " + userMovement);
        outputList.add(userMovement);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput deviceState(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String anomalyString = "";

        if(SentegrityTrustFactorDatasets.getInstance().isTethering() != null &&
                SentegrityTrustFactorDatasets.getInstance().isTethering()){
            anomalyString += "isTethering_";
        }
        if(SentegrityTrustFactorDatasets.getInstance().isOnCall() != null &&
                SentegrityTrustFactorDatasets.getInstance().isOnCall()){
            anomalyString += "onCall_";
        }
        if(SentegrityTrustFactorDatasets.getInstance().hasOrientationLock() != null &&
                SentegrityTrustFactorDatasets.getInstance().hasOrientationLock()){
            anomalyString += "orientationLock_";
        }
        if(SentegrityTrustFactorDatasets.getInstance().isNotDisturbMode() != null &&
                SentegrityTrustFactorDatasets.getInstance().isNotDisturbMode()){
            anomalyString += "doNotDisturb_";
        }
        if(!TextUtils.isEmpty(SentegrityTrustFactorDatasets.getInstance().getLastApplication())){

        }
        if(SentegrityTrustFactorDatasets.getInstance().isAirplaneMode() != null &&
                SentegrityTrustFactorDatasets.getInstance().isAirplaneMode()){
            anomalyString += "airplane_";
        }

        if(TextUtils.isEmpty(anomalyString)){
            anomalyString = "none_";
        }

        outputList.add(anomalyString);

        output.setOutput(outputList);

        return output;
    }
}
