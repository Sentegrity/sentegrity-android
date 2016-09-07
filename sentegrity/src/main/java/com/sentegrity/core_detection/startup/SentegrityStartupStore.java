package com.sentegrity.core_detection.startup;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.crypto.SentegrityCrypto;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.utilities.DeviceID;
import com.sentegrity.core_detection.utilities.Helpers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStartupStore {

    private static SentegrityStartupStore sInstance;

    private SentegrityStartup currentStartupStore;

    private final Context context;
    private final String storePath;

    public SentegrityStartupStore(Context context) {
        this.context = context;
        this.storePath = context.getFilesDir().getAbsolutePath();
    }

    public static synchronized void initialize(Context context) {
        sInstance = new SentegrityStartupStore(context);
    }

    public static SentegrityStartupStore getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }

    private String currentState;

    public String getCurrentState() {
        return currentState;
    }

    public String getStorePath(){
        return storePath + File.separator + SentegrityConstants.STARTUP_FILE_NAME;
    }

    public void resetStartupStore(){
        File f = new File(getStorePath());

        if (f.exists()) {
            if(!f.delete()){
                Logger.INFO("Assertion store file JSON cannot be deleted");
            }
        }
        currentStartupStore = null;
    }

    public void setCurrentState(String currentState) {
        SentegrityStartup startup = currentStartupStore;
        if (startup == null) {
            Logger.INFO("Setting Startup file Current state Failed");
            return;
        }
        this.currentState = currentState;
        startup.setLastState(currentState);
    }

    public void setStartupDataWithComputationResults(SentegrityTrustScoreComputation computationResults){
        if(currentStartupStore == null){
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to get startup file").setFailureReason("No startup file received").setRecoverySuggestion("Try validating startup file"));

            Logger.INFO("Failed to get Startup file", error);

            //TODO: return?
            //return;
        }

        SentegrityHistoryObject runHistoryObject = new SentegrityHistoryObject();

        runHistoryObject.setTimestamp(System.currentTimeMillis());

        runHistoryObject.setCoreDetectionResult(computationResults.getCoreDetectionResult());
        runHistoryObject.setPreAuthenticationAction(computationResults.getPreAuthenticationAction());
        runHistoryObject.setPostAuthenticationAction(computationResults.getPostAuthenticationAction());
        runHistoryObject.setAuthenticationResult(computationResults.getAuthenticationResult());

        runHistoryObject.setDeviceScore(computationResults.getSystemScore());
        runHistoryObject.setTrustScore(computationResults.getDeviceScore());
        runHistoryObject.setUserScore(computationResults.getUserScore());

        runHistoryObject.setSystemIssues(computationResults.getSystemIssues());
        runHistoryObject.setUserIssues(computationResults.getUserIssues());

        runHistoryObject.setSystemAnalysisResults(computationResults.getSystemAnalysisResults());
        runHistoryObject.setUserAnalysisResults(computationResults.getUserAnalysisResults());

        runHistoryObject.setUserSuggestions(computationResults.getUserSuggestions());
        runHistoryObject.setSystemSuggestions(computationResults.getSystemSuggestions());

        if(currentStartupStore.getRunHistoryObjects() == null || currentStartupStore.getRunHistoryObjects().size() < 1){
            List<SentegrityHistoryObject> objectList = new ArrayList<>();
            objectList.add(runHistoryObject);
            currentStartupStore.setRunHistoryObjects(objectList);
        }else{
            currentStartupStore.getRunHistoryObjects().add(runHistoryObject);
        }

        if(!setStartupStore()){
            Logger.INFO("Failed to set startup file for unknown reasons.");
        }
    }

    public boolean setStartupStore(){
        if(currentStartupStore == null){
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting startup file unsuccessful").setFailureReason("Startup class reference is invalid").setRecoverySuggestion("Try passing a valid startup object"));

            Logger.INFO("Failed to Write Startup Store", error);

            return false;
        }

        String stringJson = new Gson().toJson(currentStartupStore);

        if(TextUtils.isEmpty(stringJson)){
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting startup file unsuccessful").setFailureReason("Startup class reference doesn't parse to json").setRecoverySuggestion("Try passing a valid JSON startup object"));

            Logger.INFO("Failed to Write Startup Store", error);

            return false;
        }

        File f = new File(getStorePath());

        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileUtils.write(f, stringJson);
            return true;
        } catch (IOException e) {
            SentegrityError error = SentegrityError.UNABLE_TO_WRITE_STORE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to write startup file").setFailureReason("Unable to write startup file").setRecoverySuggestion("Try providing correct path"));

            Logger.INFO("Failed to Write Startup Store", error);
        }

        return false;
    }

    public SentegrityStartup getStartupStore() {

        if(currentStartupStore == null) {
            //Create new
            SentegrityStartup sentegrityStartup = new SentegrityStartup();

            currentStartupStore = sentegrityStartup;

            File f = new File(getStorePath());

            if (!f.exists()) {

                return null;
            }

            SentegrityStartup startup;
            String startupJson;
            try {
                startupJson = FileUtils.readFileToString(f);
                startup = new Gson().fromJson(startupJson, SentegrityStartup.class);
                return currentStartupStore = startup;
            } catch (IOException e) {
                SentegrityError error = SentegrityError.INVALID_STARTUP_FILE;
                error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                error.setDetails(new ErrorDetails().setDescription("Getting startup file unsuccessful").setFailureReason("Startup Class file is invalid").setRecoverySuggestion("Try removing the startup file and retry"));

                Logger.INFO("Failed to Read Startup Store", error);
            }
        }else{
            return currentStartupStore;
        }

        return null;
    }

    public void createNewStartupFile(){
        SentegrityStartup startup = new SentegrityStartup();

        currentStartupStore = startup;

        byte[] deviceSaltData = SentegrityCrypto.getInstance().generateSalt256();
        currentStartupStore.setDeviceSaltString(SentegrityCrypto.getInstance().convertDataToHexString(deviceSaltData));

        byte[] userKeySaltData = SentegrityCrypto.getInstance().generateSalt256();
        currentStartupStore.setUserKeySaltString(SentegrityCrypto.getInstance().convertDataToHexString(userKeySaltData));

        byte[] transparentAuthGlobalPBKDF2Salt = SentegrityCrypto.getInstance().generateSalt256();
        currentStartupStore.setTransparentAuthGlobalPBKDF2SaltString(SentegrityCrypto.getInstance().convertDataToHexString(transparentAuthGlobalPBKDF2Salt));

        int estimateRounds = SentegrityCrypto.getInstance().getEstimateIterationsForMillis(500);
        currentStartupStore.setUserKeyPBKDF2rounds(estimateRounds);
        currentStartupStore.setTransparentAuthPBKDF2rounds(estimateRounds);


        //sentegrityStartup.setUserSalt(SentegrityConstants.USER_SALT_DEFAULT);
        //sentegrityStartup.setDeviceSalt(deviceSalt());
        currentStartupStore.setLastOSVersion(Build.VERSION.RELEASE);

        currentStartupStore.setLastState("");

        currentStartupStore.setRunHistoryObjects(new ArrayList<SentegrityHistoryObject>());
        currentStartupStore.setTransparentAuthKeyObjects(new ArrayList<SentegrityTransparentAuthObject>());
        currentStartupStore.setRunCount(0);
        currentStartupStore.setRunCountAtLastUpload(0);
        currentStartupStore.setTimeOfLastUpload(0);

        setStartupStore();

        //return masterKeyString;
    }

    public String udpateStartupFileWithPassword(String password){
        if(getStartupStore() == null)
            return null;

        String masterKeyString = SentegrityCrypto.getInstance().provisionNewUserKeyAndCreateMasterKeyWithPassword(password);
        if(masterKeyString == null){
            SentegrityError error = SentegrityError.INVALID_STARTUP_FILE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Error creating new user and master key").setFailureReason("Unknown").setRecoverySuggestion("Try removing the startup file and retry"));

            Logger.INFO("Failed to Read Startup Store", error);
        }

        setStartupStore();

        return masterKeyString;
    }

    public void udpateStartupFileWithEmail(String email){
        currentStartupStore.setEmail(email);

        setStartupStore();
    }

    public void setCurrentStartupStore(SentegrityStartup currentStartupStore) {
        this.currentStartupStore = currentStartupStore;
    }
}
