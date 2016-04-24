package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.text.TextUtils;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.root.RootDetection;
import com.stericson.RootShell.RootShell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchSandbox {

    @Deprecated
    public static SentegrityTrustFactorOutput integrity(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput rootDetect(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String rootmethods = "";

        RootDetection rootDetection = SentegrityTrustFactorDatasets.getInstance().getRootDetection();

        if(rootDetection == null || !rootDetection.hasData()){
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        if(rootDetection.isAccessGiven){
            rootmethods += "appRunningAsRoot";
        }

        if(rootDetection.isRootAvailable){
            if(!TextUtils.isEmpty(rootmethods))
                rootmethods += "_";
            rootmethods += "superSuFound";
        }

        if(rootDetection.isBusyBoxAvailable){
            if(!TextUtils.isEmpty(rootmethods))
                rootmethods += "_";
            rootmethods += "busyBoxFound";
        }

        if(!TextUtils.isEmpty(rootmethods))
            outputList.add(rootmethods);

        output.setOutput(outputList);

        return output;
    }
}
