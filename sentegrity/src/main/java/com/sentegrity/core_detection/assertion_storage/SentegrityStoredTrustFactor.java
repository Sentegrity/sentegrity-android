package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStoredTrustFactor implements Serializable {

    @SerializedName("factorID")
    private int factorID;

    @SerializedName("revision")
    private int revision;

    @SerializedName("firstRun")
    private long firstRun;

    @SerializedName("decayMetric")
    private double decayMetric;

    @SerializedName("learned")
    private boolean learned;

    @SerializedName("runCount")
    private int runCount;

    @SerializedName("assertionObjects")
    private List<SentegrityStoredAssertion> assertions;

    public int getFactorID() {
        return factorID;
    }

    public int getRevision() {
        return revision;
    }

    public long getFirstRun() {
        return firstRun;
    }

    public double getDecayMetric() {
        return decayMetric;
    }

    public boolean isLearned() {
        return learned;
    }

    public int getRunCount() {
        return runCount;
    }

    public List<SentegrityStoredAssertion> getAssertions() {
        return assertions != null ? assertions : new ArrayList<SentegrityStoredAssertion>();
    }

    public void setFactorID(int factorID) {
        this.factorID = factorID;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public void setFirstRun(long firstRun) {
        this.firstRun = firstRun;
    }

    public void setDecayMetric(double decayMetric) {
        this.decayMetric = decayMetric;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public void setAssertions(List<SentegrityStoredAssertion> assertions) {
        this.assertions = assertions;
    }
}
