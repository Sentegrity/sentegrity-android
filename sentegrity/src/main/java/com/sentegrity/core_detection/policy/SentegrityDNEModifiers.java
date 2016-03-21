package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityDNEModifiers implements Serializable {

    @SerializedName("unauthorized")
    private float unauthorized;

    @SerializedName("unsupported")
    private float unsupported;

    @SerializedName("unavailable")
    private float unavailable;

    @SerializedName("disabled")
    private float disabled;

    @SerializedName("noData")
    private float noData;

    @SerializedName("expired")
    private float expired;

    @SerializedName("error")
    private float error;

    @SerializedName("invalid")
    private float invalid;

}
