package com.sentegrity.core_detection.logger;

/**
 * Created by dmestrov on 23/03/16.
 */
public enum ErrorDomain {
    CORE_DETECTION_DOMAIN("Core Detection"),
    ASSERTION_STORE_DOMAIN("Assertion Store"),
    TRUSTFACTOR_DISPATCHER_DOMAIN("TrustFactor Dispatcher"),
    SENTEGRITY_DOMAIN("Sentegrity");

    private String name;

    ErrorDomain(String domain){
        name = domain;
    }

    public String getName(){
        return name;
    }
}
