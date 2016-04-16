package com.sentegrity.core_detection.dispatch.trust_factors.helpers;


import android.text.TextUtils;
import android.util.Log;

import com.sentegrity.core_detection.dispatch.trust_factors.helpers.route.ActiveRoute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dmestrov on 13/04/16.
 */
public class SentegrityTrustFactorDatasetRoute {

    public static List<ActiveRoute> getAllRoutes() throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{"ip", "route", "list", "table", "all"});
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<ActiveRoute> routes = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String line;
        long start = System.nanoTime();

        Pattern patternIface = Pattern.compile("dev\\W+(\\w+)");
        Pattern patternGateway = Pattern.compile("via\\W+(\\w+)");
        Matcher m;

        while ((line = in.readLine()) != null) {
            //if there's no gateway or interface, we can just skip this
            //default destination and vpn need gateway
            if(!line.contains("dev") || !line.contains("via"))
                continue;

//            int viaIndex = line.indexOf("via");
//            if(viaIndex == -1)
//                continue;
//
//            int devIndex = line.indexOf("dev");
//            if(devIndex == -1)
//                continue;

            ActiveRoute route = new ActiveRoute();
            route.isDefault = line.startsWith("def");

            m = patternGateway.matcher(line);
            if(!m.find())
                continue;
            route.gateway = m.group(1);

            m = patternIface.matcher(line);
            if(!m.find())
                continue;
            route.iface = m.group(1);

            lines.add(line);
            routes.add(route);
        }

        Log.d("route", "got routes in: " + (System.nanoTime() - start));
        return routes;
    }

    @Deprecated
    public static List<ActiveRoute> getRoutes() throws IOException {
        //TODO: if fails try other command --> route -n
        Process process = Runtime.getRuntime().exec(new String[]{"netstat", "-rn"});
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<ActiveRoute> routes = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            String[] list = line.split("\\s+");

            if (list.length != 8)
                continue;
            if ("Destination".equals(list[0]) || "Iface".equals(list[7]))
                continue;

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

            lines.add(line);
            routes.add(route);
        }

        return routes;
    }

    //AF_INET, INADDR_ANY ??? how do we know it's default
    private static boolean isDefault(String destination) {
        if (TextUtils.isEmpty(destination))
            return false;
        if ("default".equals(destination) || "0.0.0.0".equals(destination) || "::/0".equals(destination) || "*".equals(destination))
            return true;
        return false;
    }
}
