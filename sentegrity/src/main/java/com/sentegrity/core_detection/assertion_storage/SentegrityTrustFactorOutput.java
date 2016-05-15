package com.sentegrity.core_detection.assertion_storage;

import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;
import com.sentegrity.core_detection.utilities.Helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityTrustFactorOutput {

    private SentegrityTrustFactor trustFactor;
    private SentegrityStoredTrustFactor storedTrustFactor;

    private List<String> output;

    private List<SentegrityStoredAssertion> storedAssertionObjectsMatched;
    private List<SentegrityStoredAssertion> candidateAssertionObjectsForWhitelisting;
    private List<SentegrityStoredAssertion> candidateAssertionObjects;

    private int statusCode;

    private boolean matchFound;
    private boolean forComputation;
    private boolean whiteList;

    private int appliedWeight;
    private double percentAppliedWeight;

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

    public void setStatusCode(int statusCode) {
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
        return output != null ? output : new ArrayList<String>();
    }

    public List<SentegrityStoredAssertion> getStoredAssertionObjectsMatched() {
        return storedAssertionObjectsMatched != null ? storedAssertionObjectsMatched : new ArrayList<SentegrityStoredAssertion>();
    }

    public List<SentegrityStoredAssertion> getCandidateAssertionObjectsForWhitelisting() {
        return candidateAssertionObjectsForWhitelisting;
    }

    public List<SentegrityStoredAssertion> getCandidateAssertionObjects() {
        return candidateAssertionObjects != null ? candidateAssertionObjects : new ArrayList<SentegrityStoredAssertion>();
    }

    public int getStatusCode() {
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


    public void setAssertionObjectsFromOutputWithDeviceSalt(String deviceSalt){
        List<SentegrityStoredAssertion> assertions = new ArrayList<>();

        for(String trustFactorOutput : getOutput()){
            SentegrityStoredAssertion assertion = new SentegrityStoredAssertion();

            String hash = Helpers.getSHA1Hash(getTrustFactor().getID() + "1234567890" + deviceSalt + trustFactorOutput) + "-" + trustFactorOutput;

            assertion.setHash(hash);
            assertion.setLastTime(System.currentTimeMillis());
            assertion.setHitCount(1);
            assertion.setCreated(System.currentTimeMillis());
            assertion.setDecayMetric(1.0);

            assertions.add(assertion);
        }

        setCandidateAssertionObjects(assertions);
    }

}
