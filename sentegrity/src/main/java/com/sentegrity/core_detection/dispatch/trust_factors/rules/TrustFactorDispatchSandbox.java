package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.text.TextUtils;
import android.util.Log;

import com.scottyab.rootbeer.RootBeer;
import com.scottyab.rootbeer.RootBeerNative;
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

    public static SentegrityTrustFactorOutput integrity(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String violation = "";

        RootBeer rootBeer = SentegrityTrustFactorDatasets.getInstance().getRootBeer();

        long current = System.currentTimeMillis();
        if (rootBeer.detectRootManagementApps()) {
            violation += "rootManagementApps_";
        }
        Log.d("integrity", "rootManagementApps_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.detectPotentiallyDangerousApps()) {
            violation += "potentiallyDangerousApps_";
        }
        Log.d("integrity", "potentiallyDangerousApps_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.detectRootCloakingApps()) {
            violation += "rootCloackingApps_";
        }
        Log.d("integrity", "rootCloackingApps_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.detectTestKeys()) {
            violation += "testKeys_";
        }
        Log.d("integrity", "testKeys_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.checkForDangerousProps()) {
            violation += "dangerousProps_";
        }
        Log.d("integrity", "dangerousProps_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.checkForBusyBoxBinary()) {
            violation += "busyBoxBinary_";
        }
        Log.d("integrity", "busyBoxBinary_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.checkForSuBinary()) {
            violation += "suBinary_";
        }
        Log.d("integrity", "suBinary_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.checkSuExists()) {
            violation += "suExists_";
        }
        Log.d("integrity", "suExists_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.checkForRWPaths()) {
            violation += "rwPaths_";
        }
        Log.d("integrity", "rwPaths_" + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        if (rootBeer.checkForRootNative()) {
            violation += "rootNative_";
        }
        Log.d("integrity", "rootNative_" + (System.currentTimeMillis() - current));

        if (!TextUtils.isEmpty(violation))
            outputList.add(violation);

        output.setOutput(outputList);

        return output;
    }

    /**
     {
     "id": 42,
     "notFoundIssueMessage": "Sandbox root violation",
     "notFoundSuggestionMessage": "Reinstall operating system",
     "lowConfidenceIssueMessage": "",
     "lowConfidenceSuggestionMessage": "",
     "revision": 1,
     "classID": 1,
     "subClassID": 4,
     "name": "Sandbox integrity compromise",
     "transparentEligible": 0,
     "partialWeight": 0,
     "weight": 100,
     "learnMode": 0,
     "learnTime": 0,
     "learnAssertionCount": 0,
     "learnRunCount": 0,
     "decayMode": 0,
     "decayMetric": 0,
     "wipeOnUpdate": 0,
     "dispatch": "Sandbox",
     "implementation": "rootDetect:",
     "whitelistable": 0,
     "privateAPI": 0,
     "payload": [

     ]
     }
     */

    @Deprecated
    public static SentegrityTrustFactorOutput rootDetect(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String rootmethods = "";

        RootDetection rootDetection = SentegrityTrustFactorDatasets.getInstance().getRootDetection();

        if (rootDetection == null || !rootDetection.hasData()) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        if (rootDetection.isAccessGiven) {
            rootmethods += "appRunningAsRoot_";
        }

        if (rootDetection.isRootAvailable) {
            rootmethods += "superSuFound_";
        }

        if (rootDetection.isBusyBoxAvailable) {
            rootmethods += "busyBoxFound_";
        }

        if (!TextUtils.isEmpty(rootmethods))
            outputList.add(rootmethods);

        output.setOutput(outputList);

        return output;
    }
}
