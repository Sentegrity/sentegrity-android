package com.sentegrity.core_detection.startup;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityHistory implements Serializable {

    @SerializedName("deviceScore")
    private int deviceScore;

    @SerializedName("trustScore")
    private int trustScore;

    @SerializedName("deviceIssues")
    private List<String> deviceIssues;

    @SerializedName("userScore")
    private int userScore;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("protectModeAction")
    private int protectModeAction;

    @SerializedName("userIssues")
    private List<String> userIssues;

    public void setDeviceScore(int deviceScore) {
        this.deviceScore = deviceScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public void setDeviceIssues(List<String> deviceIssues) {
        this.deviceIssues = deviceIssues;
    }

    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setProtectModeAction(int protectModeAction) {
        this.protectModeAction = protectModeAction;
    }

    public void setUserIssues(List<String> userIssues) {
        this.userIssues = userIssues;
    }

    public int getDeviceScore() {
        return deviceScore;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public List<String> getDeviceIssues() {
        return deviceIssues;
    }

    public int getUserScore() {
        return userScore;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getProtectModeAction() {
        return protectModeAction;
    }

    public List<String> getUserIssues() {
        return userIssues;
    }
}