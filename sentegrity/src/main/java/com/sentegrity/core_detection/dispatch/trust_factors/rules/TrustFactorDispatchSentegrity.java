package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.text.TextUtils;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.root.RootDetection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchSentegrity {

    public static SentegrityTrustFactorOutput tamper(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String tamper = "";

        Boolean appSignatureChanged = SentegrityTrustFactorDatasets.getInstance().isAppSignatureOk();

        if(appSignatureChanged == null){
            //unknown
        }else if(!appSignatureChanged){
            tamper += "APK_SIGNATURE_CHANGED";
        }

        Boolean appRunOnEmulator = SentegrityTrustFactorDatasets.getInstance().isOnEmulator();

        if(appRunOnEmulator == null){
            //unknown
        }else if(appRunOnEmulator){
            tamper += "_RUN_ON_EMULATOR";
        }

        Boolean isDebuggable = SentegrityTrustFactorDatasets.getInstance().checkDebuggable();

        if(isDebuggable == null){
            //unknown
        }else if(isDebuggable){
            tamper += "_APP_IS_DEBUGGABLE";
        }

        Boolean isFromPlayStore = SentegrityTrustFactorDatasets.getInstance().isFromPlayStore();

        if(isFromPlayStore == null){
            //unknown
        }else if(!isFromPlayStore){
            tamper += "_APP_NOT_FROM_PLAY_STORE";
        }

        if(!TextUtils.isEmpty(tamper))
            outputList.add(tamper);

        output.setOutput(outputList);

        return output;
    }
}
