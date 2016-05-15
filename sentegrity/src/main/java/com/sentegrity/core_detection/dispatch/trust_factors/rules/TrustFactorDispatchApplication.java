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
}
