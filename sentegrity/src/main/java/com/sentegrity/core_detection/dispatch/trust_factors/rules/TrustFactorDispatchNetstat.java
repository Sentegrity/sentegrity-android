package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrystFactorDatasetNetstat;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchNetstat {

    public static SentegrityTrustFactorOutput badDst(List<Object> payload){
        List list;
        try {
            list = SentegrityTrystFactorDatasetNetstat.getTcp4();
            Log.d("test", "test");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput priviledgedPort(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput newService(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput dataExfiltration(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }

    public static SentegrityTrustFactorOutput unencryptedTraffic(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
