package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchBluetooth {

    //TODO: rename to connectedDevices (update policy accordingly)
    public static SentegrityTrustFactorOutput connectedClassicDevice(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus());
            return output;
        }

        Set<String> bluetoothDevices = SentegrityTrustFactorDatasets.getInstance().getPairedBTDevices();

        if (SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getPairedBTDNEStatus());
            return output;
        }

        if (bluetoothDevices == null || bluetoothDevices.size() < 1) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        for (String device : bluetoothDevices) {
            Log.d("bluetoothDevices", "name: " + device);
            outputList.add(device);
        }

        output.setOutput(outputList);

        return output;
    }

    //TODO: rename to discoveredDevices (update policy accordingly)
    public static SentegrityTrustFactorOutput discoveredBLEDevice(List<Object> payload) {
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if (SentegrityTrustFactorDatasets.getInstance().getScannedBTDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getScannedBTDNEStatus() != DNEStatusCode.EXPIRED) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getScannedBTDNEStatus());
            return output;
        }

        Set<String> bluetoothDevices = SentegrityTrustFactorDatasets.getInstance().getScannedBTDevices();

        if (SentegrityTrustFactorDatasets.getInstance().getScannedBTDNEStatus() != DNEStatusCode.OK) {
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getScannedBTDNEStatus());
            return output;
        }

        if (bluetoothDevices == null || bluetoothDevices.size() < 1) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        for (String device : bluetoothDevices) {
            outputList.add(device);
        }

        output.setOutput(outputList);

        return output;
    }
}
