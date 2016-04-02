package com.sentegrity.core_detection.assertion_storage;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;

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

    public SentegrityAssertionStore() {
        trustFactors = new ArrayList<>();
    }

    public void setTrustFactors(List<SentegrityStoredTrustFactor> trustFactors) {
        this.trustFactors = trustFactors;
    }

    public boolean addAllToStore(List<SentegrityStoredTrustFactor> list) {
        if (list == null || list.size() < 1) {
            SentegrityError error = SentegrityError.NO_TRUST_FACTOR_OUTPUT_OBJECTS_RECEIVED;
            error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to receive stored trust factor objects").setFailureReason("No stored trust factor objects provided").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

            Logger.INFO("Failed to Receive Stored Trust Factor objects", error);
            return false;
        }

        for (SentegrityStoredTrustFactor storedTrustFactor : list) {
            if (!addToStore(storedTrustFactor)) {
                SentegrityError error = SentegrityError.UNABLE_TO_ADD_STORE_TRUST_FACTOR_OBJECTS_INTO_STORE;
                error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
                error.setDetails(new ErrorDetails().setDescription("Failed to add new stored trust factor object").setFailureReason("Unable to add new stored trust factor object").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

                Logger.INFO("Failed to Add a new Stored Trust Factor objects", error);
                return false;
            }
        }

        return true;
    }

    public boolean addToStore(SentegrityStoredTrustFactor storedTrustFactor) {

        if (storedTrustFactor == null) {
            SentegrityError error = SentegrityError.NO_TRUST_FACTOR_OUTPUT_OBJECTS_RECEIVED;
            error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to receive stored trust factor objects").setFailureReason("No stored trust factor objects provided").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

            Logger.INFO("Failed to Receive Stored Trust Factor objects", error);
            return false;
        }

        if (trustFactors == null)
            trustFactors = new ArrayList<>();

        trustFactors.add(storedTrustFactor);

        return true;
    }

    public boolean replaceInStore(SentegrityStoredTrustFactor storedTrustFactor) {
        if (storedTrustFactor == null) {
            SentegrityError error = SentegrityError.NO_TRUST_FACTOR_OUTPUT_OBJECTS_RECEIVED;
            error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to replace stored trust factor objects").setFailureReason("No stored trust factor objects provided during replacement").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

            Logger.INFO("Failed to Replace Stored Trust Factor objects", error);
            return false;
        }

        SentegrityStoredTrustFactor existing = getStoredTrustFactor(storedTrustFactor.getFactorID());

        if (existing != null) {
            if (!removeFromStore(existing)) {
                SentegrityError error = SentegrityError.UNABLE_TO_REMOVE_ASSERTION;
                error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
                error.setDetails(new ErrorDetails().setDescription("Failed to remove stored trust factor objects").setFailureReason("Unable to remove stored trust factor objects").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

                Logger.INFO("Failed to Remove Stored Trust Factor objects", error);
                return false;
            }
        }
        if (!addToStore(storedTrustFactor)) {
            SentegrityError error = SentegrityError.UNABLE_TO_ADD_STORE_TRUST_FACTOR_OBJECTS_INTO_STORE;
            error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to add new stored trust factor object").setFailureReason("Unable to add new stored trust factor object").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

            Logger.INFO("Failed to Add a new Stored Trust Factor objects", error);
            return false;
        }
        return true;
    }

    private boolean removeFromStore(SentegrityStoredTrustFactor existing) {
        if (existing == null) {
            SentegrityError error = SentegrityError.NO_TRUST_FACTOR_OUTPUT_OBJECTS_RECEIVED;
            error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to replace stored trust factor objects").setFailureReason("No stored trust factor objects provided during replacement").setRecoverySuggestion("Try providing a valid stored trust factor output object"));

            Logger.INFO("Failed to Replace Stored Trust Factor objects", error);
            return false;
        }

        if (getStoredTrustFactor(existing.getFactorID()) != null) {
            trustFactors.remove(existing);
        } else {
            SentegrityError error = SentegrityError.NO_MATCHING_ASSERTION_FOUND;
            error.setDomain(ErrorDomain.ASSERTION_STORE_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to match stored trust factor objects").setFailureReason("No matching trust factor objects found for removal").setRecoverySuggestion("Try providing a valid matching stored trust factor output object"));

            Logger.INFO("Failed to match Stored Trust Factor objects", error);
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