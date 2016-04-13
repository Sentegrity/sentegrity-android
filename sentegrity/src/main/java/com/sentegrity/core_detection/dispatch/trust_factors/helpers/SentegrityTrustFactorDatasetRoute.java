package com.sentegrity.core_detection.dispatch.trust_factors.helpers;


import android.content.Context;
import android.net.RouteInfo;
import android.text.TextUtils;

import com.sentegrity.core_detection.dispatch.trust_factors.helpers.route.ActiveRoute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 13/04/16.
 */
public class SentegrityTrustFactorDatasetRoute {

    public static List<ActiveRoute> getRoutes() throws IOException {
        //TODO: if fails try other command --> route -n
        Process process = Runtime.getRuntime().exec(new String[]{"netstat", "-rn"});
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<ActiveRoute> routes = new ArrayList<>();
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            String[] list = line.split("\\s+");

            if (list.length != 8)
                continue;
            if ("Destination".equals(list[0]) || "Iface".equals(list[7]))
                continue;

//            This is list for vanilla Android 6.0
//            list[0] --> Destination
//            list[1] --> Gateway
//            list[2] --> Genmask
//            list[3] --> Flags
//            list[4] --> MSS (netstat), Metric (route)
//            list[5] --> Window (netstat), Ref (route)
//            list[6] --> irtt (netstat), Use (route)
//            list[7] --> Iface

            ActiveRoute route = new ActiveRoute();
            route.isDefault = isDefault(list[0]);
            route.gateway = list[1];
            route.iface = list[7];

            routes.add(route);
        }

        return routes;
    }

    //AF_INET, INADDR_ANY ??? how do we know it's default
    private static boolean isDefault(String destination) {
        if (TextUtils.isEmpty(destination))
            return false;
        if ("0.0.0.0".equals(destination) || "::/0".equals(destination) || "*".equals(destination))
            return true;
        return false;
    }
}
