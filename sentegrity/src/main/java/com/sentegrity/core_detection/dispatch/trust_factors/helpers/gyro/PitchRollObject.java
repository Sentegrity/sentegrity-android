package com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro;

import android.util.Log;

import java.util.Random;

/**
 * Created by dmestrov on 03/04/16.
 */
public class PitchRollObject {
    public float pitch;
    public float roll;
    public float azimuth;

    public PitchRollObject(float[] values){
        if(values == null || values.length < 3) return;
        azimuth = remap(values[0]);
        pitch = remap(values[1]);
        roll = remap(values[2]);

        Log.d("pitch", azimuth + ", " + pitch + ", " + roll);
    }

    private float remap(float value) {
        // -1 to 1
        return (float) (-1 + (value + Math.PI) / Math.PI);
//        // 0 to 1
//        return (float) ((value + Math.PI) / 2 * Math.PI);
    }
}
