package com.sentegrity.android.activity;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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

    private static final long LOG_HISTORY_TIME = 24 * 60 * 60 * 1000; //keep history for 24 hours

    public ActivitiesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        SharedPreferences sp = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);

        final int lastActivity = sp.getInt("lastActivity", -1);
        if(detectedActivities.get(0).getType() != lastActivity){
            String currentValue = sp.getString("activities", "");
            currentValue += getLine(detectedActivities);

            String newValue = "";
            String[] list = currentValue.split("\n");
            long currentTime = System.currentTimeMillis();
            for(int i = 0; i < list.length; i++){
                try {
                    if (Long.valueOf(list[i].replaceAll("^.*?(\\w+)\\W*$", "$1")) + LOG_HISTORY_TIME < currentTime)
                        break;
                    newValue += list[i] + "\n";
                }catch (Exception e){/*just in case*/}
            }

            sp.edit().putString("activities", newValue).apply();
            sp.edit().putInt("lastActivity", detectedActivities.get(0).getType()).apply();


            //TODO: remove if not in debug
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getDetectedActivity(lastActivity), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private String getLine(List<DetectedActivity> activityList){
        String activities = "";
        for(DetectedActivity activity : activityList){
            activities += getDetectedActivity(activity.getType()) + " " + activity.getConfidence() + ", ";
        }
        activities = activities.trim();
        activities += getDate(System.currentTimeMillis()) + " " + System.currentTimeMillis() + "\n";
        return activities;
    }

    public static String getDetectedActivity(int detectedActivityType) {
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