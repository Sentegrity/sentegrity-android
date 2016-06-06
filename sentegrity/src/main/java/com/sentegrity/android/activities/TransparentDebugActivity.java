package com.sentegrity.android.activities;

import android.os.Bundle;

import com.sentegrity.android.R;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.startup.SentegrityTransparentAuthObject;

import java.util.List;

/**
 * Created by dmestrov on 22/05/16.
 */
public class TransparentDebugActivity extends DebugActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected String getActivityTitle() {
        return "Transparent auth debug";
    }

    @Override
    protected String getDebugInfo() {
        String complete = "";

        String systemTrustFactorsTriggered = "\nTrustFactors Eligible\n+++++++++++++++++++++++++++\n";

        for(SentegrityTrustFactorOutput output : computationResult.getTransparentAuthenticationTrustFactorOutputs()){
            String currentAssertions = "";

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
                    + "Current Assertions: " + "\n" + currentAssertions;
        }

        complete += systemTrustFactorsTriggered;

        String transparentAuthKey = "\nCandidate Auth Key\n+++++++++++++++++++++++++++\n";
        transparentAuthKey += "--Name:\n" + computationResult.getCandidateTransparentKeyHashString() + "\n\n";

        complete += transparentAuthKey;

        String transparentAuthMatch = "\nFound Matching Key\n+++++++++++++++++++++++++++\n";
        transparentAuthMatch += computationResult.isFoundTransparentMatch() + "\n\n";

        complete += transparentAuthMatch;

        String storedTransparentAuthKeys = "\nStored Transaprent Auth Keys\n+++++++++++++++++++++++++++\n";

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        List<SentegrityTransparentAuthObject> storedTransparentAuthObjects = startup.getTransparentAuthKeyObjects();

        for(SentegrityTransparentAuthObject storedTransparentAuthObject : storedTransparentAuthObjects){
            storedTransparentAuthKeys += "AuthKeyHash: " + "\n" + storedTransparentAuthObject.getTransparentKeyPBKDF2HashString() + "\n"
                    + "HitCount: " + storedTransparentAuthObject.getHitCount() + "\n"
                    + "DecayMetric: " + storedTransparentAuthObject.getDecayMetric() + "\n"
                    + "LastTime: " + storedTransparentAuthObject.getLastTime() + "\n\n";
        }

        complete += storedTransparentAuthKeys;

        return complete;
    }
}
