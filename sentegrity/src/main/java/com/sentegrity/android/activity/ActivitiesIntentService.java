package com.sentegrity.android.activity;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dmestrov on 19/04/16.
 */
public class ActivitiesIntentService extends IntentService {

    private static final String TAG = "ActivitiesIntentService";

    public ActivitiesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        SharedPreferences sp = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
        String currentValue = sp.getString("activities", "");
        if(currentValue.substring(currentValue.lastIndexOf("\n")).startsWith(getDetectedActivity(detectedActivities.get(0).getType()))){

        }else{
            currentValue += getLine(detectedActivities);
            sp.edit().putString("activities", currentValue).apply();
        }
    }

    private String getLine(List<DetectedActivity> activityList){
        String activities = "";
        for(DetectedActivity activity : activityList){
            activities += getDetectedActivity(activity.getType()) + " " + activity.getConfidence() + ", ";
        }
        activities += getDate(System.currentTimeMillis()) + "\n";
        return activities;
    }

    public String getDetectedActivity(int detectedActivityType) {
        switch(detectedActivityType) {
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
                return "unkown";
            default:
                return "undefined: " + detectedActivityType;
        }
    }

    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public String getDate(long milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}