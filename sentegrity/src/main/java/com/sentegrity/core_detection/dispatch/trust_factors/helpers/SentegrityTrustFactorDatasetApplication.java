package com.sentegrity.core_detection.dispatch.trust_factors.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.sentegrity.core_detection.dispatch.trust_factors.helpers.application.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 18/04/16.
 */
public class SentegrityTrustFactorDatasetApplication {

    public static List<AppInfo> getUserAppInfo(Context context) {
        List<AppInfo> list = new ArrayList<>();
        final PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            list.add(new AppInfo(packageInfo));
        }
        return list;
    }
}
