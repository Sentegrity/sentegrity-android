package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.route.ActiveRoute;

import java.util.ArrayList;
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
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput noRoute(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        List<ActiveRoute> routes = SentegrityTrustFactorDatasets.getInstance().getRouteInfo();
        boolean defaultRoute = false;

        if (routes == null || routes.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        try {
            for (ActiveRoute route : routes) {
                if(route.isDefault){
                    defaultRoute = true;
                    break;
                }
            }
            if(!defaultRoute)
                outputList.add("noRoute");
        } catch (Exception e) {
            //TODO: why not error?
            return null;
        }

        output.setOutput(outputList);

        return output;
    }
}
