package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;

import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityTrustFactorOutput {

    @SerializedName("trustFactor")
    private SentegrityTrustFactor trustFactor;

    @SerializedName("storedTrustFactor")
    private SentegrityStoredTrustFactor storedTrustFactor;

    @SerializedName("output")
    private List<String> output;

    @SerializedName("storedAssertionObjectsMatched")
    private List<SentegrityStoredAssertion> storedAssertionObjectsMatched;

    @SerializedName("candidateAssertionObjectsForWhitelisting")
    private List<SentegrityStoredAssertion> candidateAssertionObjectsForWhitelisting;

    @SerializedName("candidateAssertionObjects")
    private List<SentegrityStoredAssertion> candidateAssertionObjects;

    @SerializedName("statusCode")
    private DNEStatusCode statusCode;

    @SerializedName("matchFound")
    private boolean matchFound;

    @SerializedName("forComputation")
    private boolean forComputation;

    @SerializedName("whiteList")
    private boolean whiteList;

    @SerializedName("appliedWeight")
    private int appliedWeight;

    @SerializedName("percentAppliedWeight")
    private double percentAppliedWeight;

    public void setAsertionObjectsFromOutputWithDeviceSalt(String deviceSalt){

    }

    public SentegrityTrustFactorOutput(){
        statusCode = DNEStatusCode.OK;
    }

    public void setTrustFactor(SentegrityTrustFactor trustFactor) {
        this.trustFactor = trustFactor;
    }

    public void setStoredTrustFactor(SentegrityStoredTrustFactor storedTrustFactor) {
        this.storedTrustFactor = storedTrustFactor;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    public void setStoredAssertionObjectsMatched(List<SentegrityStoredAssertion> storedAssertionObjectsMatched) {
        this.storedAssertionObjectsMatched = storedAssertionObjectsMatched;
    }

    public void setCandidateAssertionObjectsForWhitelisting(List<SentegrityStoredAssertion> candidateAssertionObjectsForWhitelisting) {
        this.candidateAssertionObjectsForWhitelisting = candidateAssertionObjectsForWhitelisting;
    }

    public void setCandidateAssertionObjects(List<SentegrityStoredAssertion> candidateAssertionObjects) {
        this.candidateAssertionObjects = candidateAssertionObjects;
    }

    public void setStatusCode(DNEStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public void setMatchFound(boolean matchFound) {
        this.matchFound = matchFound;
    }

    public void setForComputation(boolean forComputation) {
        this.forComputation = forComputation;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    public void setAppliedWeight(int appliedWeight) {
        this.appliedWeight = appliedWeight;
    }

    public void setPercentAppliedWeight(double percentAppliedWeight) {
        this.percentAppliedWeight = percentAppliedWeight;
    }

    public SentegrityTrustFactor getTrustFactor() {
        return trustFactor;
    }

    public SentegrityStoredTrustFactor getStoredTrustFactor() {
        return storedTrustFactor;
    }

    public List<String> getOutput() {
        return output;
    }

    public List<SentegrityStoredAssertion> getStoredAssertionObjectsMatched() {
        return storedAssertionObjectsMatched;
    }

    public List<SentegrityStoredAssertion> getCandidateAssertionObjectsForWhitelisting() {
        return candidateAssertionObjectsForWhitelisting;
    }

    public List<SentegrityStoredAssertion> getCandidateAssertionObjects() {
        return candidateAssertionObjects;
    }

    public DNEStatusCode getStatusCode() {
        return statusCode;
    }

    public boolean isMatchFound() {
        return matchFound;
    }

    public boolean isForComputation() {
        return forComputation;
    }

    public boolean isWhiteList() {
        return whiteList;
    }

    public int getAppliedWeight() {
        return appliedWeight;
    }

    public double getPercentAppliedWeight() {
        return percentAppliedWeight;
    }
}
