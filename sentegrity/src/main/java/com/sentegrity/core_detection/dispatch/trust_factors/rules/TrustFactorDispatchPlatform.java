package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.platform.PatchVersion;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.platform.VulnerablePlatformData;
import com.sentegrity.core_detection.utilities.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchPlatform {

    public static SentegrityTrustFactorOutput vulnerableVersion(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        List<VulnerablePlatformData> vulnerablePlatformDataList = SentegrityTrustFactorDatasets.getInstance().getVulnerablePlatformData();

        if (vulnerablePlatformDataList == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String manufacturer = Build.MANUFACTURER;
        String currentVersion = Build.VERSION.RELEASE;

        boolean isVulnerable = false;

        Pattern pattern;
        Matcher m;

        for (int i = 0; i < vulnerablePlatformDataList.size(); i++) {
            VulnerablePlatformData data = vulnerablePlatformDataList.get(i);
            if(TextUtils.isEmpty(data.getRegex()))
                continue;
            //check if need to check for manufacturer
            if(!TextUtils.equals(data.getManufacturer(), "*")){
                //check if we have correct manufacturer for this regex, otherwise just continue
                if(!TextUtils.equals(manufacturer, data.getManufacturer())){
                    continue;
                }
            }
            pattern = Pattern.compile(data.getRegex());
            m = pattern.matcher(currentVersion);
            if(!m.find())
                continue;
            else{
                isVulnerable = true;
                break;
            }
        }

        if(isVulnerable){
            outputList.add(currentVersion);
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput outdatedVersion(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput versionAllowed(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        List<PatchVersion> patchVersions = SentegrityTrustFactorDatasets.getInstance().getPatchVersionsList();

        if (patchVersions == null) {
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        String carrier = SentegrityTrustFactorDatasets.getInstance().getCarrierConnectionName();
        String currentVersion = Build.VERSION.RELEASE;
        String model = Build.MODEL;
        String patch = Build.ID;

        Boolean isVulnerable = null;

        for (int i = 0; i < patchVersions.size(); i++) {
            PatchVersion data = patchVersions.get(i);
            if(TextUtils.isEmpty(data.getModel()))
                continue;

            if(!TextUtils.equals(data.getModel(), model))
                continue;

            if(!TextUtils.equals(data.getSdkVersion(), currentVersion))
                continue;

            //check if need to check for manufacturer
            if(!TextUtils.equals(data.getCarrier(), "*")){
                //check if we have correct manufacturer for this regex, otherwise just continue
                if(!TextUtils.equals(data.getCarrier(), carrier)){
                    continue;
                }
            }

            isVulnerable = !TextUtils.equals(data.getPatchName(), patch);

            break;
        }

        if(isVulnerable == null){
            output.setStatusCode(DNEStatusCode.DISABLED);
            return output;
        }else if(isVulnerable){
            outputList.add(patch);
        }

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput shortUptime(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        long uptime = SystemClock.elapsedRealtime();

        if(uptime <= 0) {
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        int secondsInHour = 3600;

        int hoursUp = (int) ((uptime / 1000.0f) / secondsInHour);

        String hoursUpString;

        if(hoursUp < (double) ((LinkedTreeMap)payload.get(0)).get("minimumHoursUp")){
            hoursUpString = "up" + hoursUp;
            outputList.add(hoursUpString);
        }

        output.setOutput(outputList);

        return output;
    }
}
