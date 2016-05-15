package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.trustlook.sdk.cloudscan.ScanResult;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.PkgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchApplicationSecurity {

    public static SentegrityTrustFactorOutput trustLookScan(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        long current = System.currentTimeMillis();
        List<PkgInfo> list = SentegrityTrustFactorDatasets.getInstance().getPkgInfoList();

        ScanResult scanResult = SentegrityTrustFactorDatasets.getInstance().getCloudScanClient().cacheCheck(list);
        //ScanResult scanResult = SentegrityTrustFactorDatasets.getInstance().getCloudScanClient().cloudScan(list);
        if (scanResult.isSuccess()) {
            List<AppInfo> appInfoList;
            appInfoList = scanResult.getList();
            for (AppInfo appInfo : appInfoList) {
                if (appInfo.getScore() >= 8) {
                    //malware app
                    String category = appInfo.getCategory();
                    String virusName = appInfo.getVirusNameInCloud();
                } else if (appInfo.getScore() == 7) {
                    //high risk app
                    String category = appInfo.getCategory();
                    String virusName = appInfo.getVirusNameInCloud();
                } else if (appInfo.getScore() == 6) {
                    //non­aggressive risk app, we should skip these ?
                    String category = appInfo.getCategory();
                    String virusName = appInfo.getVirusNameInCloud();
                } else {
                    //if score is in [0,5]
                    //the app is safe
                }
            }
        } else {
            //TODO: handle error code (if any)
            int errorCode = scanResult.getError();
        }

        Log.d("trustLook", "time: " + (System.currentTimeMillis() - current));
        return output;
    }

    public static SentegrityTrustFactorOutput malwarePackageName(List<Object> payload) {
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

        return output;
    }

    //TODO: needs to be added to policy
    //this rule check for high risk apps, those aren't big threat to system itself, they are just "annoying" apps
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

        return output;
    }
}
