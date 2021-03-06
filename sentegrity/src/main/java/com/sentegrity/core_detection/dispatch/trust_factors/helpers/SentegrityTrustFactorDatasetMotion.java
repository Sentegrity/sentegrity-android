package com.sentegrity.core_detection.dispatch.trust_factors.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.sentegrity.android.activity.ActivitiesIntentService;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.GyroRadsObject;

import java.util.List;
import java.util.Random;

/**
 * Created by dmestrov on 10/04/16.
 */
public class SentegrityTrustFactorDatasetMotion {

    public static float getGripMovement(){
        return getGripMovement(SentegrityTrustFactorDatasets.getInstance().getGyroRads());
    }

    public static float getGripMovement(List<GyroRadsObject> gyroRads){
        float lastX = 0.0f;
        float lastY = 0.0f;
        float lastZ = 0.0f;

        float dist = 0.0f;
        int count = 0;

        for(GyroRadsObject sample : gyroRads){
            if(lastX == 0){
                lastX = sample.x;
                lastY = sample.y;
                lastZ = sample.z;
                continue;
            }

            float dx = (sample.x - lastX);
            float dy = (sample.y - lastY);

            dist = (float) (dist + Math.sqrt(dx * dx + dy * dy + dx * dx));
            count ++;
        }

        float avgDist = dist / (float) count;

        return avgDist;
    }

    public static String getUserMovement(Context context){
        SharedPreferences sp = context.getSharedPreferences(SentegrityConstants.SHARED_PREFS_NAME, SentegrityConstants.SHARED_PREFS_MODE);

        final int lastActivity = sp.getInt("lastActivity", -1);
        if(lastActivity == -1)
            return null;

        return ActivitiesIntentService.getDetectedActivity(lastActivity);

        /*final String activities = sp.getString("activities", "");
        String list[] = activities.split("\n");
        if(list.length == 0)
            return null;

        try {
            //if (Long.valueOf(list[0].replaceAll("^.*?(\\w+)\\W*$", "$1")) + 15 * 60 * 1000 < System.currentTimeMillis())
            //    return null;
            return list[0].trim().split(" ")[0];
        }catch (Exception e){//just in case}

        return null;*/
    }

    public static String getPreviousUserMovement(Context context){
        SharedPreferences sp = context.getSharedPreferences(SentegrityConstants.SHARED_PREFS_NAME, SentegrityConstants.SHARED_PREFS_MODE);

        final String activities = sp.getString("activities", "");
        String list[] = activities.split("\n");
        if(list.length == 0)
            return null;

        try {
            String line = list[list.length - 1];
            if(TextUtils.isEmpty(line.trim())) {
                if(list.length > 1)
                    line = list[list.length - 2];
                else
                    return null;
            }
            //if (Long.valueOf(line.replaceAll("^.*?(\\w+)\\W*$", "$1")) + 15 * 60 * 1000 < System.currentTimeMillis())
            //    return null;
            return line.trim().split(" ")[0];
        }catch (Exception e){/*just in case*/}

        return null;
    }

    public static String getOrientation(Context context){
        return getOrientation(context, SentegrityTrustFactorDatasets.getInstance().getAccelRads());
    }

    public static String getOrientation(Context context, List<AccelRadsObject> accelRads){
        String orientation = "error";

        if(SentegrityTrustFactorDatasets.getInstance().getAccelMotionDNEStatus() != DNEStatusCode.OK){
            orientation = getScreenOrientation(context);
        }else{

            float xAvg, yAvg, zAvg;

            float xTotal = 0.0f, yTotal = 0.0f, zTotal = 0.0f;

            int count = 0;

            for(AccelRadsObject sample : accelRads){
                count++;
                xTotal += sample.x;
                yTotal += sample.y;
                zTotal += sample.z;
            }

            if(count < 1){
                orientation = getScreenOrientation(context);
            }else {
                xAvg = xTotal / (float)count;
                yAvg = yTotal / (float)count;
                zAvg = zTotal / (float)count;

                //we need to round this to G values
                xAvg /= 9.81;
                yAvg /= 9.81;
                zAvg /= 9.81;

                if(xAvg >= 0.35 && yAvg <= 0.7 && yAvg >= -0.7){
                    //landscape Left
                    Log.d("orientation", "left");
                    orientation = "Landscape_Left";
                }else if(xAvg <= -0.35 && yAvg <= 0.7 && yAvg >= -0.7){
                    //landscape Right
                    Log.d("orientation", "right");
                    orientation = "Landscape_Right";
                }else if(yAvg <= -0.15 && xAvg <= 0.7 && xAvg >= -0.7){
                    //upside down
                    Log.d("orientation", "upsidedown");
                    orientation = "Portrait_Upside_Down";
                }else if(yAvg >= 0.15 && xAvg <= 0.7 && xAvg >= -0.7){
                    //portrait
                    Log.d("orientation", "portrait");
                    orientation = "Portrait";
                //TODO should we use 0.35?
                }else if(xAvg <= 0.15 && xAvg >= -0.15 && yAvg <= 0.15 && yAvg >= -0.15 && zAvg <= 0){
                    //face down
                    Log.d("orientation", "down");
                    orientation = "Face_Down";
                //TODO should we use 0.35?
                }else if(xAvg <= 0.15 && xAvg >= -0.15 && yAvg <= 0.15 && yAvg >= -0.15 && zAvg > 0){
                    //face up
                    Log.d("orientation", "up");
                    orientation = "Face_Up";
                }else {
                    //unknown
                    Log.d("orientation", "unknown");
                    orientation = "unknown";
                }
            }
        }

        return orientation;
    }

    public static String getScreenOrientation(Context context) {
        final int screenOrientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (screenOrientation) {
            case Surface.ROTATION_0:
                return "Portrait";
            case Surface.ROTATION_90:
                return "Landscape_Left";
            case Surface.ROTATION_180:
                return "Portrait_Upside_Down";
            case Surface.ROTATION_270:
                return "Landscape_Right";
            default:
                return "unknown";
        }
    }
}
