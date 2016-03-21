package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStoredTrustFactor implements Serializable {

    @SerializedName("factorID")
    private int factorID;

    @SerializedName("revision")
    private int revision;

    @SerializedName("firstRun")
    private long firstRun;

    @SerializedName("decayMetric")
    private double decayMetric;

    @SerializedName("learned")
    private boolean learned;

    @SerializedName("runCount")
    private int runCount;

    @SerializedName("assertionObjects")
    private List<SentegrityStoredAssertion> assertions;
}
