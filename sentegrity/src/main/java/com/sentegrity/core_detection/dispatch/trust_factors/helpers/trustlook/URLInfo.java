package com.sentegrity.core_detection.dispatch.trust_factors.helpers.trustlook;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmestrov on 01/06/16.
 */
public class URLInfo {

    public static int UNKNOWN = -1;
    public static int OK = 1;
    public static int MALWARE = 2;

    @SerializedName("url")
    private String url;

    @SerializedName("state")
    private int state;

    public String getUrl() {
        return url;
    }

    public int getState() {
        return state;
    }
}
