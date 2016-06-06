package com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat;

import android.text.TextUtils;

/**
 * Created by dmestrov on 10/04/16.
 */
public enum TCPState {

    ESTABLISHED("ESTABLISHED", 0, "01"),
    SYN_SENT("SYN_SENT", 1, "02"),
    SYN_RECV("SYN_RECV", 2, "03"),
    FIN_WAIT1("FIN_WAIT1", 3, "04"),
    FIN_WAIT2("FIN_WAIT2", 4, "05"),
    TIME_WAIT("TIME_WAIT", 5, "06"),
    CLOSE("CLOSE", 6, "07"),
    CLOSE_WAIT("CLOSE_WAIT", 7, "08"),
    LAST_ACK("LAST_ACK", 8, "09"),
    LISTEN("LISTEN", 9, "0A"),
    CLOSING("CLOSING", 10, "0B");

    private final String state;
    private final String stateString;

    TCPState(String state, int i, String s) {
        this.state = state;
        this.stateString = s;
    }

    public String toString(){
        return state;
    }

    public static String getByState(String state){
        for(TCPState s : values()){
            if(TextUtils.equals(s.stateString, state))
                return s.state;
        }
        return "?";
    }
}
