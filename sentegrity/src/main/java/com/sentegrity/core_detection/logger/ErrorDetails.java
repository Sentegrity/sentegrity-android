package com.sentegrity.core_detection.logger;

import android.text.TextUtils;

/**
 * Created by dmestrov on 23/03/16.
 */
public class ErrorDetails {

    public String description, reason, suggestion;

    public ErrorDetails setDescription(String description) {
        this.description = description;
        return this;
    }

    public ErrorDetails setFailureReason(String failureReason) {
        this.reason = failureReason;
        return this;
    }

    public ErrorDetails setRecoverySuggestion(String recoverySuggestion) {
        this.suggestion = recoverySuggestion;
        return this;
    }

    @Override
    public String toString() {
        String details = "";
        if (!TextUtils.isEmpty(description)) {
            details += "Description: " + description;
        }
        if (!TextUtils.isEmpty(reason)) {
            if (!TextUtils.isEmpty(details)) details += "; ";
            details += "Failure Reason: " + reason;
        }
        if (!TextUtils.isEmpty(suggestion)) {
            if (!TextUtils.isEmpty(details)) details += "; ";
            details += "Recovery Suggestion: " + suggestion;
        }
        return details;
    }
}
