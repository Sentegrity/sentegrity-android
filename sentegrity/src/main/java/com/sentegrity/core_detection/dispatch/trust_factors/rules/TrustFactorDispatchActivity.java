package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

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
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput deviceState(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String anomalyString = "";

        //TODO: we're gonna check one by one all that we have
        //here's ios list
//        if ([statusBar[@"isBackingUp"] intValue]==1)
        //not sure we can check this

//        if ([statusBar[@"isOnCall"] intValue]==1)
        //for this we'll need phonelistener

//        if ([statusBar[@"isNavigating"] intValue]==1)
//        if ([statusBar[@"isUsingYourLocation"] intValue]==1)
        //some idea for the solution?

//        if ([statusBar[@"doNotDisturb"] intValue]==1)
        //use Settings.ACTION_VOICE_CONTROL_DO_NOT_DISTURB_MODE only on android >= 23

//        if ([statusBar[@"orientationLock"] intValue]==1)
//        if ([statusBar[@"isTethering"] intValue]==1)
//        if (![statusBar[@"lastApp"] isEqualToString:@""])
//        if ([statusBar[@"isAirplaneMode"] intValue]==1)

        if(SentegrityTrustFactorDatasets.getInstance().isTethering()){
            anomalyString += "isTethering_";
        }
        if(SentegrityTrustFactorDatasets.getInstance().hasOrientationLock()){
            anomalyString += "orientationLock_";
        }
        if(!TextUtils.isEmpty(SentegrityTrustFactorDatasets.getInstance().getLastApplication())){

        }


        if(SentegrityTrustFactorDatasets.getInstance().isAirplaneMode()){
            anomalyString += "airplane_";
        }



        if(TextUtils.isEmpty(anomalyString)){
            anomalyString = "none_";
        }

        outputList.add(anomalyString);

        output.setOutput(outputList);

        return new SentegrityTrustFactorOutput();
    }
}
