package com.sentegrity.core_detection.constants;

/**
 * Created by dmestrov on 22/03/16.
 */
public enum AttributingClass{
    SYSTEM_BREACH(0),
    SYSTEM_POLICY(1),
    SYSTEM_SECURITY(2),
    USER_POLICY(3),
    USER_ANOMALY(4);

    int id;
    AttributingClass(int id) {
        this.id = id;
    }

    public static AttributingClass getByID(int id){
        for(AttributingClass ac : values()){
            if(ac.id == id)
                return ac;
        }
        return SYSTEM_BREACH;
    }
}