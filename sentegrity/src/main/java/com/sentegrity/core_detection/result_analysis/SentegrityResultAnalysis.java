package com.sentegrity.core_detection.result_analysis;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.constants.CoreDetectionResult;
import com.sentegrity.core_detection.constants.PostAuthAction;
import com.sentegrity.core_detection.constants.PreAuthAction;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.transparent_authentication.SentegrityTransparentAuthentication;

/**
 * Created by dmestrov on 03/04/16.
 */
public class SentegrityResultAnalysis {

    public static SentegrityTrustScoreComputation analyzeResults(SentegrityTrustScoreComputation computationResults, SentegrityPolicy policy) {
        computationResults.setDeviceTrusted(true);
        computationResults.setUserTrusted(true);
        computationResults.setSystemTrusted(true);
        computationResults.setAttemptTransparentAuthentication(true);


        if (computationResults.getSystemScore() < policy.getSystemThreshold()) {
            computationResults.setSystemTrusted(false);
            computationResults.setDeviceTrusted(false);
            computationResults.setAttemptTransparentAuthentication(false);
        }
        if (computationResults.getUserScore() < policy.getUserThreshold()) {
            computationResults.setUserTrusted(false);
            computationResults.setDeviceTrusted(false);
            computationResults.setAttemptTransparentAuthentication(false);
        }

        if (computationResults.isShouldAttemptTransparentAuthentication()) {

            boolean entropyRequirementsMeetForTransparentAuth;
            entropyRequirementsMeetForTransparentAuth = SentegrityTransparentAuthentication.getInstance().analyzeEligibleTransparentAuthObjects(computationResults, policy);

            if(!entropyRequirementsMeetForTransparentAuth){
                computationResults.setShouldAttemptTransparentAuthentication(false);
                computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_ENTROPY_LOW);
                computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
                computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
            }
        }

        if(!computationResults.isSystemTrusted()){
            if(computationResults.getSystemBreachScore() <= computationResults.getSystemSecurityScore()){

                computationResults.setCoreDetectionResult(CoreDetectionResult.DEVICE_COMPROMISE);
                computationResults.setAttributingClassID(computationResults.getSystemBreachClass().getID());
                computationResults.setPreAuthenticationAction(computationResults.getSystemBreachClass().getPreAuthenticationAction());
                computationResults.setPostAuthenticationAction(computationResults.getSystemBreachClass().getPostAuthenticationAction());

                computationResults.setSystemGUIIconID(computationResults.getSystemBreachClass().getID());
                computationResults.setSystemGUIIconText(computationResults.getSystemBreachClass().getDescription());

            }else if(computationResults.getSystemPolicyScore() <= computationResults.getSystemSecurityScore()){

                computationResults.setCoreDetectionResult(CoreDetectionResult.POLICY_VIOLATION);
                computationResults.setAttributingClassID(computationResults.getSystemPolicyClass().getID());
                computationResults.setPreAuthenticationAction(computationResults.getSystemPolicyClass().getPreAuthenticationAction());
                computationResults.setPostAuthenticationAction(computationResults.getSystemPolicyClass().getPostAuthenticationAction());

                computationResults.setSystemGUIIconID(computationResults.getSystemPolicyClass().getID());
                computationResults.setSystemGUIIconText(computationResults.getSystemPolicyClass().getDescription());

            }else{

                computationResults.setCoreDetectionResult(CoreDetectionResult.HIGH_RISK_DEVICE);
                computationResults.setAttributingClassID(computationResults.getSystemSecurityClass().getID());
                computationResults.setPreAuthenticationAction(computationResults.getSystemSecurityClass().getPreAuthenticationAction());
                computationResults.setPostAuthenticationAction(computationResults.getSystemSecurityClass().getPostAuthenticationAction());

                computationResults.setSystemGUIIconID(computationResults.getSystemSecurityClass().getID());
                computationResults.setSystemGUIIconText(computationResults.getSystemSecurityClass().getDescription());

            }
        }else {
            computationResults.setSystemGUIIconID(0);
            computationResults.setSystemGUIIconText("Device\nTrusted");
        }

        if(!computationResults.isUserTrusted()){
            if(computationResults.getUserPolicyScore() <= computationResults.getUserAnomalyScore()){

                computationResults.setUserGUIIconID(computationResults.getUserPolicyClass().getID());
                computationResults.setUserGUIIconText(computationResults.getUserPolicyClass().getDescription());

                if(computationResults.isSystemTrusted()){

                    computationResults.setCoreDetectionResult(CoreDetectionResult.POLICY_VIOLATION);
                    computationResults.setAttributingClassID(computationResults.getUserPolicyClass().getID());
                    computationResults.setPreAuthenticationAction(computationResults.getUserPolicyClass().getPreAuthenticationAction());
                    computationResults.setPostAuthenticationAction(computationResults.getUserPolicyClass().getPostAuthenticationAction());

                }else{

                }

            }else{

                computationResults.setUserGUIIconID(computationResults.getUserAnomalyClass().getID());
                computationResults.setUserGUIIconText(computationResults.getUserAnomalyClass().getDescription());

                if(computationResults.isSystemTrusted()){

                    computationResults.setCoreDetectionResult(CoreDetectionResult.USER_ANOMALY);
                    computationResults.setAttributingClassID(computationResults.getUserAnomalyClass().getID());
                    computationResults.setPreAuthenticationAction(computationResults.getUserAnomalyClass().getPreAuthenticationAction());
                    computationResults.setPostAuthenticationAction(computationResults.getUserAnomalyClass().getPostAuthenticationAction());

                }else{

                }
            }
        }else {
            computationResults.setUserGUIIconID(0);
            computationResults.setUserGUIIconText("User\nTrusted");
        }

        return computationResults;
    }
}
