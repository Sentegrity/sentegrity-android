package com.sentegrity.core_detection.startup;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStartup implements Serializable {

    @SerializedName("deviceSalt")
    private String deviceSalt;

    @SerializedName("coreDetectionChecksum")
    private int coreDetectionChecksum;

    @SerializedName("runHistory")
    private List<SentegrityHistory> runHistory;

    @SerializedName("userSalt")
    private String userSalt;

    @SerializedName("lastState")
    private String lastState;

    @SerializedName("lastOSVersion")
    private String lastOSVersion;
}
