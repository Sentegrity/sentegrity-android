package com.sentegrity.core_detection.policy;

import android.text.TextUtils;

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

    @SerializedName("dnePenalty")
    private int dnePenalty;

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
    private float decayMetric;

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
    private List<Object> payload;

    public int getID() {
        return ID;
    }

    public String getNotFoundIssueMessage() {
        return notFoundIssueMessage;
    }

    public String getNotFoundSuggestionMessage() {
        return notFoundSuggestionMessage;
    }

    public String getLowConfidenceIssueMessage() {
        return lowConfidenceIssueMessage;
    }

    public String getLowConfidenceSuggestionMessage() {
        return lowConfidenceSuggestionMessage;
    }

    public int getRevision() {
        return revision;
    }

    public int getClassificationID() {
        return classificationID;
    }

    public int getSubclassificationID() {
        return subclassificationID;
    }

    public String getName() {
        return name;
    }

    public int getPartialWeight() {
        return partialWeight;
    }

    public int getWeight() {
        return weight;
    }

    public int getDnePenalty() {
        return dnePenalty;
    }

    public int getLearnMode() {
        return learnMode;
    }

    public int getLearnTime() {
        return learnTime;
    }

    public int getLearnAssertionCount() {
        return learnAssertionCount;
    }

    public int getLearnRunCount() {
        return learnRunCount;
    }

    public int getDecayMode() {
        return decayMode;
    }

    public float getDecayMetric() {
        return decayMetric;
    }

    public int getWipeOnUpdate() {
        return wipeOnUpdate;
    }

    public String getDispatch() {
        return dispatch;
    }

    public String getImplementation() {
        return implementation;
    }

    public int getWhitelistable() {
        return whitelistable;
    }

    public int getPrivateAPI() {
        return privateAPI;
    }

    public List<Object> getPayload() {
        return payload;
    }

    public String generateClassName(String packageName){
        return packageName + dispatch;
    }

    public String generateImplementation(){
        if(TextUtils.isEmpty(implementation)) return implementation;
        if(implementation.endsWith(":")) return implementation.substring(0, implementation.length() - 1);
        return implementation;
    }
}
