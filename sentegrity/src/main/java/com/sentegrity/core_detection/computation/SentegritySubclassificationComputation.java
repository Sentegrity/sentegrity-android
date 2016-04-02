package com.sentegrity.core_detection.computation;

import com.sentegrity.core_detection.policy.SentegrityTrustFactor;

import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegritySubclassificationComputation {

    private List<SentegrityTrustFactor> trustFactors;

    private int totalWeight;

    public List getTrustFactors() {
        return trustFactors;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public void setTrustFactors(List<SentegrityTrustFactor> trustFactors) {
        this.trustFactors = trustFactors;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

}
