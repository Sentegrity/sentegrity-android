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


    // Invalid Policy PIN provided
    INVALID_POLICY_PIN_PROVIDED(8),

    // Unable to deactivate protect mode due to error
    UNABLE_TO_WHITELIST_ASSERTION(9),

    // Invalid User PIN provided
    INVALID_USER_PIN_PROVIDED(10),


    // Unable to set assertion objects from output
    UNABLE_TO_SET_ASSERTION_OBJECTS_FROM_OUTPUT(50),


    // No assertions received
    NO_TRUST_FACTOR_OUTPUT_OBJECTS_RECEIVED(18),

    // Unable to add assertion object into the assertion store
    UNABLE_TO_ADD_STORE_TRUST_FACTOR_OBJECTS_INTO_STORE(21),

    // Invalid assertion objects provided
    INVALID_STORED_TRUST_FACTOR_OBJECTS_PROVIDED(27),

    // Unable to remove assertion
    UNABLE_TO_REMOVE_ASSERTION(28),

    // Assertion does not exist
    NO_MATCHING_ASSERTION_FOUND(25),

    // No FactorID received
    ASSERTION_STORE_NO_FACTOR_ID_RECEIVED(20),


    // No security token provided
    NO_APP_ID_PROVIDED(17),

    // Unable to write the assertion store
    UNABLE_TO_WRITE_STORE(42),


    // No assertions added to store
    NO_ASSERTIONS_ADDED_TO_STORE(19),

    // Unable to set assertion to the store
    UNABLE_TO_SET_ASSERTION_TO_STORE(28),

    // Cannot create new assertion for existing trustfactor
    UNABLE_TO_CREATE_NEW_STORED_ASSERTION(36),

    // Invalid due to no candidate assertions generated
    UNABLE_TO_PERFORM_BASE_ANALYSIS_FOR_TRUST_FACTOR(38),

    // Error when trying to check TF learning and add candidate assertions in
    ERROR_DURING_LEARNING_CHECK(47),


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
