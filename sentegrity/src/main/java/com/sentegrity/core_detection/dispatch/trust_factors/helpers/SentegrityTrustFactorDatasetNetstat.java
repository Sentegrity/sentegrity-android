package com.sentegrity.core_detection.dispatch.trust_factors.helpers;

import android.text.TextUtils;
import android.util.Log;

import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.ActiveConnection;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.IpVersion;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.SocketType;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.TCPState;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 10/04/16.
 */
public class SentegrityTrustFactorDatasetNetstat {

    private static final String COMMAND_TCP4 = "/proc/net/tcp";
    private static final String COMMAND_TCP6 = "/proc/net/tcp6";
    private static final String COMMAND_UDP4 = "/proc/net/udp";
    private static final String COMMAND_UDP6 = "/proc/net/udp6";

//    Other way to do it??
//    Process process = Runtime.getRuntime().exec(new String[]{"netstat"});
//    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//    List<String> lines = new ArrayList<>();
//    String line;
//    while((line = in.readLine()) != null)
//        lines.add(line);

    private static List<ActiveConnection> get(IpVersion ipversion, SocketType sockettype, String s) throws IOException {
        BufferedReader bufferedreader;
        String line;
        List<ActiveConnection> connections = new ArrayList<>();
        FileReader reader = new FileReader(new File(s));
        bufferedreader = new BufferedReader(reader, 8192);
        String[] list;

        long start = System.nanoTime();
        while ((line = bufferedreader.readLine()) != null) {

            if (line == null) {
                IOUtils.closeQuietly(bufferedreader);
                IOUtils.closeQuietly(reader);
                return connections;
            }

            line = line.trim();

            if (line.startsWith("sl"))
                continue;

            list = line.split("\\s+");
            if (list.length < 8)
                continue;

            ActiveConnection connection = new ActiveConnection();
            connection.ipVersion = ipversion;
            connection.socketType = sockettype;

            if (sockettype == SocketType.TCP)
                connection.state = TCPState.getByState(list[3]);

            if (sockettype == SocketType.UDP)
                connection.state = "UDP";


            InetAddress localInetAddress = getInetAddress(ipversion, list[1]);
            if(localInetAddress == null)
                continue;

            connection.localIp = localInetAddress.getHostAddress();
            connection.localPort = getPort(list[1]);
            connection.isLoopBack = localInetAddress.isLoopbackAddress();

            if(!connection.isListening()){
                InetAddress remoteInetAddress = getInetAddress(ipversion, list[2]);
                if(remoteInetAddress != null){

                    connection.remoteIp = remoteInetAddress.getHostAddress();
                    //we'll go with ip address instead of host name - this requires to go online and check it... it can take a while
                    //connection.remoteHost = remoteInetAddress.getHostName();
                    connection.remotePort = getPort(list[2]);
                }
            }

            connections.add(connection);
        }

        IOUtils.closeQuietly(bufferedreader);
        IOUtils.closeQuietly(reader);

        Log.d("test", "netstat " + (System.nanoTime() - start));
        return connections;
    }

    public static List<ActiveConnection> getTcp4()
            throws IOException {
        return get(IpVersion.IPv4, SocketType.TCP, COMMAND_TCP4);
    }

    public static List<ActiveConnection> getTcp6()
            throws IOException {
        return get(IpVersion.IPv6, SocketType.TCP, COMMAND_TCP6);
    }

    public static List<ActiveConnection> getUdp4()
            throws IOException {
        return get(IpVersion.IPv4, SocketType.UDP, COMMAND_UDP4);
    }

    public static List<ActiveConnection> getUdp6()
            throws IOException {
        return get(IpVersion.IPv6, SocketType.UDP, COMMAND_UDP6);
    }

    private static String getPort(String ipWithPort) {
        if (TextUtils.isEmpty(ipWithPort))
            return null;
        String[] s = ipWithPort.split(":");
        if (s.length != 2)
            return null;
        return "" + Integer.valueOf(s[1], 16);
    }

    private static InetAddress getInetAddress(IpVersion ipVersion, String ipWithPort){
        if (TextUtils.isEmpty(ipWithPort))
            return null;
        String[] s = ipWithPort.split(":");
        if (s.length != 2)
            return null;

        byte[] address = null;
        if(ipVersion == IpVersion.IPv4)
            address = parseIpv4AddressHexString(s[0]);
        else if(ipVersion == IpVersion.IPv6)
            address = parseIpv6AddressHexString(s[0]);

        if(address == null)
            return null;

        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static byte[] parseIpv4AddressHexString(String s) {
        if (s.length() != 8) {
            return null;
        }
        byte abyte0[] = new byte[4];
        int i = 0;
        while (i < 4) {
            int j = i * 2;
            try {
                abyte0[i] = (byte) Integer.parseInt(s.substring(j, j + 2), 16);
            } catch (Exception e) {
                return null;
            }
            i++;
        }
        swapEndianUint32(abyte0, 0);
        return abyte0;
    }

    private static byte[] parseIpv6AddressHexString(String s) {
        if (s.length() != 32) {
            return null;
        }
        byte abyte0[] = new byte[16];
        int i = 0;
        while (i < 16) {
            int j = i * 2;
            try {
                abyte0[i] = (byte) Integer.parseInt(s.substring(j, j + 2), 16);
            } catch (Exception e) {
                return null;
            }
            i++;
        }
        swapEndianUint32(abyte0, 0);
        swapEndianUint32(abyte0, 4);
        swapEndianUint32(abyte0, 8);
        swapEndianUint32(abyte0, 12);
        return abyte0;
    }

    private static void swapEndianUint32(byte abyte0[], int i) {
        byte byte0 = abyte0[i + 0];
        abyte0[i + 0] = abyte0[i + 3];
        abyte0[i + 3] = byte0;
        byte0 = abyte0[i + 1];
        abyte0[i + 1] = abyte0[i + 2];
        abyte0[i + 2] = byte0;
    }
}
