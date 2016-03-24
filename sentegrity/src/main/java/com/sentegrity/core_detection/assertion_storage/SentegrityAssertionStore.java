package com.sentegrity.core_detection.assertion_storage;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;

import java.util.List;

/**
 * Created by dmestrov on 22/03/16.
 */
public class SentegrityAssertionStore {

    @SerializedName("appid")
    private String appId;

    @SerializedName("storedTrustfactorObject")
    private List<SentegrityTrustFactor> trustFactors;

    public List<SentegrityTrustFactor> getTrustFactors() {
        return trustFactors;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setTrustFactors(List<SentegrityTrustFactor> trustFactors) {
        this.trustFactors = trustFactors;
    }

    private static SentegrityAssertionStore sInstance;

    final private Context context;

    public SentegrityAssertionStore(Context context) {
        this.context = context;
    }

    public static synchronized void initialize(Context context){
        sInstance = new SentegrityAssertionStore(context);
    }

    public static SentegrityAssertionStore getInstance(){
        if(sInstance == null || sInstance.context == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }
}
