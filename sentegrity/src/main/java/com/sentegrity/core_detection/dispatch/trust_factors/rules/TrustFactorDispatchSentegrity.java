package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;

import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchSentegrity {

    public static SentegrityTrustFactorOutput tamper(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
