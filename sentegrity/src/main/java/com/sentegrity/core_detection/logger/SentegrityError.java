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
    NO_TRUSTFACTORS_SET_TO_ANALYZE(3),

    // Invalid Startup File
    INVALID_STARTUP_FILE(322),

    // Invalid Startup Instance
    INVALID_STARTUP_INSTANCE(323),

    // No TrustFactor output objects provided from dispatcher
    NO_TRUSTFACTOR_OUTPUT_OBJECTS_FROM_DISPATCHER(4),

    // Unable to perform computation as no trustfactor objects provided
    NO_TRUSTFACTOR_OUTPUT_OBJECTS_FOR_COMPUTATION(5),

    // Unable to perform computation as no trustfactor objects provided
    ERROR_DURING_COMPUTATION(6),

    // Unable to get the policy from the provided path
    INVALID_POLICY_PATH(7),


    // No security token provided
    NO_APP_ID_PROVIDED(17),

    // Unable to write the assertion store
    UNABLE_TO_WRITE_STORE(42),


    // No classifications found
    NO_CLASSIFICATIONS_FOUND(33),

    // No subclassifications found
    NO_SUBCLASSIFICATIONS_FOUND(34);

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
