package com.sentegrity.core_detection.assertion_storage;

import android.content.Context;

/**
 * Created by dmestrov on 22/03/16.
 */
public class SentegrityTrustFactorStore {

    private static SentegrityTrustFactorStore sInstance;

    final private Context context;
    final private String storePath;

    public SentegrityTrustFactorStore(Context context) {
        this.context = context;
        this.storePath = context.getFilesDir().getAbsolutePath();
    }

    public static synchronized void initialize(Context context){
        sInstance = new SentegrityTrustFactorStore(context);
    }

    public static SentegrityTrustFactorStore getInstance(){
        if(sInstance == null || sInstance.context == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }
}
