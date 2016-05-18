package com.sentegrity.core_detection.assertion_storage;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by dmestrov on 22/03/16.
 */
public class SentegrityTrustFactorStore {

    private static SentegrityTrustFactorStore sInstance;

    private static SentegrityAssertionStore currentStore;

    final private String storePath;

    public SentegrityTrustFactorStore(Context context) {
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

    public String getStorePath() {
        return storePath + File.separator + SentegrityConstants.ASSERTION_STORE_FILE_NAME;
    }

    public void resetAssertionStore(){
        File f = new File(getStorePath());

        if (!f.exists()) {
            if(!f.delete()){
                Logger.INFO("Assertion store file JSON cannot be deleted");
            }
        }
        currentStore = null;
    }

    public SentegrityAssertionStore setAssertionStore(){

        if(currentStore == null){
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting assertion store file unsuccessful").setFailureReason("Assertion Store reference is invalid"));

            Logger.INFO("Failed to Write Assertion Store", error);
        }

        String stringJson = new Gson().toJson(currentStore);

        if(TextUtils.isEmpty(stringJson)) {
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting assertion store file unsuccessful").setFailureReason("Assertion Store reference is invalid"));

            Logger.INFO("Failed to Write Assertion Store", error);
        }

        File f = new File(getStorePath());

        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileUtils.write(f, stringJson);
            return currentStore;
        } catch (IOException e) {
            SentegrityError error = SentegrityError.UNABLE_TO_WRITE_STORE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to write assertion file").setFailureReason("Unable to write assertion file").setRecoverySuggestion("Try providing correct store"));

            Logger.INFO("Failed to Write Assertion Store", error);
        }

        return null;
    }

    public SentegrityAssertionStore getAssertionStore() {

        if (currentStore == null) {
            File f = new File(getStorePath());

            if (!f.exists()) {
                currentStore = new SentegrityAssertionStore();
                if(setAssertionStore() == null)
                    return null;
                return currentStore;
            }

            SentegrityAssertionStore assertionStore;
            String assertionJson;
            try {
                assertionJson = FileUtils.readFileToString(f);
                assertionStore = new Gson().fromJson(assertionJson, SentegrityAssertionStore.class);
                if (assertionStore == null)
                    return null;
                else {
                    currentStore = assertionStore;
                    return currentStore;
                }
            } catch (IOException e) {
                SentegrityError error = SentegrityError.INVALID_STARTUP_FILE;
                error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                error.setDetails(new ErrorDetails().setDescription("Getting assertion file unsuccessful").setFailureReason("Assertion Store file is invalid"));

                Logger.INFO("Failed to Read Assertion Store", error);
            }
        }else{
            return currentStore;
        }
        return null;
    }
}
