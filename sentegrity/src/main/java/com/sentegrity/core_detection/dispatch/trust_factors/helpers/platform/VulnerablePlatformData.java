package com.sentegrity.core_detection.dispatch.trust_factors.helpers.platform;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by dmestrov on 14/06/16.
 */
public class VulnerablePlatformData {
    private String manufacturer;
    private String regex;
    private String description;
    private boolean allDevices;

    public VulnerablePlatformData(String line) {
        try {
            JSONArray jsonArray = new JSONArray(line);

            manufacturer = jsonArray.getString(0);
            regex = jsonArray.getString(1);
            description = jsonArray.getString(2);

            if(TextUtils.equals(manufacturer, "*"))
                allDevices = true;

        } catch (Exception e) {
            description = line;
        }

    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAllDevices() {
        return allDevices;
    }
}
