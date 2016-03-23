package com.sentegrity.core_detection.startup;

import android.content.Context;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.utilities.DeviceID;
import com.sentegrity.core_detection.utilities.Helpers;
import com.sentegrity.core_detection.utilities.KeyValueStorage;

import java.util.Random;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityStartupStore {

    private static SentegrityStartupStore sInstance;

    final private Context context;

    public SentegrityStartupStore(Context context) {
        this.context = context;
    }

    public static synchronized void initialize(Context context){
        sInstance = new SentegrityStartupStore(context);
    }

    public static SentegrityStartupStore getInstance(){
        if(sInstance == null || sInstance.context == null) {
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

        SentegrityStartup startup = getStartupFile();
        if(startup == null) {
            return;
        }

        this.currentState = currentState;
        startup.setLastState(currentState);

        if(setStartupFile())
            return;
        else
            Logger.INFO("Setting Startup File Failed");
    }

    private SentegrityStartup getStartupFile(){
        //TODO: get startup file; handle and log errors
        return new SentegrityStartup();
    }

    private boolean setStartupFile(){
        //TODO: update startup file
        return false;
    }

    private String deviceSalt(){
        long rand = new Random().nextInt(Integer.MAX_VALUE);
        String salt = DeviceID.getID(context, CoreDetection.getInstance().getKeyValueStorage()) + "-" + rand;
        return Helpers.getSHA1Hash(salt);
    }
}
