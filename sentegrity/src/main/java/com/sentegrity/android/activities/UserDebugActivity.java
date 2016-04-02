package com.sentegrity.android.activities;

import android.os.Bundle;

import com.sentegrity.android.R;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;

/**
 * Created by dmestrov on 02/04/16.
 */
public class UserDebugActivity extends DebugActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected String getAcivityTitle() {
        return "User Debug";
    }

    @Override
    protected String getDebugInfo() {
        String complete = "";

        String userTrustFactorsTriggered = "\nTrustFactors Attributing\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getUserTrustFactorsAttributingToScore()){
            String storedAssertions = "";
            String currentAssertions = "";

            for(SentegrityStoredAssertion storedAssertion : output.getStoredAssertionObjectsMatched()){
                storedAssertions += "Hash: " + "\n" + storedAssertion.getHash() + "\n"
                        + "HitCount: " + storedAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + storedAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + storedAssertion.getLastTime() + "\n\n";
            }

            for(SentegrityStoredAssertion currentAssertion : output.getCandidateAssertionObjects()){
                currentAssertions += "Hash: " + "\n" + currentAssertion.getHash() + "\n"
                        + "HitCount: " + currentAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + currentAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + currentAssertion.getLastTime() + "\n\n";
            }

            userTrustFactorsTriggered += "--Name: " + output.getTrustFactor().getName() + "\n\n"
                    + "Weight Applied: " + output.getAppliedWeight() + "\n"
                    + "Weight Percent: " + output.getPercentAppliedWeight() + "\n"
                    + "Use Partial: " + output.getTrustFactor().getPartialWeight() + "\n"
                    + "Total Possible: " + output.getTrustFactor().getWeight() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions
                    + "Matching Assertions: " + "\n" + storedAssertions;
        }

        complete += userTrustFactorsTriggered;

        String userTrustFactorsToWhitelist = "\nTrustFactors To Whitelist\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getProtectModeUserWhitelist()){
            String storedAssertions = "";
            String currentAssertions = "";

            for(SentegrityStoredAssertion storedAssertion : output.getStoredAssertionObjectsMatched()){
                storedAssertions += "Hash: " + "\n" + storedAssertion.getHash() + "\n"
                        + "HitCount: " + storedAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + storedAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + storedAssertion.getLastTime() + "\n\n";
            }

            for(SentegrityStoredAssertion currentAssertion : output.getCandidateAssertionObjects()){
                currentAssertions += "Hash: " + "\n" + currentAssertion.getHash() + "\n"
                        + "HitCount: " + currentAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + currentAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + currentAssertion.getLastTime() + "\n\n";
            }

            userTrustFactorsToWhitelist += "--Name: " + output.getTrustFactor().getName() + "\n\n"
                    + "Weight Applied: " + output.getAppliedWeight() + "\n"
                    + "Weight Percent: " + output.getPercentAppliedWeight() + "\n"
                    + "Use Partial: " + output.getTrustFactor().getPartialWeight() + "\n"
                    + "Total Possible: " + output.getTrustFactor().getWeight() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions
                    + "Matching Assertions: " + "\n" + storedAssertions;
        }

        complete += userTrustFactorsToWhitelist;

        String userTrustFactorsNotLearned = "\nTrustFactors Not Learned\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getProtectModeUserWhitelist()){
            String storedAssertions = "";
            String currentAssertions = "";

            for(SentegrityStoredAssertion storedAssertion : output.getStoredAssertionObjectsMatched()){
                storedAssertions += "Hash: " + storedAssertion.getHash() + "\n\n"
                        + "HitCount: " + storedAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + storedAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + storedAssertion.getLastTime() + "\n\n";
            }

            for(SentegrityStoredAssertion currentAssertion : output.getCandidateAssertionObjects()){
                currentAssertions += "Hash: " + "\n" + currentAssertion.getHash() + "\n"
                        + "HitCount: " + currentAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + currentAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + currentAssertion.getLastTime() + "\n\n";
            }

            userTrustFactorsNotLearned += "--Name: " + output.getTrustFactor().getName() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions
                    + "Stored Assertions: " + "\n" + storedAssertions;
        }

        complete += userTrustFactorsNotLearned;

        String userTrustFactorsWithError = "\nTrustFactors Errored\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getUserTrustFactorsWithErrors()){

            userTrustFactorsWithError += "--Name: " + output.getTrustFactor().getName() + "\n"
                    + "DNE: " + output.getStatusCode().getId() + " (" + output.getStatusCode() + ")" + "\n\n";
        }

        complete += userTrustFactorsWithError;

        String userTrustFactorsForTransparentAuth = "\nTrustFactors For Transparent Auth\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getTransparentAuthenticationTrustFactors()){
            String currentAssertions = "";

            for(SentegrityStoredAssertion currentAssertion : output.getCandidateAssertionObjects()){
                currentAssertions += "Hash: " + "\n" + currentAssertion.getHash() + "\n"
                        + "HitCount: " + currentAssertion.getHitCount() + "\n"
                        + "DecayMetric: " + currentAssertion.getDecayMetric() + "\n"
                        + "LastTime: " + currentAssertion.getLastTime() + "\n\n";
            }

            userTrustFactorsForTransparentAuth += "--Name: " + output.getTrustFactor().getName() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions;
        }

        complete += userTrustFactorsForTransparentAuth;

        return complete;
    }
}
