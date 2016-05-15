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

        long current = System.currentTimeMillis();

        if (SentegrityTrustFactorDatasets.getInstance().getPkgListDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getPkgListDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPkgListDNEStatus());
            return output;
        }

        List<PkgInfo> list = SentegrityTrustFactorDatasets.getInstance().getPkgInfoList();

        if (SentegrityTrustFactorDatasets.getInstance().getPkgListDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPkgListDNEStatus());
            return output;
        }

        if (list == null || list.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        ScanResult scanResult = null;
        long lastOnlineCheck = 0;

        int onlineCheckDays = (int) (double) ((LinkedTreeMap) payload.get(0)).get("onlineCheckDays");

        SharedPreferences sp = SentegrityTrustFactorDatasets.getInstance().getSharedPrefs();

        lastOnlineCheck = sp.getLong("lastOnlineCheck", 0);

        //TODO: on online check we should only check for new apps, not all
        if((System.currentTimeMillis() - lastOnlineCheck) > (onlineCheckDays * MILLISECONDS_IN_DAY)) {
            //we should run online check now, since there's been more than "onlineCheckDays" days from last online check
            scanResult = SentegrityTrustFactorDatasets.getInstance().getCloudScanClient().cloudScan(list);
            sp.edit().putLong("lastOnlineCheck", System.currentTimeMillis()).apply();
        }else{
            //still not enough time, only do local scan
            scanResult = SentegrityTrustFactorDatasets.getInstance().getCloudScanClient().cacheCheck(list);
        }

        if (scanResult.isSuccess()) {
            List<AppInfo> appInfoList;
            appInfoList = scanResult.getList();
            for (AppInfo appInfo : appInfoList) {
                if (appInfo.getScore() >= 8) {
                    //malware app
                    outputList.add(appInfo.getPackageName());
                } else if (appInfo.getScore() == 7) {
                    //high risk app
                    outputList.add(appInfo.getPackageName());
                } else if (appInfo.getScore() == 6) {
                    //non­aggressive risk app, we should skip these ?
                    //outputList.add(appInfo.getPackageName());
                } else {
                    //if score is in [0,5]
                    //the app is safe
                }
            }
        } else {
            //TODO: handle different error codes (maybe UNAVAILABLE if no internet?)
            int errorCode = scanResult.getError();

            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        output.setOutput(outputList);

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
