package com.sentegrity.core_detection.logger;

import android.util.Log;

/**
 * Created by dmestrov on 22/03/16.
 */
public class Logger {

    public static void INFO(String info, SentegrityError error){
        Log.e(error.getDomain().getName(), info + "; " + error.getDetails().toString());
    }

    public static void INFO(String info){
        Log.d("CoreDetection", info);
    }
}
