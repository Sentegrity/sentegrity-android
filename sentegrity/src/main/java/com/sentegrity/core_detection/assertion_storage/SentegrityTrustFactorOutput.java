package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;

import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityTrustFactorOutput {

    @SerializedName("trustFactor")
    private SentegrityTrustFactor trustFactor;

    @SerializedName("storedTrustFactor")
    private SentegrityStoredTrustFactor storedTrustFactor;

    @SerializedName("output")
    private List<String> output;

    @SerializedName("storedAssertionObjectsMatched")
    private List<SentegrityStoredAssertion> storedAssertionObjectsMatched;

    @SerializedName("candidateAssertionObjectsForWhitelisting")
    private List<SentegrityStoredAssertion> candidateAssertionObjectsForWhitelisting;

    @SerializedName("candidateAssertionObjects")
    private List<SentegrityStoredAssertion> candidateAssertionObjects;

    @SerializedName("statusCode")
    private DNEStatusCode statusCode;

    @SerializedName("matchFound")
    private boolean matchFound;

    @SerializedName("forComputation")
    private boolean forComputation;

    @SerializedName("whiteList")
    private boolean whiteList;

    @SerializedName("appliedWeight")
    private int appliedWeight;

    @SerializedName("percentAppliedWeight")
    private double percentAppliedWeight;

    public void setAsertionObjectsFromOutputWithDeviceSalt(String deviceSalt){

    }

    public SentegrityTrustFactorOutput(){
        statusCode = DNEStatusCode.OK;
    }
}
