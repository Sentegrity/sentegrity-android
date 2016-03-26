package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.computation.SentegrityClassificationComputation;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityClassification extends SentegrityClassificationComputation implements Serializable {

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

    public int getID() {
        return ID;
    }

    public int getType() {
        return type;
    }

    public int getComputationMethod() {
        return computationMethod;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getProtectModeAction() {
        return protectModeAction;
    }

    public String getProtectModeMessage() {
        return protectModeMessage;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setComputationMethod(int computationMethod) {
        this.computationMethod = computationMethod;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProtectModeAction(int protectModeAction) {
        this.protectModeAction = protectModeAction;
    }

    public void setProtectModeMessage(String protectModeMessage) {
        this.protectModeMessage = protectModeMessage;
    }
}
