package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;

import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchBluetooth {

    public static SentegrityTrustFactorOutput discoveredBLEDevice(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput connectedClassicDevice(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
