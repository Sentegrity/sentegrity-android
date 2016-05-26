package com.sentegrity.core_detection.networking;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.startup.SentegrityHistoryObject;
import com.sentegrity.core_detection.startup.SentegrityStartup;

import java.util.List;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityNetworkRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("deviceSalt")
    private String deviceSalt;

    @SerializedName("policyID")
    private String policyID;

    @SerializedName("policyRevision")
    private String policyRevision;

    @SerializedName("runHistoryObjects")
    private List<SentegrityHistoryObject> runHistoryObjects;

    public SentegrityNetworkRequest(SentegrityStartup startup, SentegrityPolicy policy){
        if(TextUtils.isEmpty(startup.getEmail()))
            this.email = "";
        else
            this.email = "jason@sentegrity.com";//startup.getEmail();
        this.deviceSalt = startup.getDeviceSaltString();
        this.policyID = policy.getPolicyID();
        this.policyRevision = policy.getRevision() + "";
        this.runHistoryObjects = startup.getRunHistoryObjects();
    }
}
