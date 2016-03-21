package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityClassification implements Serializable {

    @SerializedName("id")
    private int ID;

    @SerializedName("type")
    private int type;

    @SerializedName("computationMethod")
    private int computationMethod;

    @SerializedName("name")
    private String name;

    @SerializedName("desc")
    private String description;

    @SerializedName("protectModeAction")
    private int protectModeAction;

    @SerializedName("protectModeMessage")
    private String protectModeMessage;

}
