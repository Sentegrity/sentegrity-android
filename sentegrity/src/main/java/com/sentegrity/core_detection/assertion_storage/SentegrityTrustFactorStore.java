package com.sentegrity.core_detection.assertion_storage;

import android.content.Context;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
        if(sInstance == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }

    public SentegrityAssertionStore setAssertionStore(SentegrityAssertionStore store, String appId){
        //TODO: handle errors
        String stringJson = new Gson().toJson(store);

        File f = new File(storePath + "/" + appId + ".store");

        try {
            if (!f.exists()) {
                boolean newFile = f.createNewFile();
            }
            FileUtils.write(f, stringJson);
            return store;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public SentegrityAssertionStore getAssertionStore(String appId){
        //TODO: handle errors
        File f = new File(storePath + "/" + appId + ".store");

        if (!f.exists()) {
            return null;
        }

        SentegrityAssertionStore assertionStore;
        String assertionJson;
        try {
            assertionJson = FileUtils.readFileToString(f);
            assertionStore = new Gson().fromJson(assertionJson, SentegrityAssertionStore.class);
            if(assertionStore != null && appId.equals(assertionStore.getAppId()))
                return assertionStore;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
