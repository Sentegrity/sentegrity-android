package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.pm.ApplicationInfo;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchApplication {

    public static SentegrityTrustFactorOutput installedApp(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        List<ApplicationInfo> userApps = SentegrityTrustFactorDatasets.getInstance().getInstalledAppInfo();

        if (userApps == null || userApps.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        for (ApplicationInfo appInfo : userApps) {

            for (Object badAppName : payload) {

                if (badAppName.equals(appInfo.packageName)) {

                    if (!outputList.contains(appInfo.packageName)) {
                        outputList.add(appInfo.packageName);
                    }
                }
            }
        }

        output.setOutput(outputList);

        return new SentegrityTrustFactorOutput();
    }

    //TODO: needs to be added to policy
    public static SentegrityTrustFactorOutput maliciousApp(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        List<ApplicationInfo> userApps = SentegrityTrustFactorDatasets.getInstance().getInstalledAppInfo();

        List<String> maliciousApps = SentegrityTrustFactorDatasets.getInstance().getMaliciousAppsList();

        if (userApps == null || userApps.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        if (maliciousApps == null || maliciousApps.size() == 0) {
            return output;
        }

        for (ApplicationInfo appInfo : userApps) {

            for (String maliciousAppName : maliciousApps) {

                if (maliciousAppName.equals(appInfo.packageName)) {

                    if (!outputList.contains(appInfo.packageName)) {
                        outputList.add(appInfo.packageName);
                    }
                }
            }
        }

        output.setOutput(outputList);

        return new SentegrityTrustFactorOutput();
    }

    //TODO: needs to be added to policy
    public static SentegrityTrustFactorOutput highRiskApp(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        List<ApplicationInfo> userApps = SentegrityTrustFactorDatasets.getInstance().getInstalledAppInfo();

        List<String> highRiskApps = SentegrityTrustFactorDatasets.getInstance().getHighRiskAppsList();

        if (userApps == null || userApps.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        if (highRiskApps == null || highRiskApps.size() == 0) {
            return output;
        }

        for (ApplicationInfo appInfo : userApps) {

            for (String highRiskAppName : highRiskApps) {

                if (highRiskAppName.equals(appInfo.packageName)) {

                    if (!outputList.contains(appInfo.packageName)) {
                        outputList.add(appInfo.packageName);
                    }
                }
            }
        }

        output.setOutput(outputList);

        return new SentegrityTrustFactorOutput();
    }


}
