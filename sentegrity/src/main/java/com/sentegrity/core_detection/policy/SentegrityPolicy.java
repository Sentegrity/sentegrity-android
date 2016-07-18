package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityPolicy implements Serializable {

    @SerializedName("policyID")
    private String policyID;

    @SerializedName("applicationVersionID")
    private String appID;

    @SerializedName("platform")
    private String platform;

    @SerializedName("revision")
    private int revision;

    @SerializedName("userThreshold")
    private int userThreshold;

    @SerializedName("systemThreshold")
    private int systemThreshold;

    @SerializedName("timeout")
    private int timeout;

    @SerializedName("DNEModifiers")
    private SentegrityDNEModifiers dneModifiers;

    @SerializedName("classifications")
    private List<SentegrityClassification> classifications;

    @SerializedName("subclassifications")
    private List<SentegritySubclassification> subclassifications;

    @SerializedName("trustFactors")
    private List<SentegrityTrustFactor> trustFactors;


    @SerializedName("transparentAuthDecayMetric")
    private float transparentAuthDecayMetric;

    @SerializedName("transparentAuthEnabled")
    private int transparentAuthEnabled;

    @SerializedName("minimumTransparentAuthEntropy")
    private int minimumTransparentAuthEntropy;

    @SerializedName("continueOnError")
    private int continueOnError;

    @SerializedName("allowPrivateAPIs")
    private int allowPrivateAPIs;

    @SerializedName("statusUploadRunFrequency")
    private int statusUploadRunFrequency;

    @SerializedName("statusUploadTimeFrequency")
    private int statusUploadTimeFrequency;

    @SerializedName("passwordRequirements")
    private SentegrityPasswordRequirements passwordRequirements;


    public String getPolicyID() {
        return policyID;
    }

    public String getAppID() {
        return appID;
    }

    public int getRevision() {
        return revision;
    }

    public int getUserThreshold() {
        return userThreshold;
    }

    public int getSystemThreshold() {
        return systemThreshold;
    }

    public int getTimeout() {
        return timeout;
    }

    public SentegrityDNEModifiers getDneModifiers() {
        return dneModifiers;
    }

    public List<SentegrityClassification> getClassifications() {
        return classifications;
    }

    public List<SentegritySubclassification> getSubclassifications() {
        return subclassifications;
    }

    public List<SentegrityTrustFactor> getTrustFactors() {
        return trustFactors;
    }

    public float getTransparentAuthDecayMetric() {
        return transparentAuthDecayMetric;
    }

    public int getTransparentAuthEnabled() {
        return transparentAuthEnabled;
    }

    public int getMinimumTransparentAuthEntropy() {
        return minimumTransparentAuthEntropy;
    }

    public int getContinueOnError() {
        return continueOnError;
    }

    public boolean continueOnError() {
        return continueOnError == 1;
    }

    public int getAllowPrivateAPIs() {
        return allowPrivateAPIs;
    }

    public int getStatusUploadRunFrequency() {
        return statusUploadRunFrequency;
    }

    public int getStatusUploadTimeFrequency() {
        return statusUploadTimeFrequency;
    }

    public String getPlatform() {
        return platform;
    }

}
