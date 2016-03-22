package com.sentegrity.core_detection;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sentegrity.core_detection.assertion_storage.SentegrityAssertionStore;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dmestrov on 20/03/16.
 */
public class CoreDetection {

    private final Context context;

    public static CoreDetection sInstance;

    public static SentegrityAssertionStore assertionStore;
    public static SentegrityTrustFactorStore trustFactorStore;
    public static SentegrityStartupStore startupStore;

    private CoreDetection(Context context){
        this.context = context;
    }

    public static CoreDetection getInstance(){
        if(sInstance == null || sInstance.context == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }

    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new CoreDetection(context);
        }else{
            Log.d("coreDetection", "Core Detection has already been initialized");
        }
    }

    //TODO: handle possible errors / policy not available, failed parsing ...
    public SentegrityPolicy parsePolicy(String policyName){
        AssetManager mg = context.getResources().getAssets();

        String policyJson;
        try {
            InputStream is = mg.open(policyName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            policyJson = new String(buffer, "UTF-8");

            return new Gson().fromJson(policyJson, SentegrityPolicy.class);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JsonSyntaxException ex){
            ex.printStackTrace();
            return null;
        }
    }

}
