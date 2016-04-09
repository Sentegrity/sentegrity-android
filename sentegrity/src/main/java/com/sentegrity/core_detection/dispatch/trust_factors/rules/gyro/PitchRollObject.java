package com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro;

import android.util.Log;

import java.util.Random;

/**
 * Created by dmestrov on 03/04/16.
 */
public class PitchRollObject {
    public float pitch;
    public float roll;
    public float azimuth;

    public PitchRollObject(){
        Random r = new Random();
        pitch = r.nextInt(3) / 10.0f;
        roll = r.nextInt(3) / 10.0f;
    }

    public PitchRollObject(float[] values){
        if(values == null || values.length < 3) return;
        azimuth = values[0];
        pitch = values[1];
        roll = values[2];

        Log.d("pitch", azimuth + ", " + pitch + ", " + roll);
    }
}
