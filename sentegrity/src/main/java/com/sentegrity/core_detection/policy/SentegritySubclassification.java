package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.computation.SentegritySubclassificationComputation;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegritySubclassification extends SentegritySubclassificationComputation implements Serializable {

    @SerializedName("id")
    private int ID;

    @SerializedName("name")
    private String name;

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

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDneUnauthorized() {
        return dneUnauthorized;
    }

    public String getDneUnsupported() {
        return dneUnsupported;
    }

    public String getDneUnavailable() {
        return dneUnavailable;
    }

    public String getDneDisabled() {
        return dneDisabled;
    }

    public String getDneNoData() {
        return dneNoData;
    }

    public String getDneExpired() {
        return dneExpired;
    }

    public String getDneInvalid() {
        return dneInvalid;
    }

    public int getWeight() {
        return weight;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDneUnauthorized(String dneUnauthorized) {
        this.dneUnauthorized = dneUnauthorized;
    }

    public void setDneUnsupported(String dneUnsupported) {
        this.dneUnsupported = dneUnsupported;
    }

    public void setDneUnavailable(String dneUnavailable) {
        this.dneUnavailable = dneUnavailable;
    }

    public void setDneDisabled(String dneDisabled) {
        this.dneDisabled = dneDisabled;
    }

    public void setDneNoData(String dneNoData) {
        this.dneNoData = dneNoData;
    }

    public void setDneExpired(String dneExpired) {
        this.dneExpired = dneExpired;
    }

    public void setDneInvalid(String dneInvalid) {
        this.dneInvalid = dneInvalid;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
