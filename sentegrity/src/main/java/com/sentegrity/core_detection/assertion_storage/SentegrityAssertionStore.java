package com.sentegrity.core_detection.assertion_storage;

import android.content.Context;

/**
 * Created by dmestrov on 22/03/16.
 */
public class SentegrityAssertionStore {

    private static SentegrityAssertionStore sInstance;

    final private Context context;

    public SentegrityAssertionStore(Context context) {
        this.context = context;
    }

    public static synchronized void initialize(Context context){
        sInstance = new SentegrityAssertionStore(context);
    }

    public static SentegrityAssertionStore getInstance(){
        if(sInstance == null || sInstance.context == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }
}
