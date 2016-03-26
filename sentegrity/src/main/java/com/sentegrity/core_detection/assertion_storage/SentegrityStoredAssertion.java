package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStoredAssertion implements Serializable {

    @SerializedName("assertionHash")
    private String hash;

    @SerializedName("hitCount")
    private int hitCount;

    @SerializedName("lastTime")
    private long lastTime;

    @SerializedName("created")
    private long created;

    @SerializedName("decayMetric")
    private double decayMetric;

    public String getHash() {
        return hash;
    }

    public int getHitCount() {
        return hitCount;
    }

    public long getLastTime() {
        return lastTime;
    }

    public long getCreated() {
        return created;
    }

    public double getDecayMetric() {
        return decayMetric;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setDecayMetric(double decayMetric) {
        this.decayMetric = decayMetric;
    }
}
