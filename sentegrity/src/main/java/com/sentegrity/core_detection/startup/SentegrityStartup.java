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

    public String getDeviceSalt() {
        return deviceSalt;
    }

    public int getCoreDetectionChecksum() {
        return coreDetectionChecksum;
    }

    public List<SentegrityHistory> getRunHistory() {
        return runHistory;
    }

    public String getUserSalt() {
        return userSalt;
    }

    public String getLastState() {
        return lastState;
    }

    public String getLastOSVersion() {
        return lastOSVersion;
    }

    public void setDeviceSalt(String deviceSalt) {
        this.deviceSalt = deviceSalt;
    }

    public void setCoreDetectionChecksum(int coreDetectionChecksum) {
        this.coreDetectionChecksum = coreDetectionChecksum;
    }

    public void setRunHistory(List<SentegrityHistory> runHistory) {
        this.runHistory = runHistory;
    }

    public void setUserSalt(String userSalt) {
        this.userSalt = userSalt;
    }

    public void setLastState(String lastState) {
        this.lastState = lastState;
    }

    public void setLastOSVersion(String lastOSVersion) {
        this.lastOSVersion = lastOSVersion;
    }
}
