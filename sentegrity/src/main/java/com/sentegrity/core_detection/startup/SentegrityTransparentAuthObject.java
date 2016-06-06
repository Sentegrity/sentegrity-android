package com.sentegrity.core_detection.startup;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityTransparentAuthObject {

    @SerializedName("transparentKeyPBKDF2HashString")
    private String transparentKeyPBKDF2HashString;

    @SerializedName("transparentKeyEncryptedMasterKeyBlobString")
    private String transparentKeyEncryptedMasterKeyBlobString;

    @SerializedName("transparentKeyEncryptedMasterKeySaltString")
    private String transparentKeyEncryptedMasterKeySaltString;

    @SerializedName("hitCount")
    private int hitCount;

    @SerializedName("lastTime")
    private long lastTime;

    @SerializedName("created")
    private long created;

    @SerializedName("decayMetric")
    private double decayMetric;

    public String getTransparentKeyPBKDF2HashString() {
        return transparentKeyPBKDF2HashString;
    }

    public void setTransparentKeyPBKDF2HashString(String transparentKeyPBKDF2HashString) {
        this.transparentKeyPBKDF2HashString = transparentKeyPBKDF2HashString;
    }

    public String getTransparentKeyEncryptedMasterKeyBlobString() {
        return transparentKeyEncryptedMasterKeyBlobString;
    }

    public void setTransparentKeyEncryptedMasterKeyBlobString(String transparentKeyEncryptedMasterKeyBlobString) {
        this.transparentKeyEncryptedMasterKeyBlobString = transparentKeyEncryptedMasterKeyBlobString;
    }

    public String getTransparentKeyEncryptedMasterKeySaltString() {
        return transparentKeyEncryptedMasterKeySaltString;
    }

    public void setTransparentKeyEncryptedMasterKeySaltString(String transparentKeyEncryptedMasterKeySaltString) {
        this.transparentKeyEncryptedMasterKeySaltString = transparentKeyEncryptedMasterKeySaltString;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public double getDecayMetric() {
        return decayMetric;
    }

    public void setDecayMetric(double decayMetric) {
        this.decayMetric = decayMetric;
    }
}
