package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.os.SystemClock;
import android.text.TextUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.netstat.ActiveConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchNetstat {

    public static SentegrityTrustFactorOutput badDst(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus());
            return output;
        }

        List<ActiveConnection> connections = SentegrityTrustFactorDatasets.getInstance().getNetstatData();

        if (SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus());
            return output;
        }

        if (connections == null || connections.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        for (ActiveConnection connection : connections) {
            //we replaced remoteHost with remoteIp

            if (connection.isListening() || connection.isLoopBack() || TextUtils.isEmpty(/*connection.remoteHost*/connection.remoteIp)) {
                continue;
            }

            for (Object badDst : payload) {
                /*if (connection.remoteHost.contains((CharSequence) badDst)) {
                    if (!outputList.contains(connection.remoteHost))
                        outputList.add(connection.remoteHost);
                }*/
                if (connection.remoteIp.contains((CharSequence) badDst)) {
                    if (!outputList.contains(connection.remoteIp))
                        outputList.add(connection.remoteIp);
                }
            }
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput priviledgedPort(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus());
            return output;
        }

        List<ActiveConnection> connections = SentegrityTrustFactorDatasets.getInstance().getNetstatData();

        if (SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus());
            return output;
        }

        if (connections == null || connections.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<Integer> payloadInt = new ArrayList<>();
        for(Object port : payload){
            payloadInt.add((int) (double) port);
        }

        for (ActiveConnection connection : connections) {
            if (!connection.isListening()) {
                continue;
            }

            for (int badPort : payloadInt) {
                if (connection.getLocalPort() == badPort) {
                    if (!outputList.contains(connection.localPort))
                        outputList.add(connection.localPort);
                }
            }
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput newService(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus());
            return output;
        }

        List<ActiveConnection> connections = SentegrityTrustFactorDatasets.getInstance().getNetstatData();

        if (SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus());
            return output;
        }

        if (connections == null || connections.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        for (ActiveConnection connection : connections) {
            if (!connection.isListening()) {
                continue;
            }

            if (!outputList.contains(connection.localPort))
                outputList.add(connection.localPort);
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput dataExfiltration(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        Long dataXfer = SentegrityTrustFactorDatasets.getInstance().getDataXferInfo();

        if(dataXfer == null){
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        long uptime = 0;
        uptime = SystemClock.uptimeMillis();

        if(uptime <= 0) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }
        //to seconds
        uptime /= 1000;

        int timeInterval = 0;
        timeInterval = (int) (double) ((LinkedTreeMap)payload.get(0)).get("secondsInterval");

        if(timeInterval == 0){
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        int dataMax = 0;
        dataMax = (int) (double) ((LinkedTreeMap)payload.get(0)).get("maxSentMB");

        if(dataMax == 0){
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        int timeSlots = 0;
        timeSlots = Math.round(uptime / timeInterval);

        if(timeSlots < 1){
            return output;
        }

        int dataSentPerTimeSlot = (int) Math.ceil(dataXfer / (float) timeSlots);

        if(dataSentPerTimeSlot < 1){
            return output;
        }

        //we need it in bytes
        if(dataSentPerTimeSlot > dataMax * 1000000){
            outputList.add("exfil");
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput unencryptedTraffic(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getNetstatDataDNEStatus());
            return output;
        }

        List<ActiveConnection> connections = SentegrityTrustFactorDatasets.getInstance().getNetstatData();

        if (SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus());
            return output;
        }

        if (connections == null || connections.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<Integer> payloadInt = new ArrayList<>();
        for(Object port : payload){
            payloadInt.add((int) (double) port);
        }

        for (ActiveConnection connection : connections) {
            if (connection.isLoopBack()) {
                continue;
            }

            if (Integer.valueOf(connection.remotePort) == 443) {
                continue;
            }

            for (int badDstPort : payloadInt) {
                if (connection.getRemotePort() == badDstPort) {
                    /*remoteHost is replaced with remoteIp and added to output
                    if (!outputList.contains(connection.remoteHost)) {
                        String host = connection.remoteHost;
                        if (host == null)
                            continue;

                        String[] components = host.split("\\.");
                        if (host.length() > 15) {
                            if (components.length > 1) {
                                host = components[components.length - 2] + "." + components[components.length - 1];
                            }
                        } else {
                            if (components.length == 4) {
                                host = components[components.length - 4] + "." + components[components.length - 3] + "." + components[components.length - 2];
                            }
                        }

                        if (!outputList.contains(host)) {
                            outputList.add(host);
                        }
                    }*/
                    if(!outputList.contains(connection.remotePort)){
                        outputList.add(connection.remotePort);
                    }
                }
            }
        }

        output.setOutput(outputList);

        return output;
    }
}
