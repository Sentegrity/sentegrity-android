package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityPolicy implements Serializable {

    @SerializedName("policyID")
    private int policyID;

    @SerializedName("appID")
    private String appID;

    @SerializedName("revision")
    private int revision;

    @SerializedName("userThreshold")
    private int userThreshold;

    @SerializedName("systemThreshold")
    private int systemThreshold;

    @SerializedName("timeout")
    private int timeout;

    @SerializedName("contactPhone")
    private String contactPhone;

    @SerializedName("contactURL")
    private String contactURL;

    @SerializedName("contactEmail")
    private String contactEmail;

    @SerializedName("DNEModifiers")
    private SentegrityDNEModifiers dneModifiers;

    @SerializedName("classifications")
    private List<SentegrityClassification> classifications;

    @SerializedName("subclassifications")
    private List<SentegritySubclassification> subclassifications;

    @SerializedName("trustFactors")
    private List<SentegrityTrustFactor> trustFactors;

    public int getPolicyID() {
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

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactURL() {
        return contactURL;
    }

    public String getContactEmail() {
        return contactEmail;
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
}
