package com.sentegrity.core_detection.networking;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.startup.SentegrityHistoryObject;

import java.util.List;

/**
 * Created by dmestrov on 18/07/16.
 */
public class SentegrityUploadRequest {

    @SerializedName("user_activation_id")
    private String email;

    @SerializedName("device_salt")
    private String deviceSalt;

    @SerializedName("platform")
    private int platform;

    @SerializedName("current_policy_id")
    private String policyID;

    @SerializedName("current_policy_revision")
    private String policyRevision;

    @SerializedName("run_history_objects")
    private List<SentegrityHistoryObject> runHistoryObjects;

    @SerializedName("app_version")
    private String applicationVersionID;

    @SerializedName("phone_model")
    private String deviceName;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDeviceSalt(String deviceSalt) {
        this.deviceSalt = deviceSalt;
    }

    public void setPolicyID(String policyID) {
        this.policyID = policyID;
    }

    public void setPolicyRevision(String policyRevision) {
        this.policyRevision = policyRevision;
    }

    public void setRunHistoryObjects(List<SentegrityHistoryObject> runHistoryObjects) {
        this.runHistoryObjects = runHistoryObjects;
    }

    public void setApplicationVersionID(String applicationVersionID) {
        this.applicationVersionID = applicationVersionID;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
