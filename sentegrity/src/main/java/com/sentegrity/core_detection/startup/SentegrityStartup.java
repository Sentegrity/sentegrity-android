package com.sentegrity.core_detection.startup;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStartup implements Serializable {

    @SerializedName("deviceSalt")
    private String deviceSalt;

    @SerializedName("coreDetectionChecksum")
    private int coreDetectionChecksum;

    @SerializedName("runHistory")
    private List<SentegrityHistory> runHistory;

    @SerializedName("userSalt")
    private String userSalt;

    @SerializedName("lastState")
    private String lastState;

    @SerializedName("lastOSVersion")
    private String lastOSVersion;


    @SerializedName("runCount")
    private int runCount;

    @SerializedName("runCountAtLastUpload")
    private int runCountAtLastUpload;

    @SerializedName("timeOfLastUpload")
    private long timeOfLastUpload;

    @SerializedName("transparentAuthGlobalPBKDF2SaltString")
    private String transparentAuthGlobalPBKDF2SaltString;

    @SerializedName("transparentAuthPBKDF2rounds")
    private int transparentAuthPBKDF2rounds;

    @SerializedName("transparentAuthKeyObjects")
    private List<SentegrityTransparentAuthObject> transparentAuthKeyObjects;

    @SerializedName("userKeyPBKDF2rounds")
    private int userKeyPBKDF2rounds;

    @SerializedName("userKeySaltString")
    private String userKeySaltString;

    @SerializedName("userKeyHash")
    private String userKeyHash;

    @SerializedName("userKeyEncryptedMasterKeyBlobString")
    private String userKeyEncryptedMasterKeyBlobString;

    public String getDeviceSalt() {
        return deviceSalt;
    }

    public int getCoreDetectionChecksum() {
        return coreDetectionChecksum;
    }

    public List<SentegrityHistory> getRunHistory() {
        return runHistory;
    }

    public String getUserSalt() {
        return userSalt;
    }

    public String getLastState() {
        return lastState;
    }

    public String getLastOSVersion() {
        return lastOSVersion;
    }

    public void setDeviceSalt(String deviceSalt) {
        this.deviceSalt = deviceSalt;
    }

    public void setCoreDetectionChecksum(int coreDetectionChecksum) {
        this.coreDetectionChecksum = coreDetectionChecksum;
    }

    public void setRunHistory(List<SentegrityHistory> runHistory) {
        this.runHistory = runHistory;
    }

    public void setUserSalt(String userSalt) {
        this.userSalt = userSalt;
    }

    public void setLastState(String lastState) {
        this.lastState = lastState;
    }

    public void setLastOSVersion(String lastOSVersion) {
        this.lastOSVersion = lastOSVersion;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public int getRunCountAtLastUpload() {
        return runCountAtLastUpload;
    }

    public void setRunCountAtLastUpload(int runCountAtLastUpload) {
        this.runCountAtLastUpload = runCountAtLastUpload;
    }

    public long getTimeOfLastUpload() {
        return timeOfLastUpload;
    }

    public void setTimeOfLastUpload(long timeOfLastUpload) {
        this.timeOfLastUpload = timeOfLastUpload;
    }

    public String getTransparentAuthGlobalPBKDF2SaltString() {
        return transparentAuthGlobalPBKDF2SaltString;
    }

    public void setTransparentAuthGlobalPBKDF2SaltString(String transparentAuthGlobalPBKDF2SaltString) {
        this.transparentAuthGlobalPBKDF2SaltString = transparentAuthGlobalPBKDF2SaltString;
    }

    public int getTransparentAuthPBKDF2rounds() {
        return transparentAuthPBKDF2rounds;
    }

    public void setTransparentAuthPBKDF2rounds(int transparentAuthPBKDF2rounds) {
        this.transparentAuthPBKDF2rounds = transparentAuthPBKDF2rounds;
    }

    public List<SentegrityTransparentAuthObject> getTransparentAuthKeyObjects() {
        return transparentAuthKeyObjects;
    }

    public void setTransparentAuthKeyObjects(List<SentegrityTransparentAuthObject> transparentAuthKeyObjects) {
        this.transparentAuthKeyObjects = transparentAuthKeyObjects;
    }

    public int getUserKeyPBKDF2rounds() {
        return userKeyPBKDF2rounds;
    }

    public void setUserKeyPBKDF2rounds(int userKeyPBKDF2rounds) {
        this.userKeyPBKDF2rounds = userKeyPBKDF2rounds;
    }

    public String getUserKeySaltString() {
        return userKeySaltString;
    }

    public void setUserKeySaltString(String userKeySaltString) {
        this.userKeySaltString = userKeySaltString;
    }

    public String getUserKeyHash() {
        return userKeyHash;
    }

    public void setUserKeyHash(String userKeyHash) {
        this.userKeyHash = userKeyHash;
    }

    public String getUserKeyEncryptedMasterKeyBlobString() {
        return userKeyEncryptedMasterKeyBlobString;
    }

    public void setUserKeyEncryptedMasterKeyBlobString(String userKeyEncryptedMasterKeyBlobString) {
        this.userKeyEncryptedMasterKeyBlobString = userKeyEncryptedMasterKeyBlobString;
    }
}
