package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.route.ActiveRoute;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchRoute {

    public static SentegrityTrustFactorOutput vpnUp(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        /*
        List<ActiveRoute> routes = SentegrityTrustFactorDatasets.getInstance().getRouteInfo();

        if (routes == null || routes.size() == 0) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        try {
            for (ActiveRoute route : routes) {
                for (Object vpnInterface : payload) {
                    String vpnIface = (String) vpnInterface;
                    if (route.iface.contains(vpnIface)) {
                        if (route.gateway.contains(".")) {
                            if (!outputList.contains(route.gateway)) {
                                outputList.add(route.gateway);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //TODO: why not error?
            return null;
        }*/

        List<NetworkInterface> interfaces = SentegrityTrustFactorDatasets.getInstance().getRouteInfo();

        if (interfaces == null || interfaces.size() == 0) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        payload.add("tun0");
        List<NetworkInterface> vpnInterfaces = new ArrayList<>();
        for (NetworkInterface networkInterface : interfaces) {
            try {
                if (!networkInterface.isUp() || networkInterface.getInterfaceAddresses().size() == 0)
                    continue;
            } catch (SocketException e) {
                continue;
            }
            for (Object vpnInterface : payload) {
                String vpnIface = (String) vpnInterface;
                //TODO add "tun0" to payload
                if (networkInterface.getName().contains(vpnIface)) {
                    vpnInterfaces.add(networkInterface);
                    break;
                }
            }
        }

        for(NetworkInterface networkInterface : vpnInterfaces){
            for (Enumeration<InetAddress> en =
                 networkInterface.getInetAddresses(); en.hasMoreElements();) {
                InetAddress address = en.nextElement();
                if (!address.isLoopbackAddress()) {
                    if(!outputList.contains(address.getHostAddress()))
                        outputList.add(address.getHostAddress());
                }
            }
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput noRoute(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        /*List<ActiveRoute> routes = SentegrityTrustFactorDatasets.getInstance().getRouteInfo();
        boolean defaultRoute = false;

        if (routes == null || routes.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        try {
            for (ActiveRoute route : routes) {
                if (route.isDefault) {
                    defaultRoute = true;
                    break;
                }
            }
            if (!defaultRoute)
                outputList.add("noRoute");
        } catch (Exception e) {
            //TODO: why not error?
            return null;
        }*/

        Boolean noConnection = SentegrityTrustFactorDatasets.getInstance().hasInternetConnection();

        if(noConnection == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }else if(!noConnection){
            outputList.add("no-route");
        }

        output.setOutput(outputList);

        return output;
    }
}
