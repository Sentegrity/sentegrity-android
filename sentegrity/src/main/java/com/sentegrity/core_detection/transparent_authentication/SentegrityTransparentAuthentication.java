package com.sentegrity.core_detection.transparent_authentication;

import android.content.Context;
import android.text.TextUtils;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.constants.AttributingSubClass;
import com.sentegrity.core_detection.constants.CoreDetectionResult;
import com.sentegrity.core_detection.constants.PostAuthAction;
import com.sentegrity.core_detection.constants.PreAuthAction;
import com.sentegrity.core_detection.crypto.SentegrityCrypto;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.startup.SentegrityTransparentAuthObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityTransparentAuthentication {


    private static SentegrityTransparentAuthentication sInstance;

    private SentegrityTransparentAuthentication() {
    }

    public static SentegrityTransparentAuthentication getInstance() {
        if (sInstance == null) {
            sInstance = new SentegrityTransparentAuthentication();
        }
        return sInstance;
    }

    public SentegrityTrustScoreComputation attemptTransparentAuthentication(SentegrityTrustScoreComputation computationResults, SentegrityPolicy policy){

        if(computationResults.getTransparentAuthenticationTrustFactorOutputs() == null){
            computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_ERROR);
            computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
            computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
            return computationResults;
        }

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        if(startup == null){
            computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_ERROR);
            computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
            computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
            return computationResults;
        }

        String candidateTransparentAuthKeyRawOutputString = "";

        for(SentegrityTrustFactorOutput trustFactorOutput : computationResults.getTransparentAuthenticationTrustFactorOutputs()){
            for(String output : trustFactorOutput.getOutput()){
                candidateTransparentAuthKeyRawOutputString += ("," + output);
            }
        }

        boolean transparentKeyError = false;
        computationResults.setCandidateTransparentKey(SentegrityCrypto.getInstance().getTransparentKeyForTrustFactorOutput(candidateTransparentAuthKeyRawOutputString));

        if(computationResults.getCandidateTransparentKey() == null){
            if(transparentKeyError){

            }else{

            }
            computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_ERROR);
            computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
            computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
            return computationResults;
        }

        boolean shaHashError = false;
        computationResults.setCandidateTransparentKeyHashString(SentegrityCrypto.getInstance().createSHA1HashOfData(computationResults.getCandidateTransparentKey()));

        if(computationResults.getCandidateTransparentKeyHashString() == null || computationResults.getCandidateTransparentKeyHashString().length() == 0){
            if(shaHashError){

            }else{

            }
        }

        computationResults.setCandidateTransparentKeyHashString(computationResults.getCandidateTransparentKeyHashString() + "-" + candidateTransparentAuthKeyRawOutputString);

        computationResults.setFoundTransparentMatch(false);

        List<SentegrityTransparentAuthObject> currentTransparentAuthKeyObjects = startup.getTransparentAuthKeyObjects();
        List<SentegrityTransparentAuthObject> decayedTransparentAuthKeyObjects;

        if(currentTransparentAuthKeyObjects.size() > 0){
            decayedTransparentAuthKeyObjects = performMetricBasedDecay(currentTransparentAuthKeyObjects, policy);

            for(SentegrityTransparentAuthObject storedTransparentAuthObject : decayedTransparentAuthKeyObjects){

                if(TextUtils.equals(storedTransparentAuthObject.getTransparentKeyPBKDF2HashString(), computationResults.getCandidateTransparentKeyHashString())) {
                    computationResults.setFoundTransparentMatch(true);

                    int origHitCount = storedTransparentAuthObject.getHitCount();

                    storedTransparentAuthObject.setHitCount(origHitCount + 1);

                    storedTransparentAuthObject.setLastTime(SentegrityTrustFactorDatasets.getInstance().getRunTime());

                    computationResults.setMatchingTransparentAuthenticationObject(storedTransparentAuthObject);

                    computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_SUCCESS);
                    computationResults.setPreAuthenticationAction(PreAuthAction.TRANSPARENTLY_AUTHENTICATE);

                    computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
                    break;
                }

            }

            startup.setTransparentAuthKeyObjects(decayedTransparentAuthKeyObjects);
        }

        if(computationResults.getCoreDetectionResult() != CoreDetectionResult.TRANSPARENT_AUTH_SUCCESS
                && computationResults.getCoreDetectionResult() != CoreDetectionResult.TRANSPARENT_AUTH_ERROR
                && !computationResults.isFoundTransparentMatch()){
            computationResults.setCoreDetectionResult(CoreDetectionResult.TRANSPARENT_AUTH_NEW_KEY);
            computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
            computationResults.setPostAuthenticationAction(PostAuthAction.CREATE_TRANSPARENT_KEY);
        }

        return computationResults;
    }

    public List<SentegrityTransparentAuthObject> performMetricBasedDecay(List<SentegrityTransparentAuthObject> currentTransparentAuthKeyObjects, SentegrityPolicy policy) {
        final double secondsInDay = 86400.0;
        double daysSinceCreation = 0.0;
        double hitsPerDay = 0.0;

        List<SentegrityTransparentAuthObject> transparentAuthObjectsToKeep = new ArrayList<>();

        for(SentegrityTransparentAuthObject storedTransparentAuthObject : currentTransparentAuthKeyObjects){
            daysSinceCreation = (SentegrityTrustFactorDatasets.getInstance().getRunTime() - storedTransparentAuthObject.getCreated()) / secondsInDay;

            if(daysSinceCreation < 1){
                daysSinceCreation = 1;
            }

            hitsPerDay = storedTransparentAuthObject.getHitCount() / daysSinceCreation;

            storedTransparentAuthObject.setDecayMetric(hitsPerDay);

            if(storedTransparentAuthObject.getDecayMetric() > policy.getTransparentAuthDecayMetric()){
                transparentAuthObjectsToKeep.add(storedTransparentAuthObject);
            }
        }

        return transparentAuthObjectsToKeep;
    }

    public boolean analyzeEligibleTransparentAuthObjects(SentegrityTrustScoreComputation computationResults, SentegrityPolicy policy){

        List<SentegrityTrustFactorOutput> transparentAuthObjectsToKeep = new ArrayList<>();
        List<SentegrityTrustFactorOutput> transparentAuthHighEntropyObjects = new ArrayList<>();

        int highEntropyCount = 0;

        boolean wifiAuthenticator = false;
        List<SentegrityTrustFactorOutput> wifiAuthenticationTrustFactorOutputObjects = new ArrayList<>();

        boolean locationAuthenticator = false;
        List<SentegrityTrustFactorOutput> locationAuthenticationTrustFactorOutputObjects = new ArrayList<>();

        boolean bluetoothAuthenticator = false;
        List<SentegrityTrustFactorOutput> bluetoothAuthenticationTrustFactorOutputObjects = new ArrayList<>();

        boolean gripAuthenticator = false;
        List<SentegrityTrustFactorOutput> gripAuthenticationTrustFactorOutputObjects = new ArrayList<>();

        boolean timeAuthenticator = false;
        List<SentegrityTrustFactorOutput> timeAuthenticationTrustFactorOutputObjects = new ArrayList<>();

        for(SentegrityTrustFactorOutput trustFactorOutput : computationResults.getTransparentAuthenticationTrustFactorOutputs()){
            switch (trustFactorOutput.getTrustFactor().getSubclassificationID()){
                case AttributingSubClass.WIFI:
                    wifiAuthenticator = true;
                    wifiAuthenticationTrustFactorOutputObjects.add(trustFactorOutput);
                    highEntropyCount++;
                    break;
                case AttributingSubClass.LOCATION:
                    locationAuthenticator = true;
                    locationAuthenticationTrustFactorOutputObjects.add(trustFactorOutput);
                    highEntropyCount++;
                    break;
                case AttributingSubClass.GRIP:
                    gripAuthenticator = true;
                    gripAuthenticationTrustFactorOutputObjects.add(trustFactorOutput);
                    highEntropyCount++;
                    break;
                case AttributingSubClass.BLUETOOTH:
                    bluetoothAuthenticator = true;
                    bluetoothAuthenticationTrustFactorOutputObjects.add(trustFactorOutput);
                    highEntropyCount++;
                    break;
                case AttributingSubClass.TIME:
                    timeAuthenticator = true;
                    timeAuthenticationTrustFactorOutputObjects.add(trustFactorOutput);
                    highEntropyCount++;
                    break;
                default:
                    transparentAuthObjectsToKeep.add(trustFactorOutput);
                    break;
            }
        }

        //keep this order!
        if(bluetoothAuthenticator){
            transparentAuthObjectsToKeep.addAll(bluetoothAuthenticationTrustFactorOutputObjects);
            computationResults.setTransparentAuthenticationTrustFactorOutputs(transparentAuthObjectsToKeep);
            return true;
        }else if(wifiAuthenticator){
            transparentAuthObjectsToKeep.addAll(wifiAuthenticationTrustFactorOutputObjects);
            computationResults.setTransparentAuthenticationTrustFactorOutputs(transparentAuthObjectsToKeep);
            return true;
        }else{
            transparentAuthHighEntropyObjects.addAll(locationAuthenticationTrustFactorOutputObjects);
            transparentAuthHighEntropyObjects.addAll(gripAuthenticationTrustFactorOutputObjects);
            transparentAuthHighEntropyObjects.addAll(timeAuthenticationTrustFactorOutputObjects);

            if(transparentAuthObjectsToKeep.size() < policy.getMinimumTransparentAuthEntropy()){
                return false;
            }else{
                List<SentegrityTrustFactorOutput> transparentAuthHighEntropyObjectsTrimmed = transparentAuthHighEntropyObjects.subList(0, policy.getMinimumTransparentAuthEntropy());
                transparentAuthObjectsToKeep.addAll(transparentAuthHighEntropyObjectsTrimmed);
                computationResults.setTransparentAuthenticationTrustFactorOutputs(transparentAuthObjectsToKeep);
                return true;
            }
        }
    }
}
