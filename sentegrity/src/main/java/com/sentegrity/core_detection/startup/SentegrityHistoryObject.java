package com.sentegrity.core_detection.startup;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityHistoryObject implements Serializable {

    @SerializedName("preAuthenticationAction")
    private int preAuthenticationAction;

    @SerializedName("postAuthenticationAction")
    private int postAuthenticationAction;

    @SerializedName("coreDetectionResult")
    private int coreDetectionResult;

    @SerializedName("authenticationResult")
    private int authenticationResult;

    @SerializedName("deviceScore")
    private int deviceScore;

    @SerializedName("trustScore")
    private int trustScore;

    @SerializedName("systemIssues")
    private List<String> systemIssues;

    @SerializedName("userIssues")
    private List<String> userIssues;

    @SerializedName("userAnalysisResults")
    private List<String> userAnalysisResults;

    @SerializedName("systemAnalysisResults")
    private List<String> systemAnalysisResults;

    @SerializedName("userScore")
    private int userScore;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("protectModeAction")
    private int protectModeAction;

    public void setPreAuthenticationAction(int preAuthenticationAction) {
        this.preAuthenticationAction = preAuthenticationAction;
    }

    public void setPostAuthenticationAction(int postAuthenticationAction) {
        this.postAuthenticationAction = postAuthenticationAction;
    }

    public void setCoreDetectionResult(int coreDetectionResult) {
        this.coreDetectionResult = coreDetectionResult;
    }

    public void setAuthenticationResult(int authenticationResult) {
        this.authenticationResult = authenticationResult;
    }

    public void setDeviceScore(int deviceScore) {
        this.deviceScore = deviceScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public void setSystemIssues(List<String> systemIssues) {
        this.systemIssues = systemIssues;
    }

    public void setUserIssues(List<String> userIssues) {
        this.userIssues = userIssues;
    }

    public void setUserAnalysisResults(List<String> userAnalysisResults) {
        this.userAnalysisResults = userAnalysisResults;
    }

    public void setSystemAnalysisResults(List<String> systemAnalysisResults) {
        this.systemAnalysisResults = systemAnalysisResults;
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
}