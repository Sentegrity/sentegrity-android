package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityTrustFactor implements Serializable {

    @SerializedName("id")
    private int ID;

    @SerializedName("notFoundIssueMessage")
    private String notFoundIssueMessage;

    @SerializedName("notFoundSuggestionMessage")
    private String notFoundSuggestionMessage;

    @SerializedName("lowConfidenceIssueMessage")
    private String lowConfidenceIssueMessage;

    @SerializedName("lowConfidenceSuggestionMessage")
    private String lowConfidenceSuggestionMessage;

    @SerializedName("revision")
    private int revision;

    @SerializedName("classID")
    private int classificationID;

    @SerializedName("subClassID")
    private int subclassificationID;

    @SerializedName("name")
    private String name;

    @SerializedName("partialWeight")
    private int partialWeight;

    @SerializedName("weight")
    private int weight;

    @SerializedName("learnMode")
    private int learnMode;

    @SerializedName("learnTime")
    private int learnTime;

    @SerializedName("learnAssertionCount")
    private int learnAssertionCount;

    @SerializedName("learnRunCount")
    private int learnRunCount;

    @SerializedName("decayMode")
    private int decayMode;

    @SerializedName("decayMetric")
    private int decayMetric;

    @SerializedName("wipeOnUpdate")
    private int wipeOnUpdate;

    @SerializedName("dispatch")
    private String dispatch;

    @SerializedName("implementation")
    private String implementation;

    @SerializedName("whitelistable")
    private int whitelistable;

    @SerializedName("privateAPI")
    private int privateAPI;

    @SerializedName("payload")
    private List<String> payload;

}
