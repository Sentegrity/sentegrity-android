package com.sentegrity.core_detection.dispatch;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 23/03/16.
 */
public class SentegrityTrustFactorDispatcher {

    public static List<SentegrityTrustFactorOutput> performTrustFactorAnalysis(List<SentegrityTrustFactor> trustFactors, int timeout){

        boolean timeoutHit = false;
        long startTime = System.currentTimeMillis();
        SentegrityStartupStore.getInstance().setCurrentState("Performing TrustFactor Analysis");

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        List<SentegrityTrustFactorOutput> trustFactorOutputs = new ArrayList<>();

        for(SentegrityTrustFactor tf : trustFactors){
            if(timeoutHit){
                SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();
                output.setStatusCode(DNEStatusCode.EXPIRED);
                output.setTrustFactor(tf);
                trustFactorOutputs.add(output);
                continue;
            }

            long currentTime = System.currentTimeMillis();
            if(currentTime - startTime > timeout){
                SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();
                output.setStatusCode(DNEStatusCode.EXPIRED);
                output.setTrustFactor(tf);
                trustFactorOutputs.add(output);

                timeoutHit = true;
                continue;
            }

            SentegrityTrustFactorOutput output = executeTrustFactor(tf, startup);
            trustFactorOutputs.add(output);
        }

        return trustFactorOutputs;
    }

    private static SentegrityTrustFactorOutput executeTrustFactor(SentegrityTrustFactor trustFactor, SentegrityStartup startup){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        return output;
    }
}
