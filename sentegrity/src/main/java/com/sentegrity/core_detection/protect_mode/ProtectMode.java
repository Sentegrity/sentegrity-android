package com.sentegrity.core_detection.protect_mode;

import com.sentegrity.core_detection.assertion_storage.SentegrityAssertionStore;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredTrustFactor;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.policy.SentegrityPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 27/03/16.
 */
public class ProtectMode {

    final private SentegrityPolicy policy;
    final private List<SentegrityTrustFactorOutput> trustFactorsToWhiteList;

    public ProtectMode(SentegrityPolicy policy, List<SentegrityTrustFactorOutput> trustFactorsToWhiteList) {
        this.policy = policy;
        this.trustFactorsToWhiteList = trustFactorsToWhiteList;
    }

    public boolean deactivateProtectMode(int action, String input){
        switch (action){
            case 1:
            case 2:
            case 3:
                if("user".equals(input.toLowerCase())){
                    if(trustFactorsToWhiteList != null && trustFactorsToWhiteList.size() > 0){
                        if(!whitelistAttributingTrustFactorOutput()){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            case 4:
                return false;
            case 5:
                if("admin".equals(input.toLowerCase())){
                    if(trustFactorsToWhiteList != null && trustFactorsToWhiteList.size() > 0){
                        if(!whitelistAttributingTrustFactorOutput()){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
        }

        return true;
    }

    private boolean whitelistAttributingTrustFactorOutput(){
        SentegrityAssertionStore localStore = SentegrityTrustFactorStore.getInstance().getAssertionStore(policy.getAppID());
        if(localStore == null){
            return false;
        }

        List<SentegrityStoredAssertion> existingStoredAssertionObjects = new ArrayList();
        List<SentegrityStoredAssertion> mergedStoredAssertionObjects = new ArrayList();

        for(SentegrityTrustFactorOutput trustFactorOutput : trustFactorsToWhiteList){
            if(trustFactorOutput.getStoredTrustFactor().getAssertions() == null || trustFactorOutput.getStoredTrustFactor().getAssertions().size() < 1){
                trustFactorOutput.getStoredTrustFactor().setAssertions(trustFactorOutput.getCandidateAssertionObjects());
            }else{
                existingStoredAssertionObjects = trustFactorOutput.getStoredTrustFactor().getAssertions();
                mergedStoredAssertionObjects = existingStoredAssertionObjects;
                mergedStoredAssertionObjects.addAll(trustFactorOutput.getCandidateAssertionObjectsForWhitelisting());
                trustFactorOutput.getStoredTrustFactor().setAssertions(mergedStoredAssertionObjects);
            }

            SentegrityStoredTrustFactor storedTrustFactor = localStore.getStoredTrustFactor(trustFactorOutput.getTrustFactor().getID());

            if(storedTrustFactor == null){
                continue;
            }
            if(!localStore.replaceInStore(trustFactorOutput.getStoredTrustFactor())){
                continue;
            }
        }

        SentegrityAssertionStore localStoreOutput = SentegrityTrustFactorStore.getInstance().setAssertionStore(localStore, policy.getAppID());

        if(localStoreOutput == null){
            return false;
        }

        return true;
    }
}
