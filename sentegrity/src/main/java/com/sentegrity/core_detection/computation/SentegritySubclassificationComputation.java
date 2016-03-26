package com.sentegrity.core_detection.computation;

import com.sentegrity.core_detection.policy.SentegrityTrustFactor;

import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegritySubclassificationComputation {

    private List<SentegrityTrustFactor> trustFactors;

    private int totalWeight;

    private int baseWeight;

    public List getTrustFactors() {
        return trustFactors;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getBaseWeight() {
        return baseWeight;
    }

    public void setTrustFactors(List<SentegrityTrustFactor> trustFactors) {
        this.trustFactors = trustFactors;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public void setBaseWeight(int baseWeight) {
        this.baseWeight = baseWeight;
    }
}
