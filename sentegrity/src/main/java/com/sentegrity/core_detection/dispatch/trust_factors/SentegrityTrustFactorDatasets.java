package com.sentegrity.core_detection.dispatch.trust_factors;

import java.util.List;

/**
 * Created by dmestrov on 23/03/16.
 */
public class SentegrityTrustFactorDatasets {

    final private long runTime;

    private static SentegrityTrustFactorDatasets sInstance;

    public SentegrityTrustFactorDatasets() {
        this.runTime = System.currentTimeMillis();
    }

    public static synchronized SentegrityTrustFactorDatasets getInstance(){
        if(sInstance == null){
            sInstance = new SentegrityTrustFactorDatasets();
        }
        return sInstance;
    }

    public long getRunTime() {
        return runTime;
    }

    public static boolean validatePayload(List<Object> payload){
        return !(payload == null || payload.size() < 1);
    }
    /**
     * Call on reloading login
     */
    public static void destroy(){
        sInstance = null;
    }


}
