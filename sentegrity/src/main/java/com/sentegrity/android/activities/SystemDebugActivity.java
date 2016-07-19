package com.sentegrity.android.activities;

import android.os.Bundle;

import com.sentegrity.android.R;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;

/**
 * Created by dmestrov on 02/04/16.
 */
public class SystemDebugActivity extends DebugActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected String getActivityTitle() {
        return "System Debug";
    }

    @Override
    protected String getDebugInfo() {
        String complete = "";

        String systemTrustFactorsTriggered = "\nTrustFactors Triggered\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getSystemTrustFactorsAttributingToScore()){
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

            systemTrustFactorsTriggered += "--Name: " + output.getTrustFactor().getName().toUpperCase() + "\n\n"
                    + "Weight Applied: " + output.getAppliedWeight() + "\n"
                    + "Weight Percent: " + output.getPercentAppliedWeight() + "\n"
                    + "Use Partial: " + output.getTrustFactor().getPartialWeight() + "\n"
                    + "Total Possible: " + output.getTrustFactor().getWeight() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions
                    + "Matching Assertions: " + "\n" + storedAssertions;
        }

        complete += systemTrustFactorsTriggered;

        String systemTrustFactorsToWhitelist = "\nTrustFactors To Whitelist\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getSystemTrustFactorWhitelist()){
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

            systemTrustFactorsToWhitelist += "--Name: " + output.getTrustFactor().getName().toUpperCase() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions
                    + "Stored Assertions: " + "\n" + storedAssertions;
        }

        complete += systemTrustFactorsToWhitelist;

        String systemTrustFactorsNotLearned = "\nTrustFactors Not Learned\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getSystemTrustFactorsNotLearned()){
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

            systemTrustFactorsNotLearned += "--Name: " + output.getTrustFactor().getName().toUpperCase() + "\n\n"
                    + "Current Assertions: " + "\n" + currentAssertions
                    + "Stored Assertions: " + "\n" + storedAssertions;
        }

        complete += systemTrustFactorsNotLearned;

        String systemTrustFactorsWithError = "\nTrustFactors Errored\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getSystemTrustFactorsWithErrors()){

            systemTrustFactorsWithError += "--Name: " + output.getTrustFactor().getName().toUpperCase() + "\n"
                    + "DNE: " + output.getStatusCode() + " (" + DNEStatusCode.toString(output.getStatusCode()) + ")" + "\n\n";
        }

        complete += systemTrustFactorsWithError;

        return complete;
    }
}
