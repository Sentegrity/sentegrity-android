package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStoredAssertion implements Serializable {

    @SerializedName("assertionHash")
    private String hash;

    @SerializedName("hitCount")
    private int hitCount;

    @SerializedName("lastTime")
    private long lastTime;

    @SerializedName("created")
    private long created;

    @SerializedName("decayMetric")
    private double decayMetric;
}
