package com.sentegrity.core_detection.constants;

import android.content.Context;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityConstants {

    public static final String UNIQUE_DEVICE_ID = "1234567890";

    public static final String STARTUP_FILE_NAME = "startup";

    public static final String STORE_FILETYPE = ".store";

    public static final String ASSERTION_STORE_FILE_NAME = "store";

    //public static final String CORE_DETECTION_BUNDLE = "PolicyBundle";
    //public static final String CORE_DETECTION_BUNDLE_EXTENSTION = "bundle";
    public static final String CORE_DETECTION_POLICY_FILE_NAME = "policy";


    public static final String HOTSPOT_SSID_LIST_FILE_NAME = "hotspot_ssids.list";
    public static final String OUI_LIST_FILE_NAME = "oui.list";
    public static final String DEFAULT_SSID_LIST_FILE_NAME = "default_ssids.list";
    public static final String MALICIOUS_APP_LIST_FILE_NAME = "maliciousapp.list";
    public static final String HIGH_RISK_APP_LIST_FILE_NAME = "highriskapp.list";
    public static final String VULNERABLE_PLATFORM_LIST_FILE_NAME = "vulnerable_platform.list";
    public static final String PATCH_VERSION_LIST_FILE_NAME = "patch_version.list";

    //pulic static final String APK_SIGNATURE = "2da2e47fc43442cfe3109a8a71885912";
    public static final String DEVICE_SALT_DEFAULT = "821057bac664ccbe58338360c0cd1c78";
    public static final String USER_SALT_DEFAULT = "a4a8eeb22881b5ccce006f60bb6f8aab";
    public static final String TRUSTLOOK_CLIENT_ID = "0623f7917a1e2e09e7bcc700482392fba620e6a2a29200fbc6a92198";

    public static final int allowPrivateAPIs = 1;

    public static final String baseURL = "https://cloud.sentegrity.com/app_dev.php/";

    public static final int BLUETOOTH_SEARCH_TIME = 5000;

    public static final String SHARED_PREFS_NAME = "prefs";
    public static final int SHARED_PREFS_MODE = Context.MODE_PRIVATE;
}
