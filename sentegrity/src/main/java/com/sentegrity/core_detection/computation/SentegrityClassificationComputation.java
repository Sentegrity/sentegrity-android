package com.sentegrity.core_detection.computation;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.policy.SentegritySubclassification;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityClassificationComputation {

    private int score;

    private List<SentegritySubclassification> subclassifications;

    private List<SentegrityTrustFactor> trustFactors;

    private List<SentegrityTrustFactorOutput> trustFactorsToWhitelist;

    private List<SentegrityTrustFactorOutput> trustFactorsForTransparentAuthentication;

    private List<SentegrityTrustFactorOutput> trustFactorsTriggered;

    private List<SentegrityTrustFactorOutput> trustFactorsNotLearned;

    private List<SentegrityTrustFactorOutput> trustFacotrsWithErrors;

    private List<String> issues;

    private List<String> suggestions;

    private List<String> status;

    private List<String> authenticators;

    public int getScore() {
        return score;
    }

    public List<SentegritySubclassification> getSubclassifications() {
        return subclassifications;
    }

    public List<SentegrityTrustFactor> getTrustFactors() {
        return trustFactors;
    }

    public List<SentegrityTrustFactorOutput> getTrustFactorsToWhitelist() {
        return trustFactorsToWhitelist;
    }

    public List<SentegrityTrustFactorOutput> getTrustFactorsForTransparentAuthentication() {
        return trustFactorsForTransparentAuthentication;
    }

    public List<SentegrityTrustFactorOutput> getTrustFactorsTriggered() {
        return trustFactorsTriggered;
    }

    public List<SentegrityTrustFactorOutput> getTrustFactorsNotLearned() {
        return trustFactorsNotLearned;
    }

    public List<SentegrityTrustFactorOutput> getTrustFacotrsWithErrors() {
        return trustFacotrsWithErrors;
    }

    public List<String> getIssues() {
        return issues;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public List<String> getStatus() {
        return status;
    }

    public List<String> getAuthenticators() {
        return authenticators != null ? authenticators : new ArrayList<String>();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setSubclassifications(List<SentegritySubclassification> subclassifications) {
        this.subclassifications = subclassifications;
    }

    public void setTrustFactors(List<SentegrityTrustFactor> trustFactors) {
        this.trustFactors = trustFactors;
    }

    public void setTrustFactorsToWhitelist(List<SentegrityTrustFactorOutput> trustFactorsToWhitelist) {
        this.trustFactorsToWhitelist = trustFactorsToWhitelist;
    }

    public void setTrustFactorsForTransparentAuthentication(List<SentegrityTrustFactorOutput> trustFactorsForTransparentAuthentication) {
        this.trustFactorsForTransparentAuthentication = trustFactorsForTransparentAuthentication;
    }

    public void setTrustFactorsTriggered(List<SentegrityTrustFactorOutput> trustFactorsTriggered) {
        this.trustFactorsTriggered = trustFactorsTriggered;
    }

    public void setTrustFactorsNotLearned(List<SentegrityTrustFactorOutput> trustFactorsNotLearned) {
        this.trustFactorsNotLearned = trustFactorsNotLearned;
    }

    public void setTrustFacotrsWithErrors(List<SentegrityTrustFactorOutput> trustFacotrsWithErrors) {
        this.trustFacotrsWithErrors = trustFacotrsWithErrors;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public void setAuthenticators(List<String> authenticators) {
        this.authenticators = authenticators;
    }
}
