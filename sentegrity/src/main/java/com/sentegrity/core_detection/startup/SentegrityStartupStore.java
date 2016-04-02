package com.sentegrity.core_detection.startup;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.utilities.DeviceID;
import com.sentegrity.core_detection.utilities.Helpers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStartupStore {

    private static SentegrityStartupStore sInstance;

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

    public void setCurrentState(String currentState) {
        SentegrityStartup startup = getStartupData();
        if (startup == null) {
            Logger.INFO("Setting Startup file Current state Failed");
            return;
        }
        this.currentState = currentState;
        startup.setLastState(currentState);

        if (!setStartupData(startup)) {
            Logger.INFO("Setting Startup File Failed");
        }
    }

    public SentegrityStartup getStartupData() {
        File f = new File(storePath + "/" + SentegrityConstants.STARTUP_FILE_NAME);

        if (!f.exists()) {
            //Create new
            SentegrityStartup sentegrityStartup = new SentegrityStartup();

            sentegrityStartup.setUserSalt(SentegrityConstants.USER_SALT_DEFAULT);
            sentegrityStartup.setDeviceSalt(deviceSalt());
            sentegrityStartup.setLastOSVersion(Build.VERSION.RELEASE); // user release or sdk ?? (i.e. "4.3" or 18)

            if (setStartupData(sentegrityStartup)) {
                return sentegrityStartup;
            }

            return null;
        }

        SentegrityStartup startup;
        String startupJson;
        try {
            startupJson = FileUtils.readFileToString(f);
            startup = new Gson().fromJson(startupJson, SentegrityStartup.class);
            return startup;
        } catch (IOException e) {
            SentegrityError error = SentegrityError.INVALID_STARTUP_FILE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Getting startup file unsuccessful").setFailureReason("Startup Class file is invalid").setRecoverySuggestion("Try removing the startup file and retry"));

            Logger.INFO("Failed to Read Startup Store", error);
        }

        return null;
    }

    public boolean setStartupData(SentegrityStartup startup) {
        if(startup == null) {
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting startup file unsuccessful").setFailureReason("Startup class reference is invalid").setRecoverySuggestion("Try passing a valid startup object"));

            Logger.INFO("Failed to Write Startup Store", error);
        }

        String stringJson = new Gson().toJson(startup);

        if(TextUtils.isEmpty(stringJson)){
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting startup file unsuccessful").setFailureReason("Startup class reference doesn't parse to json").setRecoverySuggestion("Try passing a valid JSON startup object"));

            Logger.INFO("Failed to Write Startup Store", error);
        }

        File f = new File(storePath + "/" + SentegrityConstants.STARTUP_FILE_NAME);

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

    private String deviceSalt() {
        long rand = new Random().nextInt(Integer.MAX_VALUE);
        String salt = DeviceID.getID(context, CoreDetection.getInstance().getKeyValueStorage()) + "-" + rand;
        return Helpers.getSHA1Hash(salt);
    }
}
