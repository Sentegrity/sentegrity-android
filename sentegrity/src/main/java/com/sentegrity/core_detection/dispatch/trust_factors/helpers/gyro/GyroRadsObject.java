package com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro;

import android.util.Log;

import java.util.Random;

/**
 * Created by dmestrov on 03/04/16.
 */
public class GyroRadsObject {
    public float x;
    public float y;
    public float z;

    public GyroRadsObject(float[] values){
        if(values == null || values.length < 3) return;
        x = values[0];
        y = values[1];
        z = values[2];

        Log.d("gyro", x + ", " + y + ", " + z);
    }

    @Override
    public String toString() {
        return x + "\n" + y + "\n" + z;
    }
}
