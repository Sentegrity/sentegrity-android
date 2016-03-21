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


}
