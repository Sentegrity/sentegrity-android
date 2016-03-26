package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityDNEModifiers implements Serializable {

    @SerializedName("unauthorized")
    private double unauthorized;

    @SerializedName("unsupported")
    private double unsupported;

    @SerializedName("unavailable")
    private double unavailable;

    @SerializedName("disabled")
    private double disabled;

    @SerializedName("noData")
    private double noData;

    @SerializedName("expired")
    private double expired;

    @SerializedName("error")
    private double error;

    @SerializedName("invalid")
    private double invalid;

    public double getUnauthorized() {
        return unauthorized;
    }

    public double getUnsupported() {
        return unsupported;
    }

    public double getUnavailable() {
        return unavailable;
    }

    public double getDisabled() {
        return disabled;
    }

    public double getNoData() {
        return noData;
    }

    public double getExpired() {
        return expired;
    }

    public double getError() {
        return error;
    }

    public double getInvalid() {
        return invalid;
    }

    public void setUnauthorized(double unauthorized) {
        this.unauthorized = unauthorized;
    }

    public void setUnsupported(double unsupported) {
        this.unsupported = unsupported;
    }

    public void setUnavailable(double unavailable) {
        this.unavailable = unavailable;
    }

    public void setDisabled(double disabled) {
        this.disabled = disabled;
    }

    public void setNoData(double noData) {
        this.noData = noData;
    }

    public void setExpired(double expired) {
        this.expired = expired;
    }

    public void setError(double error) {
        this.error = error;
    }

    public void setInvalid(double invalid) {
        this.invalid = invalid;
    }
}
