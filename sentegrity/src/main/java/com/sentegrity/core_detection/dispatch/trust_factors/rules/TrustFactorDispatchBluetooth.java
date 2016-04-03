package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchBluetooth {

    public static SentegrityTrustFactorOutput discoveredBLEDevice(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        //TODO: check for previous bt callback status
        /*if(SentegrityTrustFactorDatasets.getInstance().getConnectedClassicDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getConnectedClassicDNEStatus() != DNEStatusCode.EXPIRED){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getConnectedClassicDNEStatus());
            return output;
        }*/

        List<String> bluetoothDevices = SentegrityTrustFactorDatasets.getInstance().getClassicBTInfo();

        if (bluetoothDevices == null || bluetoothDevices.size() < 1) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        for(String device : bluetoothDevices){
            outputList.add(device);
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput connectedClassicDevice(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        //TODO: check for previous bt callback status
        /*if(SentegrityTrustFactorDatasets.getInstance().getDiscoveredBLEDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getDiscoveredBLEDNEStatus() != DNEStatusCode.EXPIRED){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getDiscoveredBLEDNEStatus());
            return output;
        }*/

        List<String> bluetoothDevices = SentegrityTrustFactorDatasets.getInstance().getDiscoveredBLEInfo();

        if (bluetoothDevices == null || bluetoothDevices.size() < 1) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        for(String device : bluetoothDevices){
            outputList.add(device);
        }

        output.setOutput(outputList);

        return output;
    }
}
