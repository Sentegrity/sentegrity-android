package com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro;

import android.util.Log;

import java.util.Random;

/**
 * Created by dmestrov on 09/04/16.
 */
public class AccelRadsObject {
    public float x;
    public float y;
    public float z;

    public AccelRadsObject(){
        Random r = new Random();
        x = r.nextInt(3) / 10.0f;
        y = r.nextInt(3) / 10.0f;
        z = r.nextInt(3) / 10.0f;
    }

    public AccelRadsObject(float[] values){
        if(values == null || values.length < 3) return;
        x = values[0];
        y = values[1];
        z = values[2];

        Log.d("accel", x + ", " + y + ", " + z);
    }
}
