package com.sentegrity.core_detection.transparent_authentication;

import android.content.Context;

import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.constants.CoreDetectionResult;
import com.sentegrity.core_detection.constants.PostAuthAction;
import com.sentegrity.core_detection.constants.PreAuthAction;
import com.sentegrity.core_detection.policy.SentegrityPolicy;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityTransparentAuthentication {


    private static SentegrityTransparentAuthentication sInstance;

    private SentegrityTransparentAuthentication() {
    }

    public static SentegrityTransparentAuthentication getInstance() {
        if (sInstance == null) {
            sInstance = new SentegrityTransparentAuthentication();
        }
        return sInstance;
    }

    public SentegrityTrustScoreComputation attemptTransparentAuthentication(SentegrityTrustScoreComputation computationResults, SentegrityPolicy policy){

        if(computationResults.getTransparentAuthenticationTrustFactorOutputs() == null || computationResults.getTransparentAuthenticationTrustFactorOutputs().size() == 0){
            computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_ERROR);
            computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
            computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
            return computationResults;
        }

        String candidateTransparentAuthKeyRawOutputString = "";

        return computationResults;
    }
}
