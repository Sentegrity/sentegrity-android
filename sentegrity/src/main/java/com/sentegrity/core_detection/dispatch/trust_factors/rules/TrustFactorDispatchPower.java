package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;

import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchPower {

    public static SentegrityTrustFactorOutput powerLevelTime(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput pluggedIn(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput batteryState(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
