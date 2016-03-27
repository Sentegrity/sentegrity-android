package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 22/03/16.
 */
public class SentegrityAssertionStore {

    @SerializedName("appid")
    private String appId;

    @SerializedName("storedTrustFactorObjects")
    private List<SentegrityStoredTrustFactor> trustFactors;

    public List<SentegrityStoredTrustFactor> getTrustFactors() {
        return trustFactors;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public SentegrityAssertionStore(){
        trustFactors = new ArrayList<>();
    }

    public void setTrustFactors(List<SentegrityStoredTrustFactor> trustFactors) {
        this.trustFactors = trustFactors;
    }

    public boolean addAllToStore(List<SentegrityStoredTrustFactor> list) {
        if (list == null || list.size() < 1) {
            return false;
        }

        for (SentegrityStoredTrustFactor storedTrustFactor : list) {
            if (!addToStore(storedTrustFactor)) {
                return false;
            }
        }

        return true;
    }

    public boolean addToStore(SentegrityStoredTrustFactor storedTrustFactor) {

        if (storedTrustFactor == null)
            return false;

        if (trustFactors == null)
            trustFactors = new ArrayList<>();

        trustFactors.add(storedTrustFactor);

        return true;
    }

    public boolean replaceInStore(SentegrityStoredTrustFactor storedTrustFactor) {
        if (storedTrustFactor == null) {
            return false;
        }

        SentegrityStoredTrustFactor existing = getStoredTrustFactor(storedTrustFactor.getFactorID());

        if (existing != null) {
            if (!removeFromStore(existing)) {
                return false;
            }
        }
        if(!addToStore(storedTrustFactor)){
            return false;
        }
        return true;
    }

    private boolean removeFromStore(SentegrityStoredTrustFactor existing) {
        if (existing == null) {
            return false;
        }

        if(getStoredTrustFactor(existing.getFactorID()) != null){
            trustFactors.remove(existing);

            //TODO: different object? removeFromStore by id?
        }else{
            return false;
        }
        return true;
    }


    public SentegrityStoredTrustFactor createStoredTrustFactor(SentegrityTrustFactorOutput output) {
        if (output == null)
            return null;

        SentegrityStoredTrustFactor storedTrustFactor = new SentegrityStoredTrustFactor();
        storedTrustFactor.setFactorID(output.getTrustFactor().getID());
        storedTrustFactor.setRevision(output.getTrustFactor().getRevision());
        storedTrustFactor.setDecayMetric(output.getTrustFactor().getDecayMetric());
        storedTrustFactor.setLearned(false);
        storedTrustFactor.setFirstRun(System.currentTimeMillis());
        storedTrustFactor.setRunCount(0);

        return storedTrustFactor;
    }

    public SentegrityStoredTrustFactor getStoredTrustFactor(int id) {
        if (id < 1)
            return null;

        if (getTrustFactors() == null || getTrustFactors().size() < 1) {
            return null;
        }

        for (SentegrityStoredTrustFactor trustFactor : getTrustFactors()) {
            if (trustFactor.getFactorID() == id) {
                return trustFactor;
            }
        }

        return null;
    }
}