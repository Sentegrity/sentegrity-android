package com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro;

import android.util.Log;

/**
 * Created by dmestrov on 09/04/16.
 */
public class MagneticObject {
    public float x;
    public float y;
    public float z;

    public MagneticObject(float[] values){
        if(values == null || values.length < 3) return;
        x = values[0];
        y = values[1];
        z = values[2];

        Log.d("mag", x + ", " + y + ", " + z);
    }
}
