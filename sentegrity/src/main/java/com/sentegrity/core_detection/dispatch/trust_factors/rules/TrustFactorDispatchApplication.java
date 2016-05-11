package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.cloudscan.LegitResult;
import com.trustlook.sdk.cloudscan.ScanResult;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.AppLegit;
import com.trustlook.sdk.data.PkgInfo;
import com.trustlook.sdk.data.Region;

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

        /*List<String> outputList = new ArrayList<>();

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

        output.setOutput(outputList);*/

        long current = System.currentTimeMillis();
        CloudScanClient cloudScanClient = new CloudScanClient.Builder().setContext(SentegrityTrustFactorDatasets.getInstance().context)
                .setToken("0623f7917a1e2e09e7bcc700482392fba620e6a2a29200fbc6a92198")
                .setRegion(Region.INTL)
                .setConnectionTimeout(6000)
                .setSocketTimeout(6500)
                .build();

        List<PkgInfo> pkgInfoList = new ArrayList<PkgInfo>();
        List<PackageInfo> packageInfoList = getLocalAppsPkgInfo(SentegrityTrustFactorDatasets.getInstance().context);
        for (PackageInfo pi : packageInfoList) {
            if (pi != null && pi.applicationInfo != null) {

                PkgInfo pkgInfo = cloudScanClient.populatePkgInfo(pi.packageName, pi.applicationInfo.publicSourceDir);
                //String md5 = pkgInfo.getMd5();
                //String pkgName = pkgInfo.getPkgName();
                //String pkgPath = pkgInfo.getPkgPath();
                //long pkgSize = pkgInfo.getPkgSize();
                pkgInfoList.add(pkgInfo);
            }

        }

        Log.d("trustlook", "time1: " + (System.currentTimeMillis() - current));
        //LegitResult scanResult = cloudScanClient.LegitScan(pkgInfoList);
        ScanResult cloudScan = cloudScanClient.cloudScan(pkgInfoList);
        ScanResult cache = cloudScanClient.cacheCheck(pkgInfoList);
        /*if (scanResult.isSuccess()) {
            List<AppInfo> appInfoList;
            appInfoList = scanResult.getList();
            for (AppInfo
                    appInfo : appInfoList) {
                if (appInfo.getScore() >= 8) {
                    String category = appInfo.getCategory();
                    String virusName = appInfo.getVirusNameInCloud();
                } else if (appInfo.getScore() == 7) {
                    String category = appInfo.getCategory();
                    String virusName = appInfo.getVirusNameInCloud();
                } else if (appInfo.getScore() == 6) { //ifscore==6,it’sanon­aggressiveriskapp, //retrievecategoryandnam
                    String category = appInfo.getCategory();
                    String virusName = appInfo.getVirusNameInCloud();
                } else {  //ifscoreisin[0,5],theappissafe
                }
            }
        } else {
            int errorCode = scanResult.getError();
        }*/

        Log.d("trustlook", "time: " + (System.currentTimeMillis() - current));

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


    public static List<PackageInfo> getLocalAppsPkgInfo(Context context) {
        final int MAX_ATTEMPTS = 3;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try {
                List<PackageInfo> pkgInfoList = context.getPackageManager().getInstalledPackages(
                        PackageManager.GET_PERMISSIONS | PackageManager.GET_PROVIDERS);

                Log.d(Constants.TAG, "=> Total installed packages: " + pkgInfoList.size());
                return pkgInfoList;
            } catch (RuntimeException re) {

                // Just wait for cooling down
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            }
        }
        return new ArrayList<PackageInfo>();
    }
}
