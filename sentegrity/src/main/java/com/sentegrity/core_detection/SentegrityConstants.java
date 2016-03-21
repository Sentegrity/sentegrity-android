package com.sentegrity.core_detection;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityConstants {

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


    public enum DNEStatusCode{
        OK(0),
        UNAUTHORIZED(1),
        UNSUPPORTED(2),
        UNAVAILABLE(3),
        DISABLED(4),
        EXPIRED(5),
        ERROR(6),
        NO_DATA(7),
        INVALID(8);

        int id;
        DNEStatusCode(int id) {
            this.id = id;
        }

        public static DNEStatusCode getByID(int id){
            for(DNEStatusCode sc : values()){
                if(sc.id == id)
                    return sc;
            }
            return OK;
        }
    }
}
