package com.sentegrity.android.activity;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sentegrity.core_detection.constants.SentegrityConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dmestrov on 19/04/16.
 */
public class ActivitiesIntentService extends IntentService {

    private static final String TAG = "ActivitiesIntentService";

    private static final long LOG_HISTORY_TIME = 1 * 10 * 60 * 1000; //keep history for 15 minutes

    public ActivitiesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        SharedPreferences sp = getApplicationContext().getSharedPreferences(SentegrityConstants.SHARED_PREFS_NAME, SentegrityConstants.SHARED_PREFS_MODE);

        final int lastActivity = sp.getInt("lastActivity", -1);
        String currentValue = sp.getString("activities", "");

        String newValue = "";

        //if we have first value, let's just update it
        if (detectedActivities.get(0).getType() == lastActivity && currentValue.split("\n").length > 0) {
            currentValue = currentValue.substring(currentValue.indexOf("\n") + 1);
        }
        currentValue = getLine(detectedActivities) + currentValue;

        String[] list = currentValue.split("\n");

        long currentTime = System.currentTimeMillis();
        if(list.length > 1){
            for (int i = 0; i < list.length; i++) {
                try {
                    if(TextUtils.isEmpty(list[i]))
                        continue;
                    if (Long.valueOf(list[i].replaceAll("^.*?(\\w+)\\W*$", "$1")) + LOG_HISTORY_TIME < currentTime)
                        break;
                    newValue += list[i] + "\n";
                } catch (Exception e) {/*just in case*/}
            }
        }else if(list.length == 1){
            newValue += list[0];
        }

        newValue = newValue.trim();


        sp.edit().putString("activities", newValue).apply();
        sp.edit().putInt("lastActivity", detectedActivities.get(0).getType()).apply();

        Log.d("newActivity", getLine(detectedActivities));
    }

    private String getLine(List<DetectedActivity> activityList) {
        String activities = "";
        for (DetectedActivity activity : activityList) {
            activities += getDetectedActivity(activity.getType()) + " " + activity.getConfidence() + ", ";
        }
        activities = activities.trim();
        activities += getDate(System.currentTimeMillis()) + " " + System.currentTimeMillis() + "\n";
        return activities;
    }

    public static String getDetectedActivity(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "bike";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.WALKING:
                return "walk";
            case DetectedActivity.RUNNING:
                return "run";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            default:
                return "undefined_" + detectedActivityType;
        }
    }

    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public String getDate(long milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}