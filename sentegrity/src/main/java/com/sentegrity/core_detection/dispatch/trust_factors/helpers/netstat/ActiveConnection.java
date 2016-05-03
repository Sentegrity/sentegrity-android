package com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat;

import android.text.TextUtils;

/**
 * Created by dmestrov on 10/04/16.
 */
public class ActiveConnection {

    public IpVersion ipVersion;
    public String localIp;
    public String localPort;
    public String remoteIp;
    @Deprecated
    public String remoteHost;
    public String remotePort;
    public String state;
    public boolean isLoopBack;
    public SocketType socketType;
    public int uid;
    public int totalTX;
    public int totalRX;

    public boolean isListening(){
        return !TextUtils.isEmpty(state) && "listen".equals(state.toLowerCase());
    }

    public boolean isLoopBack(){
        return isLoopBack;
    }

    public ActiveConnection(){

    }

    public int getRemotePort() {
        if(TextUtils.isEmpty(remotePort))
            return 0;
        return Integer.parseInt(remotePort);
    }

    public int getLocalPort() {
        if(TextUtils.isEmpty(localPort))
            return 0;
        return Integer.parseInt(localPort);
    }
}
