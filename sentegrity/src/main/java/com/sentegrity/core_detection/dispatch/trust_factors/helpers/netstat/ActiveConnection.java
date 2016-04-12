package com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat;

/**
 * Created by dmestrov on 10/04/16.
 */
public class ActiveConnection {

    public int ipVersion;
    public String localIp;
    public String localPort;
    public String remoteIp;
    public String remoteHost;
    public String remotePort;
    public String state;
    public boolean isLoopBack;
    public int socketType;
    public int uid;
    public int totalTX;
    public int totalRX;

    public ActiveConnection(){

    }
}
