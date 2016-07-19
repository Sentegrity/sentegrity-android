package com.sentegrity.core_detection.constants;

/**
 * Created by dmestrov on 22/03/16.
 */
public final class DNEStatusCode{

    public static final int OK = 0;
    public static final int UNAUTHORIZED = 1;
    public static final int UNSUPPORTED = 2;
    public static final int UNAVAILABLE = 3;
    public static final int DISABLED = 4;
    public static final int EXPIRED = 5;
    public static final int ERROR = 6;
    public static final int NO_DATA = 7;
    public static final int INVALID = 8;

    public static String toString(int statusCode){
        switch (statusCode){
            case 0:
                return "OK";
            case 1:
                return "UNAUTHORIZED";
            case 2:
                return "UNSUPPORTED";
            case 3:
                return "UNAVAILABLE";
            case 4:
                return "DISABLED";
            case 5:
                return "EXPIRED";
            case 6:
                return "ERROR";
            case 7:
                return "NO_DATA";
            case 8:
                return "INVALID";
        }
        return "" + statusCode;
    }
}
