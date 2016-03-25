package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;

import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchMotion /*implements TrustFactorDispatch*/ {

    public static SentegrityTrustFactorOutput orientation(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput movement(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput grip(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    /*
    @Override
    public SentegrityTrustFactorOutput run(String method, List<Object> payload) {
        if("orientation".equals(method)){
            return orientation(payload);
        }
        else if("movement".equals(method)){
            return movement(payload);
        }
        else if("grip".equals(method)){
            return grip(payload);
        }
        return null;
    }*/
}
