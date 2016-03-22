package com.sentegrity.android;

import android.app.Application;

import com.sentegrity.core_detection.CoreDetection;

/**
 * Created by dmestrov on 20/03/16.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CoreDetection.initialize(this);
    }
}
