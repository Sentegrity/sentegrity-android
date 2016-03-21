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
}