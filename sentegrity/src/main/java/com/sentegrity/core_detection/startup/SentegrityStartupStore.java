package com.sentegrity.core_detection.startup;

import android.content.Context;
import android.os.Build;

import com.google.gson.Gson;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.logger.Logger;
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
            return;
        }
        this.currentState = currentState;
        startup.setLastState(currentState);

        if (!setStartupData(startup)) {
            Logger.INFO("Setting Startup File Failed");
        }
    }

    private SentegrityStartup getStartupData() {
        //TODO: handle errors
        File f = new File(storePath + "/" + SentegrityConstants.STARTUP_FILE_NAME);

        if (!f.exists()) {
            //Create new
            SentegrityStartup sentegrityStartup = new SentegrityStartup();

            sentegrityStartup.setUserSalt(SentegrityConstants.USER_SALT_DEFAULT);
            sentegrityStartup.setDeviceSalt(deviceSalt());
            sentegrityStartup.setLastOSVersion(Build.VERSION.RELEASE); // user release or sdk ?? (eg. "4.3" or 18)

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
            e.printStackTrace();
        }

        return null;
    }

    private boolean setStartupData(SentegrityStartup startup) {
        //TODO: handle errors
        String stringJson = new Gson().toJson(startup);

        File f = new File(storePath + "/" + SentegrityConstants.STARTUP_FILE_NAME);

        try {
            if (!f.exists()) {
                boolean newFile = f.createNewFile();
            }
            FileUtils.write(f, stringJson);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String deviceSalt() {
        long rand = new Random().nextInt(Integer.MAX_VALUE);
        String salt = DeviceID.getID(context, CoreDetection.getInstance().getKeyValueStorage()) + "-" + rand;
        return Helpers.getSHA1Hash(salt);
    }
}
