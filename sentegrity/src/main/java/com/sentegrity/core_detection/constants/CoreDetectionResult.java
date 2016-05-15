package com.sentegrity.core_detection.constants;

/**
 * Created by dmestrov on 15/05/16.
 */
public class CoreDetectionResult {
    public final static int USER_ANOMALY = 1;
    public final static int POLICY_VIOLATION = 2;
    public final static int HIGH_RISK_DEVICE = 3;
    public final static int TRANSPARENT_AUTH_SUCCESS = 4;
    public final static int TRANSPARENT_AUTH_NEW_KEY = 5;
    public final static int COREDETECTION_ERROR = 6;
    public final static int TRANSPARENT_AUTH_ERROR = 7;
    public final static int DEVICE_COMPROMISE = 8;
    public final static int TRANSPARENT_AUTH_ENTROPY_LOW = 9;
}