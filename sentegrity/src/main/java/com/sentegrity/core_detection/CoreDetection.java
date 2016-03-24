package com.sentegrity.core_detection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sentegrity.core_detection.assertion_storage.SentegrityAssertionStore;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.utilities.KeyValueStorage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dmestrov on 20/03/16.
 */
public class CoreDetection {

    private static final String STORAGE_NAME = "CoreDetection";
    private static final int STORAGE_MODE = Context.MODE_PRIVATE;

    private final Context context;

    private static CoreDetection sInstance;

    private SentegrityPolicy currentPolicy;

    private CoreDetectionCallback coreDetectionCallback;

    private KeyValueStorage keyValueStorage;

    private CoreDetection(Context context){
        this.context = context;
        keyValueStorage = new KeyValueStorage(context.getSharedPreferences(STORAGE_NAME, STORAGE_MODE));
    }

    public static CoreDetection getInstance(){
        if(sInstance == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }

    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new CoreDetection(context);
            SentegrityAssertionStore.initialize(context);
            SentegrityTrustFactorStore.initialize(context);
            SentegrityStartupStore.initialize(context);

            startCoreDetectionActivities();
        }else{
            Log.d("coreDetection", "Core Detection has already been initialized");
        }
    }

    public KeyValueStorage getKeyValueStorage(){
        return keyValueStorage;
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

    private static void startCoreDetectionActivities(){
        // TODO: start core activities --> location, bluetooth, motion, magnetometer, wifi // (depending on availability)
    }

    public void performCoreDetectionWithPolicy(SentegrityPolicy policy, final CoreDetectionCallback callback){

        if(1 == 1){
            // let's just simulate everything for now
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callback.onFinish(null, null, true);
                }
            }, 1000);
            return;
        }

        if(callback == null){
            SentegrityError error = SentegrityError.NO_CALLBACK_BLOCK_PROVIDED;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("An invalid callback was provided").setRecoverySuggestion("Try passing a valid callback block"));

            Logger.INFO("Perform Core Detection Unsuccessful", error);
            //callback.onFinish(null, error, false);
            return;
        }
        if(policy == null){
            SentegrityError error = SentegrityError.CORE_DETECTION_NO_POLICY_PROVIDED;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("An invalid policy was provided").setRecoverySuggestion("Try passing a valid policy"));

            Logger.INFO("Perform Core Detection Unsuccessful", error);
            callback.onFinish(null, error, false);
            return;
        }

        this.currentPolicy = policy;
        this.coreDetectionCallback = callback;

        if(policy.getTrustFactors() == null || policy.getTrustFactors().size() < 1){
            SentegrityError error = SentegrityError.NO_TRUSTFACTORS_SET_TO_ANALYZE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No trust factors found to analyze").setRecoverySuggestion("Please provide a policy with valid TrustFactors to analyze"));

            Logger.INFO("Perform Core Detection Unsuccessful", error);
            callback.onFinish(null, error, false);
            return;
        }

        SentegrityStartupStore.getInstance().setCurrentState("Starting Core Detection");

    }




    /**
     * Call if you want to restart data
     */
    public synchronized void logout(){
        SentegrityAssertionStore.initialize(context);
        SentegrityTrustFactorStore.initialize(context);
        SentegrityStartupStore.initialize(context);
        SentegrityTrustFactorDatasets.destroy();

        startCoreDetectionActivities();
    }

}
