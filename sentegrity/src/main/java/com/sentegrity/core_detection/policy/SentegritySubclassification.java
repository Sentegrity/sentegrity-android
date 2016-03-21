package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegritySubclassification implements Serializable {

    @SerializedName("id")
    private int ID;

    @SerializedName("name")
    private String type;

    @SerializedName("dneUnauthorized")
    private String dneUnauthorized;

    @SerializedName("dneUnsupported")
    private String dneUnsupported;

    @SerializedName("dneUnavailable")
    private String dneUnavailable;

    @SerializedName("dneDisabled")
    private String dneDisabled;

    @SerializedName("dneNoData")
    private String dneNoData;

    @SerializedName("dneExpired")
    private String dneExpired;

    @SerializedName("dneInvalid")
    private String dneInvalid;

    @SerializedName("weight")
    private int weight;

}
