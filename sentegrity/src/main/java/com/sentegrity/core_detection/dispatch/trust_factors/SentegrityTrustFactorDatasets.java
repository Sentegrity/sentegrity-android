package com.sentegrity.core_detection.dispatch.trust_factors;

/**
 * Created by dmestrov on 23/03/16.
 */
public class SentegrityTrustFactorDatasets {

    final private long runTime;

    private static SentegrityTrustFactorDatasets sInstance;

    public SentegrityTrustFactorDatasets() {
        this.runTime = System.currentTimeMillis() / 1000;
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

    /**
     * Call on reloading login
     */
    public static void destroy(){
        sInstance = null;
    }


}
