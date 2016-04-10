package com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat;

/**
 * Created by dmestrov on 10/04/16.
 */
public enum UDPState {

    UDP("UDP", 0);

    private String state;

    UDPState(String state, int i) {
        this.state = state;
    }
}
