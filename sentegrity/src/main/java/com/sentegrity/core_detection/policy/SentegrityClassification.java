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

    @SerializedName("preAuthenticationAction")
    private int preAuthenticationAction;

    @SerializedName("postAuthenticationAction")
    private int postAuthenticationAction;

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

    public int getPreAuthenticationAction() {
        return preAuthenticationAction;
    }

    public int getPostAuthenticationAction() {
        return postAuthenticationAction;
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

    public void setPreAuthenticationAction(int preAuthenticationAction) {
        this.preAuthenticationAction = preAuthenticationAction;
    }

    public void setPostAuthenticationAction(int postAuthenticationAction) {
        this.postAuthenticationAction = postAuthenticationAction;
    }
}
