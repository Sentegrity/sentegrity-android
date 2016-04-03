package com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro;

import java.util.Random;

/**
 * Created by dmestrov on 03/04/16.
 */
public class GyroRadsObject {
    public float x;
    public float y;
    public float z;

    public GyroRadsObject(){
        Random r = new Random();
        x = r.nextInt(3) / 10.0f;
        y = r.nextInt(3) / 10.0f;
        z = r.nextInt(3) / 10.0f;
    }
}
