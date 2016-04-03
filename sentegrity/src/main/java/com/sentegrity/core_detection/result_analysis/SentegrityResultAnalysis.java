package com.sentegrity.core_detection.result_analysis;

import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.policy.SentegrityPolicy;

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

        if (!computationResults.isSystemTrusted()) {
            computationResults.getProtectModeWhitelist().addAll(computationResults.getProtectModeSystemWhitelist());

            if (computationResults.getSystemBreachScore() <= computationResults.getSystemSecurityScore()) {
                computationResults.setProtectModeClassID(computationResults.getSystemBreachClass().getID());
                computationResults.setProtectModeAction(computationResults.getSystemBreachClass().getPreAuthenticationAction());

                computationResults.setSystemGUIIconID(computationResults.getSystemBreachClass().getID());
                computationResults.setSystemGUIIconText(computationResults.getSystemBreachClass().getDescription());
            } else if (computationResults.getSystemPolicyScore() <= computationResults.getSystemSecurityScore()) {
                computationResults.setProtectModeClassID(computationResults.getSystemPolicyClass().getID());
                computationResults.setProtectModeAction(computationResults.getSystemPolicyClass().getPreAuthenticationAction());

                computationResults.setSystemGUIIconID(computationResults.getSystemPolicyClass().getID());
                computationResults.setSystemGUIIconText(computationResults.getSystemPolicyClass().getDescription());
            } else {
                computationResults.setProtectModeClassID(computationResults.getSystemSecurityClass().getID());
                computationResults.setProtectModeAction(computationResults.getSystemSecurityClass().getPreAuthenticationAction());

                computationResults.setSystemGUIIconID(computationResults.getSystemSecurityClass().getID());
                computationResults.setSystemGUIIconText(computationResults.getSystemSecurityClass().getDescription());
            }

        } else {
            computationResults.setSystemGUIIconID(0);
            computationResults.setSystemGUIIconText("Device\nTrusted");
        }

        if (!computationResults.isUserTrusted()) {
            computationResults.getProtectModeWhitelist().addAll(computationResults.getProtectModeUserWhitelist());

            if (computationResults.getUserPolicyScore() <= computationResults.getUserAnomalyScore()) {

                if (computationResults.isSystemTrusted()) {
                    computationResults.setProtectModeClassID(computationResults.getUserPolicyClass().getID());
                    computationResults.setProtectModeAction(computationResults.getUserPolicyClass().getPreAuthenticationAction());
                }
                computationResults.setUserGUIIconID(computationResults.getUserPolicyClass().getID());
                computationResults.setUserGUIIconText(computationResults.getUserPolicyClass().getDescription());

            } else {

                if (computationResults.isSystemTrusted()) {
                    computationResults.setProtectModeClassID(computationResults.getUserAnomalyClass().getID());
                    computationResults.setProtectModeAction(computationResults.getUserAnomalyClass().getPreAuthenticationAction());
                }
                computationResults.setUserGUIIconID(computationResults.getUserAnomalyClass().getID());
                computationResults.setUserGUIIconText(computationResults.getUserAnomalyClass().getDescription());
            }

        } else {
            computationResults.setUserGUIIconID(0);
            computationResults.setUserGUIIconText("User\nTrusted");
        }

        return computationResults;
    }
}
