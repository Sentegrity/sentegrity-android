package com.sentegrity.core_detection.dispatch.trust_factors.helpers;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro.GyroRadsObject;

import java.util.List;
import java.util.Random;

/**
 * Created by dmestrov on 10/04/16.
 */
public class SentegrityTrustFactorDatasetMotion {

    public static float getGripMovement(){
        List<GyroRadsObject> gyroRads = SentegrityTrustFactorDatasets.getInstance().getGyroRads();

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

    public static String getUserMovement(){
        //TODO: return real movement
        // fake random movement for testing purposes
        int i = new Random().nextInt(5);
        switch (i) {
            case 0:
                return "StandingStill";
            case 1:
                return "Walking";
            case 2:
                return "Running";
            case 3:
                return "ChangingOrientation";
            default:
                return "RotatingOrShaking";
        }
    }

    public static String getOrientation(Context context){
        String orientation = "error";

        if(SentegrityTrustFactorDatasets.getInstance().getAccelMotionDNEStatus() != DNEStatusCode.OK){
            orientation = getScreenOrientation(context);
        }else{
            List<AccelRadsObject> accelRads = SentegrityTrustFactorDatasets.getInstance().getAccelRads();

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
                }else if(xAvg <= 0.15 && xAvg >= -0.15 && yAvg <= 0.15 && yAvg >= -0.15 && zAvg <= 0){
                    //face down
                    Log.d("orientation", "down");
                    orientation = "Face_Down";
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
//        // fake random orientation for testing purposes
//        int i = new Random().nextInt(8);
//        switch (i) {
//            case 0:
//                return "Portrait";
//            case 1:
//                return "Landscape_Right";
//            case 2:
//                return "Landscape_Left";
//            case 3:
//                return "Portrait_Upside_Down";
//            case 4:
//                return "Face_Up";
//            case 5:
//                return "Face_Down";
//            case 6:
//                return "unknown";
//            default:
//                return "error";
//        }
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
