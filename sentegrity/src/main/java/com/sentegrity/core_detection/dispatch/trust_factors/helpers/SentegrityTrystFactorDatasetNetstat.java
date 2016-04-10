package com.sentegrity.core_detection.dispatch.trust_factors.helpers;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 10/04/16.
 */
public class SentegrityTrystFactorDatasetNetstat {

    private static final String COMMAND_TCP4 = "/proc/net/tcp";
    private static final String COMMAND_TCP6 = "/proc/net/tcp6";
    private static final String COMMAND_UDP4 = "/proc/net/udp";
    private static final String COMMAND_UDP6 = "/proc/net/udp6";

    private static List get(int ipversion, int sockettype, String s) throws IOException {
        BufferedReader bufferedreader;
        String line;
        ArrayList arraylist;
        arraylist = new ArrayList();
        bufferedreader = null;
        line = null;
        FileReader reader = new FileReader(new File(s));
        bufferedreader = new BufferedReader(reader, 8192);
        String[] list;

        while ((line = bufferedreader.readLine()) != null) {

            if (line == null) {
                IOUtils.closeQuietly(bufferedreader);
                IOUtils.closeQuietly(reader);
                return arraylist;
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

            connection.localIp = getIP(list[1]);
            connection.localPort = getPort(list[1]);

            if (TCPState.hasRemote(connection.state)) {
                connection.remoteIp = getIP(list[2]);
                connection.remotePort = getPort(list[2]);
            }

            arraylist.add(connection);
        }

        IOUtils.closeQuietly(bufferedreader);
        IOUtils.closeQuietly(reader);

        return arraylist;
    }

    public static List getTcp4()
            throws IOException {
        return get(IpVersion.IPv4, SocketType.TCP, COMMAND_TCP4);
    }

    public static List getTcp6()
            throws IOException {
        return get(IpVersion.IPv6, SocketType.TCP, COMMAND_TCP6);
    }

    public static List getUdp4()
            throws IOException {
        return get(IpVersion.IPv4, SocketType.UDP, COMMAND_UDP4);
    }

    public static List getUdp6()
            throws IOException {
        return get(IpVersion.IPv6, SocketType.UDP, COMMAND_UDP6);
    }

    //TODO: whaaat? =)
//    private static IpEndPoint parseIpAddress(String s) {
//        s = s.split(":");
//        if (s.length == 2)goto _L2;else goto _L1
//        _L1:
//        return null;
//        _L2:
//        String s1;
//        String s2 = s[0];
//        s1 = s[1];
//        byte abyte0[] = parseIpv4AddressHexString(s2);
//        s = abyte0;
//        if (abyte0 != null) {
//            break; /* Loop/switch isn't completed */
//        }
//        s = parseIpv6AddressHexString(s2);
//        if (s == null)goto _L1;else goto _L3
//        _L3:
//        s = IpAddressBytes.wrapBytes(s);
//        int i;
//        try {
//            i = Integer.parseInt(s1, 16);
//        }
//        // Misplaced declaration of an exception variable
//        catch (String s) {
//            return null;
//        }
//        return new IpEndPoint(s, i);
//    }

    private static String getIP(String ipWithPort) {
        String hexValue = ipWithPort.split(":")[0];
        String ip = "";

        for (int i = hexValue.length(); i > 0; i = i - 2) {
            ip = ip + Integer.valueOf(hexValue.substring(i - 2, i), 16) + ".";
        }
        ip = ip.substring(0, ip.length() - 1);

        return ip;
    }

    private static String getPort(String ipWithPort) {
        return "" + Integer.valueOf(ipWithPort.split(":")[1], 16);
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
            }
            // Misplaced declaration of an exception variable
            catch (Exception e) {
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
