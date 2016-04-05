package com.sentegrity.android;

import android.app.Application;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.dispatch.activity_dispatcher.SentegrityActivityDispatcher;

/**
 * Created by dmestrov on 20/03/16.
 */
public class App extends Application {

    SentegrityActivityDispatcher activityDispatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        CoreDetection.initialize(this);
        runDispatcher();
    }

    public void runDispatcher(){
        if(activityDispatcher == null)
            activityDispatcher = new SentegrityActivityDispatcher();
        activityDispatcher.runCoreDetectionActivities(this);
    }
}
