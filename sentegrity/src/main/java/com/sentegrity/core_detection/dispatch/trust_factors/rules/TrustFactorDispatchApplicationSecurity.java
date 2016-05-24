package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
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

    private final static long MILLISECONDS_IN_DAY = 86400000;

    public static SentegrityTrustFactorOutput trustLookScan(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getTrustLookBadPkgListDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getTrustLookBadPkgListDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getTrustLookBadPkgListDNEStatus());
            return output;
        }

        List<AppInfo> list = SentegrityTrustFactorDatasets.getInstance().getTrustLookBadPkgList();

        if (SentegrityTrustFactorDatasets.getInstance().getTrustLookBadPkgListDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getTrustLookBadPkgListDNEStatus());
            return output;
        }

        if (list == null) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        for(AppInfo appInfo : list){
            outputList.add(appInfo.getPackageName() + "_" + appInfo.getMd5());
        }

        output.setOutput(outputList);

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

    //we'll skip this rule
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
