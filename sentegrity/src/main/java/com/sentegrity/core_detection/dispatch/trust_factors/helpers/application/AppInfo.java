package com.sentegrity.core_detection.dispatch.trust_factors.helpers.application;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

/**
 * Created by dmestrov on 18/04/16.
 */
public class AppInfo {

    public String packageName;
    public String name;
    public String versionName;
    public int versionCode;
    public boolean isSystem;

    public AppInfo(PackageInfo packageInfo){
        this.packageName = packageInfo.packageName;
        this.versionName = packageInfo.versionName;
        this.versionCode = packageInfo.versionCode;
        this.isSystem = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        //this.name = packageInfo.applicationInfo.loadLabel(packageManager);
    }
}
