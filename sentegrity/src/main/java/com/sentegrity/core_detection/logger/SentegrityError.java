package com.sentegrity.core_detection.logger;

/**
 * Created by dmestrov on 23/03/16.
 */
public enum SentegrityError {

    /**
     * Unkown error codes
     */

    // Unknown error
    UNKNOWN_ERROR(0),

    /**
     * Core detection error codes
     */

    // No Policy Provided
    CORE_DETECTION_NO_POLICY_PROVIDED(1),

    // No Callback Block Provided
    NO_CALLBACK_BLOCK_PROVIDED(2),

    // No TrustFactors set to analyze
    NO_TRUSTFACTORS_SET_TO_ANALYZE(3);

    private int code;

    SentegrityError(int code) {
        this.code = code;
    }

    private ErrorDomain domain;

    private ErrorDetails details;

    public void setDomain(ErrorDomain domain) {
        this.domain = domain;
    }

    public ErrorDomain getDomain() {
        return domain != null ? domain : ErrorDomain.CORE_DETECTION_DOMAIN;
    }

    public void setDetails(ErrorDetails details) {
        this.details = details;
    }

    public ErrorDetails getDetails() {
        return details != null ? details : new ErrorDetails();
    }
}