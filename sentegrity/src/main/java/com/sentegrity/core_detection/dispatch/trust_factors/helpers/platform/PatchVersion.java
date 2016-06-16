package com.sentegrity.core_detection.dispatch.trust_factors.helpers.platform;

/**
 * Created by dmestrov on 16/06/16.
 */
public class PatchVersion {
    private String model;
    private String sdkVersion;
    private String carrier;
    private String patchName;

    public PatchVersion(String line){
        String[] list = line.split(",");
        if(list.length != 4)
            return;

        model = list[0].trim();
        carrier = list[1].trim();
        sdkVersion = list[2].trim();
        patchName = list[3].trim();
    }

    public String getModel() {
        return model;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getPatchName() {
        return patchName;
    }
}
