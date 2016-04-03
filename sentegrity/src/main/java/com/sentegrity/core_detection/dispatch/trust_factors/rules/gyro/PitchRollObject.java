package com.sentegrity.core_detection.dispatch.trust_factors.rules.gyro;

import java.util.Random;

/**
 * Created by dmestrov on 03/04/16.
 */
public class PitchRollObject {
    public float pitch;
    public float roll;

    public PitchRollObject(){
        Random r = new Random();
        pitch = r.nextInt(3) / 10.0f;
        roll = r.nextInt(3) / 10.0f;
    }
}
