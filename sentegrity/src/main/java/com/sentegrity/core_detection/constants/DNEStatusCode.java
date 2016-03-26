package com.sentegrity.core_detection.constants;

/**
 * Created by dmestrov on 22/03/16.
 */
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

    public int getId(){
        return id;
    }
}
