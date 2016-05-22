package com.sentegrity.core_detection.computation;

import android.text.TextUtils;
import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.policy.SentegrityClassification;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.policy.SentegritySubclassification;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;
import com.sentegrity.core_detection.startup.SentegrityTransparentAuthObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityTrustScoreComputation {

    private SentegrityPolicy policy;

    private List<SentegrityTrustFactorOutput> userTrustFactorsNotLearned;
    private List<SentegrityTrustFactorOutput> systemTrustFactorsNotLearned;

    private List<SentegrityTrustFactorOutput> userTrustFactorsAttributingToScore;
    private List<SentegrityTrustFactorOutput> systemTrustFactorsAttributingToScore;

    private List<SentegrityTrustFactorOutput> userTrustFactorsWithErrors;
    private List<SentegrityTrustFactorOutput> systemTrustFactorsWithErrors;

    private List<SentegrityTrustFactor> userAllTrustFactors;
    private List<SentegrityTrustFactor> systemAllTrustFactors;

    private int systemScore;
    private boolean systemTrusted;
    private int systemGUIIconID;
    private String systemGUIIconText;
    private List<String> systemIssues;
    private List<String> systemSuggestions;
    private List<String> systemAnalysisResults;

    private int userScore;
    private boolean userTrusted;
    private int userGUIIconID;
    private String userGUIIconText;
    private List<String> userIssues;
    private List<String> userSuggestions;
    private List<String> userDynamicTwoFactors;
    private List<String> userAnalysisResults;

    private int deviceScore;
    private boolean deviceTrusted;

    private List<SentegrityTrustFactorOutput> protectModeWhitelist;
    private List<SentegrityTrustFactorOutput> protectModeUserWhitelist;
    private List<SentegrityTrustFactorOutput> protectModeSystemWhitelist;
    private List<SentegrityTrustFactorOutput> transparentAuthenticationTrustFactors;

    private SentegrityClassification systemBreachClass;
    private SentegrityClassification systemPolicyClass;
    private SentegrityClassification systemSecurityClass;
    private SentegrityClassification userAnomalyClass;
    private SentegrityClassification userPolicyClass;

    private int systemBreachScore;
    private int systemPolicyScore;
    private int systemSecurityScore;
    private int userAnomalyScore;
    private int userPolicyScore;

    private int attributingClassID;
    private int preAuthenticationAction;
    private int postAuthenticationAction;
    private int coreDetectionResult;
    private int authenticationResult;
    private byte[] decryptedMasterKey;

    private List<SentegrityTrustFactorOutput> transparentAuthenticationTrustFactorOutputs;
    private int transparentAuthenticationAction;
    private int entropyCount;
    private boolean shouldAttemptTransparentAuthentication;
    private boolean foundTransparentMatch;
    private SentegrityTransparentAuthObject matchingTransparentAuthenticationObject;
    private byte[] candidateTransparentKey;
    private String candidateTransparentKeyHashString;

    private List<SentegrityTrustFactorOutput> userTrustFactorWhitelist;
    private List<SentegrityTrustFactorOutput> systemTrustFactorWhitelist;

    public SentegrityPolicy getPolicy() {
        return policy;
    }

    public void setSystemBreachClass(SentegrityClassification systemBreachClass) {
        this.systemBreachClass = systemBreachClass;
    }

    public void setSystemPolicyClass(SentegrityClassification systemPolicyClass) {
        this.systemPolicyClass = systemPolicyClass;
    }

    public void setSystemSecurityClass(SentegrityClassification systemSecurityClass) {
        this.systemSecurityClass = systemSecurityClass;
    }

    public void setUserAnomalyClass(SentegrityClassification userAnomalyClass) {
        this.userAnomalyClass = userAnomalyClass;
    }

    public void setUserPolicyClass(SentegrityClassification userPolicyClass) {
        this.userPolicyClass = userPolicyClass;
    }

    public SentegrityClassification getUserPolicyClass() {
        return userPolicyClass;
    }

    public SentegrityClassification getUserAnomalyClass() {
        return userAnomalyClass;
    }

    public SentegrityClassification getSystemSecurityClass() {
        return systemSecurityClass;
    }

    public SentegrityClassification getSystemPolicyClass() {
        return systemPolicyClass;
    }

    public SentegrityClassification getSystemBreachClass() {
        return systemBreachClass;
    }

    public List<SentegrityTrustFactorOutput> getUserTrustFactorsNotLearned() {
        return userTrustFactorsNotLearned;
    }

    public List<SentegrityTrustFactorOutput> getSystemTrustFactorsNotLearned() {
        return systemTrustFactorsNotLearned;
    }

    public List<SentegrityTrustFactorOutput> getUserTrustFactorsAttributingToScore() {
        return userTrustFactorsAttributingToScore;
    }

    public List<SentegrityTrustFactorOutput> getSystemTrustFactorsAttributingToScore() {
        return systemTrustFactorsAttributingToScore;
    }

    public List<SentegrityTrustFactorOutput> getUserTrustFactorsWithErrors() {
        return userTrustFactorsWithErrors;
    }

    public List<SentegrityTrustFactorOutput> getSystemTrustFactorsWithErrors() {
        return systemTrustFactorsWithErrors;
    }

    public List<SentegrityTrustFactor> getUserAllTrustFactors() {
        return userAllTrustFactors;
    }

    public List<SentegrityTrustFactor> getSystemAllTrustFactors() {
        return systemAllTrustFactors;
    }

    public int getSystemBreachScore() {
        return systemBreachScore;
    }

    public int getSystemPolicyScore() {
        return systemPolicyScore;
    }

    public int getSystemSecurityScore() {
        return systemSecurityScore;
    }

    public int getUserPolicyScore() {
        return userPolicyScore;
    }

    public int getUserAnomalyScore() {
        return userAnomalyScore;
    }

    public int getSystemScore() {
        return systemScore;
    }

    public boolean isSystemTrusted() {
        return systemTrusted;
    }

    public int getSystemGUIIconID() {
        return systemGUIIconID;
    }

    public String getSystemGUIIconText() {
        return systemGUIIconText;
    }

    public List<String> getSystemIssues() {
        return systemIssues;
    }

    public List<String> getSystemSuggestions() {
        return systemSuggestions;
    }

    public List<String> getSystemAnalysisResults() {
        return systemAnalysisResults;
    }

    public int getUserScore() {
        return userScore;
    }

    public boolean isUserTrusted() {
        return userTrusted;
    }

    public int getUserGUIIconID() {
        return userGUIIconID;
    }

    public String getUserGUIIconText() {
        return userGUIIconText;
    }

    public List<String> getUserIssues() {
        return userIssues;
    }

    public List<String> getUserSuggestions() {
        return userSuggestions;
    }

    public List<String> getUserDynamicTwoFactors() {
        return userDynamicTwoFactors;
    }

    public List<String> getUserAnalysisResults() {
        return userAnalysisResults;
    }

    public int getDeviceScore() {
        return deviceScore;
    }

    public boolean isDeviceTrusted() {
        return deviceTrusted;
    }

    public List<SentegrityTrustFactorOutput> getProtectModeWhitelist() {
        return protectModeWhitelist != null ? protectModeWhitelist : (protectModeWhitelist = new ArrayList<>());
    }

    public List<SentegrityTrustFactorOutput> getProtectModeUserWhitelist() {
        return protectModeUserWhitelist;
    }

    public List<SentegrityTrustFactorOutput> getProtectModeSystemWhitelist() {
        return protectModeSystemWhitelist;
    }

    public List<SentegrityTrustFactorOutput> getTransparentAuthenticationTrustFactors() {
        return transparentAuthenticationTrustFactors;
    }

    public void setPolicy(SentegrityPolicy policy) {
        this.policy = policy;
    }

    public void setUserTrustFactorsNotLearned(List<SentegrityTrustFactorOutput> userTrustFactorsNotLearned) {
        this.userTrustFactorsNotLearned = userTrustFactorsNotLearned;
    }

    public void setSystemTrustFactorsNotLearned(List<SentegrityTrustFactorOutput> systemTrustFactorsNotLearned) {
        this.systemTrustFactorsNotLearned = systemTrustFactorsNotLearned;
    }

    public void setUserTrustFactorsAttributingToScore(List<SentegrityTrustFactorOutput> userTrustFactorsAttributingToScore) {
        this.userTrustFactorsAttributingToScore = userTrustFactorsAttributingToScore;
    }

    public void setSystemTrustFactorsAttributingToScore(List<SentegrityTrustFactorOutput> systemTrustFactorsAttributingToScore) {
        this.systemTrustFactorsAttributingToScore = systemTrustFactorsAttributingToScore;
    }

    public void setUserTrustFactorsWithErrors(List<SentegrityTrustFactorOutput> userTrustFactorsWithErrors) {
        this.userTrustFactorsWithErrors = userTrustFactorsWithErrors;
    }

    public void setSystemTrustFactorsWithErrors(List<SentegrityTrustFactorOutput> systemTrustFactorsWithErrors) {
        this.systemTrustFactorsWithErrors = systemTrustFactorsWithErrors;
    }

    public void setUserAllTrustFactors(List<SentegrityTrustFactor> userAllTrustFactors) {
        this.userAllTrustFactors = userAllTrustFactors;
    }

    public void setSystemAllTrustFactors(List<SentegrityTrustFactor> systemAllTrustFactors) {
        this.systemAllTrustFactors = systemAllTrustFactors;
    }

    public void setSystemBreachScore(int systemBreachScore) {
        this.systemBreachScore = systemBreachScore;
    }

    public void setSystemPolicyScore(int systemPolicyScore) {
        this.systemPolicyScore = systemPolicyScore;
    }

    public void setSystemSecurityScore(int systemSecurityScore) {
        this.systemSecurityScore = systemSecurityScore;
    }

    public void setUserPolicyScore(int userPolicyScore) {
        this.userPolicyScore = userPolicyScore;
    }

    public void setUserAnomalyScore(int userAnomalyScore) {
        this.userAnomalyScore = userAnomalyScore;
    }

    public void setSystemScore(int systemScore) {
        this.systemScore = systemScore;
    }

    public void setSystemTrusted(boolean systemTrusted) {
        this.systemTrusted = systemTrusted;
    }

    public void setSystemGUIIconID(int systemGUIIconID) {
        this.systemGUIIconID = systemGUIIconID;
    }

    public void setSystemGUIIconText(String systemGUIIconText) {
        this.systemGUIIconText = systemGUIIconText;
    }

    public void setSystemIssues(List<String> systemIssues) {
        this.systemIssues = systemIssues;
    }

    public void setSystemSuggestions(List<String> systemSuggestions) {
        this.systemSuggestions = systemSuggestions;
    }

    public void setSystemAnalysisResults(List<String> systemAnalysisResults) {
        this.systemAnalysisResults = systemAnalysisResults;
    }

    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }

    public void setUserTrusted(boolean userTrusted) {
        this.userTrusted = userTrusted;
    }

    public void setUserGUIIconID(int userGUIIconID) {
        this.userGUIIconID = userGUIIconID;
    }

    public void setUserGUIIconText(String userGUIIconText) {
        this.userGUIIconText = userGUIIconText;
    }

    public void setUserIssues(List<String> userIssues) {
        this.userIssues = userIssues;
    }

    public void setUserSuggestions(List<String> userSuggestions) {
        this.userSuggestions = userSuggestions;
    }

    public void setUserDynamicTwoFactors(List<String> userDynamicTwoFactors) {
        this.userDynamicTwoFactors = userDynamicTwoFactors;
    }

    public void setUserAnalysisResults(List<String> userAnalysisResults) {
        this.userAnalysisResults = userAnalysisResults;
    }

    public void setDeviceScore(int deviceScore) {
        this.deviceScore = deviceScore;
    }

    public void setDeviceTrusted(boolean deviceTrusted) {
        this.deviceTrusted = deviceTrusted;
    }

    public void setProtectModeWhitelist(List<SentegrityTrustFactorOutput> protectModeWhitelist) {
        this.protectModeWhitelist = protectModeWhitelist;
    }

    public void setProtectModeUserWhitelist(List<SentegrityTrustFactorOutput> protectModeUserWhitelist) {
        this.protectModeUserWhitelist = protectModeUserWhitelist;
    }

    public void setProtectModeSystemWhitelist(List<SentegrityTrustFactorOutput> protectModeSystemWhitelist) {
        this.protectModeSystemWhitelist = protectModeSystemWhitelist;
    }

    public void setTransparentAuthenticationTrustFactors(List<SentegrityTrustFactorOutput> transparentAuthenticationTrustFactors) {
        this.transparentAuthenticationTrustFactors = transparentAuthenticationTrustFactors;
    }

    public int getAttributingClassID() {
        return attributingClassID;
    }

    public void setAttributingClassID(int attributingClassID) {
        this.attributingClassID = attributingClassID;
    }

    public int getPreAuthenticationAction() {
        return preAuthenticationAction;
    }

    public void setPreAuthenticationAction(int preAuthenticationAction) {
        this.preAuthenticationAction = preAuthenticationAction;
    }

    public int getPostAuthenticationAction() {
        return postAuthenticationAction;
    }

    public void setPostAuthenticationAction(int postAuthenticationAction) {
        this.postAuthenticationAction = postAuthenticationAction;
    }

    public int getCoreDetectionResult() {
        return coreDetectionResult;
    }

    public void setCoreDetectionResult(int coreDetectionResult) {
        this.coreDetectionResult = coreDetectionResult;
    }

    public int getAuthenticationResult() {
        return authenticationResult;
    }

    public void setAuthenticationResult(int authenticationResult) {
        this.authenticationResult = authenticationResult;
    }

    public byte[] getDecryptedMasterKey() {
        return decryptedMasterKey;
    }

    public void setDecryptedMasterKey(byte[] decryptedMasterKey) {
        this.decryptedMasterKey = decryptedMasterKey;
    }

    public List<SentegrityTrustFactorOutput> getTransparentAuthenticationTrustFactorOutputs() {
        return transparentAuthenticationTrustFactorOutputs;
    }

    public void setTransparentAuthenticationTrustFactorOutputs(List<SentegrityTrustFactorOutput> transparentAuthenticationTrustFactorOutputs) {
        this.transparentAuthenticationTrustFactorOutputs = transparentAuthenticationTrustFactorOutputs;
    }

    public int getTransparentAuthenticationAction() {
        return transparentAuthenticationAction;
    }

    public void setTransparentAuthenticationAction(int transparentAuthenticationAction) {
        this.transparentAuthenticationAction = transparentAuthenticationAction;
    }

    public int getEntropyCount() {
        return entropyCount;
    }

    public void setEntropyCount(int entropyCount) {
        this.entropyCount = entropyCount;
    }

    public boolean isShouldAttemptTransparentAuthentication() {
        return shouldAttemptTransparentAuthentication;
    }

    public void setShouldAttemptTransparentAuthentication(boolean shouldAttemptTransparentAuthentication) {
        this.shouldAttemptTransparentAuthentication = shouldAttemptTransparentAuthentication;
    }

    public boolean isFoundTransparentMatch() {
        return foundTransparentMatch;
    }

    public void setFoundTransparentMatch(boolean foundTransparentMatch) {
        this.foundTransparentMatch = foundTransparentMatch;
    }

    public SentegrityTransparentAuthObject getMatchingTransparentAuthenticationObject() {
        return matchingTransparentAuthenticationObject;
    }

    public void setMatchingTransparentAuthenticationObject(SentegrityTransparentAuthObject matchingTransparentAuthenticationObject) {
        this.matchingTransparentAuthenticationObject = matchingTransparentAuthenticationObject;
    }

    public byte[] getCandidateTransparentKey() {
        return candidateTransparentKey;
    }

    public void setCandidateTransparentKey(byte[] candidateTransparentKey) {
        this.candidateTransparentKey = candidateTransparentKey;
    }

    public String getCandidateTransparentKeyHashString() {
        return candidateTransparentKeyHashString;
    }

    public void setCandidateTransparentKeyHashString(String candidateTransparentKeyHashString) {
        this.candidateTransparentKeyHashString = candidateTransparentKeyHashString;
    }

    public List<SentegrityTrustFactorOutput> getUserTrustFactorWhitelist() {
        return userTrustFactorWhitelist;
    }

    public void setUserTrustFactorWhitelist(List<SentegrityTrustFactorOutput> userTrustFactorWhitelist) {
        this.userTrustFactorWhitelist = userTrustFactorWhitelist;
    }

    public List<SentegrityTrustFactorOutput> getSystemTrustFactorWhitelist() {
        return systemTrustFactorWhitelist;
    }

    public void setSystemTrustFactorWhitelist(List<SentegrityTrustFactorOutput> systemTrustFactorWhitelist) {
        this.systemTrustFactorWhitelist = systemTrustFactorWhitelist;
    }

    private static double weightPercentCalculate(SentegrityTrustFactorOutput trustFactorOutput) {
        SentegrityStoredAssertion highestStoredAssertion = trustFactorOutput.getStoredTrustFactor().getAssertions().get(0);

        double currentAssertionDecayMetricTotal = 0;
        double currentAssertionDecayMetricAverage = 0;
        double highestStoredAssertionDecayMetric = highestStoredAssertion.getDecayMetric();
        double trustFactorPolicyDecayMetric = trustFactorOutput.getTrustFactor().getDecayMetric();

        for (SentegrityStoredAssertion assertion : trustFactorOutput.getStoredAssertionObjectsMatched()) {
            currentAssertionDecayMetricTotal = currentAssertionDecayMetricTotal + assertion.getDecayMetric();
        }

        currentAssertionDecayMetricAverage = currentAssertionDecayMetricTotal / trustFactorOutput.getStoredAssertionObjectsMatched().size();

        double percent = Math.abs(1 - ((highestStoredAssertionDecayMetric - currentAssertionDecayMetricAverage) / (highestStoredAssertionDecayMetric - trustFactorPolicyDecayMetric)));

        return percent;
    }

    public static SentegrityTrustScoreComputation performTrustFactorComputation(SentegrityPolicy policy, List<SentegrityTrustFactorOutput> trustFactorOutputs) {
        if (policy == null || Integer.valueOf(policy.getPolicyID()) < 0) {
            SentegrityError error = SentegrityError.CORE_DETECTION_NO_POLICY_PROVIDED;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("No policy provided").setFailureReason("Unable to set trust factors").setRecoverySuggestion("Try passing a valid policy to set trust factors"));

            Logger.INFO("No policy provided", error);
            return null;
        }

        if (trustFactorOutputs == null || trustFactorOutputs.size() < 1) {
            SentegrityError error = SentegrityError.NO_TRUSTFACTORS_SET_TO_ANALYZE;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("No trust factors found to compute").setFailureReason("Unable to set aassertion objects").setRecoverySuggestion("Try providing trust factor outputs"));

            Logger.INFO("No TrustFactorOutputObjects found to comput", error);
            return null;
        }

        if (policy.getClassifications() == null || policy.getClassifications().size() < 1) {
            SentegrityError error = SentegrityError.NO_CLASSIFICATIONS_FOUND;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("No Classifications Found.").setFailureReason("Unable to find classifications in policy").setRecoverySuggestion("Try checking if policy has valid classifications"));

            Logger.INFO("No classifications found", error);
            return null;
        }

        if (policy.getSubclassifications() == null || policy.getSubclassifications().size() < 1) {
            SentegrityError error = SentegrityError.NO_SUBCLASSIFICATIONS_FOUND;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("No subclassifications Found.").setFailureReason("Unable to find subclassifications in policy").setRecoverySuggestion("Try checking if policy has valid subclassifications"));

            Logger.INFO("No subclassifications found", error);
            return null;
        }

        long start = System.nanoTime();
        for (SentegrityClassification classification : policy.getClassifications()) {


            List<SentegrityTrustFactorOutput> trustFactorsNotLearnedInClass = new ArrayList();
            List<SentegrityTrustFactorOutput> trustFactorsAttributingToScoreInClass = new ArrayList();
            List<SentegrityTrustFactorOutput> trustFactorWithErrorsInClass = new ArrayList();


            List<SentegrityTrustFactor> trustFactorsInClass = new ArrayList();
            List<SentegritySubclassification> subClassesInClass = new ArrayList();
            List<SentegrityTrustFactorOutput> trustFactorsToWhitelistInClass = new ArrayList();
            List<SentegrityTrustFactorOutput> trustFactorsForTransparentAuthInClass = new ArrayList();


            List<String> statusInClass = new ArrayList();
            List<String> issuesInClass = new ArrayList();
            List<String> suggestionsInClass = new ArrayList();
            List<String> dynamicTwoFactorsInClass = new ArrayList();


            for (SentegritySubclassification subclassification : policy.getSubclassifications()) {

                List<SentegrityTrustFactor> trustFactorsInSubClass = new ArrayList();

                List<Integer> subClassDNECodes = new ArrayList();

                subclassification.setScore(0);

                boolean subClassContainsTrustFactor = false;
                boolean subClassAnalysisIsIncomplete = false;

                for (SentegrityTrustFactorOutput trustFactorOutput : trustFactorOutputs) {

                    if (trustFactorOutput.getTrustFactor().getClassificationID() == classification.getID() && trustFactorOutput.getTrustFactor().getSubclassificationID() == subclassification.getID()) {

                        subClassContainsTrustFactor = true;

                        if (trustFactorOutput.isForComputation()) {

                            if (trustFactorOutput.getStatusCode() == DNEStatusCode.OK) {

                                if (!trustFactorOutput.getStoredTrustFactor().isLearned()) {
                                    trustFactorsToWhitelistInClass.add(trustFactorOutput);
                                    trustFactorsNotLearnedInClass.add(trustFactorOutput);
                                    continue;
                                }

                                if (trustFactorOutput.isMatchFound()) {
                                    //User anomaly
                                    if (classification.getComputationMethod() == 1) {
                                        //trustFactorsForTransparentAuthInClass.add(trustFactorOutput);
                                        trustFactorsAttributingToScoreInClass.add(trustFactorOutput);

                                        if (trustFactorOutput.getTrustFactor().getPartialWeight() == 1) {
                                            double percent = weightPercentCalculate(trustFactorOutput);

                                            int partialWeight = (int) (percent * trustFactorOutput.getTrustFactor().getWeight());
                                            subclassification.setScore(subclassification.getScore() + partialWeight);

                                            trustFactorOutput.setAppliedWeight(partialWeight);
                                            trustFactorOutput.setPercentAppliedWeight(percent);

                                            if (partialWeight < (trustFactorOutput.getTrustFactor().getWeight() * 0.25)) {

                                                if (!TextUtils.isEmpty(trustFactorOutput.getTrustFactor().getLowConfidenceIssueMessage())) {
                                                    if (!issuesInClass.contains(trustFactorOutput.getTrustFactor().getLowConfidenceIssueMessage())) {
                                                        issuesInClass.add(trustFactorOutput.getTrustFactor().getLowConfidenceIssueMessage());
                                                    }
                                                }

                                                if (!TextUtils.isEmpty(trustFactorOutput.getTrustFactor().getLowConfidenceSuggestionMessage())) {
                                                    if (!suggestionsInClass.contains(trustFactorOutput.getTrustFactor().getLowConfidenceSuggestionMessage())) {
                                                        suggestionsInClass.add(trustFactorOutput.getTrustFactor().getLowConfidenceSuggestionMessage());
                                                    }
                                                }

                                            }

                                            if (trustFactorOutput.getTrustFactor().getTransparentEligible() == 1) {
                                                if (partialWeight >= trustFactorOutput.getTrustFactor().getWeight() * 0.25) {
                                                    trustFactorsForTransparentAuthInClass.add(trustFactorOutput);

                                                    if (trustFactorOutput.getTrustFactor().getSubclassificationID() == 2
                                                            || trustFactorOutput.getTrustFactor().getSubclassificationID() == 8) {
                                                        String name = trustFactorOutput.getTrustFactor().getDispatch() + " authentication";

                                                        if (!dynamicTwoFactorsInClass.contains(name)) {
                                                            dynamicTwoFactorsInClass.add(name);
                                                        }
                                                    }

                                                }
                                            }


                                        } else {

                                            if (trustFactorOutput.getTrustFactor().getTransparentEligible() == 1) {
                                                trustFactorsForTransparentAuthInClass.add(trustFactorOutput);

                                                if (trustFactorOutput.getTrustFactor().getSubclassificationID() == 2
                                                        || trustFactorOutput.getTrustFactor().getSubclassificationID() == 8) {
                                                    String name = trustFactorOutput.getTrustFactor().getDispatch() + " authentication";

                                                    if (!dynamicTwoFactorsInClass.contains(name)) {
                                                        dynamicTwoFactorsInClass.add(name);
                                                    }
                                                }
                                            }


                                            subclassification.setScore(subclassification.getScore() + trustFactorOutput.getTrustFactor().getWeight());

                                            trustFactorOutput.setAppliedWeight(trustFactorOutput.getTrustFactor().getWeight());
                                            trustFactorOutput.setPercentAppliedWeight(1);
                                        }
                                    } else {


                                    }

                                } else {

                                    if(trustFactorOutput.getTrustFactor().isWhitelistable()) {
                                        trustFactorsToWhitelistInClass.add(trustFactorOutput);
                                    }

                                    // System classification and user policy
                                    if (classification.getComputationMethod() == 0) {

                                        trustFactorsAttributingToScoreInClass.add(trustFactorOutput);

                                        if (trustFactorOutput.getTrustFactor().getPartialWeight() == 1) {

                                            double percent = weightPercentCalculate(trustFactorOutput);
                                            int partialWeight = (int) (percent * trustFactorOutput.getTrustFactor().getWeight());
                                            subclassification.setScore(subclassification.getScore() + partialWeight);

                                            trustFactorOutput.setAppliedWeight(partialWeight);
                                            trustFactorOutput.setPercentAppliedWeight(percent);


                                        } else {
                                            subclassification.setScore(subclassification.getScore() + trustFactorOutput.getTrustFactor().getWeight());

                                            trustFactorOutput.setAppliedWeight(trustFactorOutput.getTrustFactor().getWeight());
                                            trustFactorOutput.setPercentAppliedWeight(1);

                                        }
                                    } else {


                                    }


                                    if (!TextUtils.isEmpty(trustFactorOutput.getTrustFactor().getNotFoundIssueMessage())) {
                                        if (!issuesInClass.contains(trustFactorOutput.getTrustFactor().getNotFoundIssueMessage())) {
                                            issuesInClass.add(trustFactorOutput.getTrustFactor().getNotFoundIssueMessage());
                                        }
                                    }

                                    if (!TextUtils.isEmpty(trustFactorOutput.getTrustFactor().getNotFoundSuggestionMessage())) {
                                        if (!suggestionsInClass.contains(trustFactorOutput.getTrustFactor().getNotFoundSuggestionMessage())) {
                                            suggestionsInClass.add(trustFactorOutput.getTrustFactor().getNotFoundSuggestionMessage());
                                        }
                                    }

                                }
                            } else {

                                trustFactorWithErrorsInClass.add(trustFactorOutput);
                                subClassDNECodes.add(trustFactorOutput.getStatusCode());

                                subClassAnalysisIsIncomplete = true;

                                if (classification.getComputationMethod() == 1) {
                                    addSuggestions(classification, subclassification, suggestionsInClass, trustFactorOutput);
                                } else if (classification.getComputationMethod() == 0) {

                                    if (classification.getType() != 0 && !TextUtils.equals(("wifi"), subclassification.getName())) {
                                        addSuggestionAndCalculateWeight(classification, subclassification, suggestionsInClass, policy, trustFactorOutput);
                                    }

                                }
                            }

                            trustFactorsInClass.add(trustFactorOutput.getTrustFactor());
                            trustFactorsInSubClass.add(trustFactorOutput.getTrustFactor());
                        }
                    }

                }

                if (subClassContainsTrustFactor) {

                    if (!subClassAnalysisIsIncomplete) {
                        statusInClass.add(subclassification.getName() + " check complete");
                    } else {
                        if (subClassDNECodes.contains(DNEStatusCode.DISABLED)) {
                            statusInClass.add(subclassification.getName() + " check disabled");
                        } else if (subClassDNECodes.contains(DNEStatusCode.NO_DATA)) {
                            statusInClass.add(subclassification.getName() + " check complete");
                        } else if (subClassDNECodes.contains(DNEStatusCode.UNAUTHORIZED)) {
                            statusInClass.add(subclassification.getName() + " check unauthorized");
                        } else if (subClassDNECodes.contains(DNEStatusCode.EXPIRED)) {
                            statusInClass.add(subclassification.getName() + " check expired");
                        } else if (subClassDNECodes.contains(DNEStatusCode.UNSUPPORTED)) {
                            statusInClass.add(subclassification.getName() + " check unsupported");
                        } else if (subClassDNECodes.contains(DNEStatusCode.UNAVAILABLE)) {
                            statusInClass.add(subclassification.getName() + " check unavailable");
                        } else if (subClassDNECodes.contains(DNEStatusCode.INVALID)) {
                            statusInClass.add(subclassification.getName() + " check invalid");
                        } else {
                            statusInClass.add(subclassification.getName() + " check error");
                        }
                    }

                    classification.setScore(classification.getScore() + subclassification.getScore() * subclassification.getWeight());

                    subclassification.setTrustFactors(trustFactorsInSubClass);
                    subClassesInClass.add(subclassification);
                }

            }

            classification.setSubclassifications(subClassesInClass);

            classification.setTrustFactors(trustFactorsInClass);

            classification.setTrustFactorsToWhitelist(trustFactorsToWhitelistInClass);

            classification.setTrustFactorsForTransparentAuthentication(trustFactorsForTransparentAuthInClass);

            classification.setStatus(statusInClass);
            classification.setIssues(issuesInClass);
            classification.setSuggestions(suggestionsInClass);
            classification.setDynamicTwoFactors(dynamicTwoFactorsInClass);

            classification.setTrustFactorsNotLearned(trustFactorsNotLearnedInClass);
            classification.setTrustFactorsTriggered(trustFactorsAttributingToScoreInClass);
            classification.setTrustFacotrsWithErrors(trustFactorWithErrorsInClass);
        }

        Log.d("timeCheck", "time: " + (System.nanoTime() - start));

        SentegrityTrustScoreComputation computationResult = new SentegrityTrustScoreComputation();
        computationResult.setPolicy(policy);

        List<String> systemIssues = new ArrayList<>();
        List<String> systemSuggestions = new ArrayList<>();
        List<String> systemSubClassStatuses = new ArrayList<>();

        List<String> userIssues = new ArrayList<>();
        List<String> userSuggestions = new ArrayList<>();
        List<String> userSubClassStatuses = new ArrayList<>();
        List<String> userDynamicTwoFactors = new ArrayList<>();

        List<SentegrityTrustFactorOutput> systemTrustFactorsAttributingToScore = new ArrayList<>();
        List<SentegrityTrustFactorOutput> systemTrustFactorsNotLearned = new ArrayList<>();
        List<SentegrityTrustFactorOutput> systemTrustFactorsWithErrors = new ArrayList<>();
        List<SentegrityTrustFactor> systemAllTrustFactors = new ArrayList<>();
        List<SentegrityTrustFactorOutput> systemTrustFactorsToWhitelist = new ArrayList<>();

        List<SentegrityTrustFactorOutput> userTrustFactorsAttributingToScore = new ArrayList<>();
        List<SentegrityTrustFactorOutput> userTrustFactorsNotLearned = new ArrayList<>();
        List<SentegrityTrustFactorOutput> userTrustFactorsWithErrors = new ArrayList<>();
        List<SentegrityTrustFactor> userAllTrustFactors = new ArrayList<>();
        List<SentegrityTrustFactorOutput> userTrustFactorsToWhitelist = new ArrayList<>();

        List<SentegrityTrustFactorOutput> allTrustFactorsForTransparentAuthentication = new ArrayList<>();

        int systemTrustScoreSum = 0;
        int userTrustScoreSum = 0;

        boolean systemPolicyViolation = false;
        boolean userPolicyViolation = false;

        for (SentegrityClassification classification : policy.getClassifications()) {
            int currentScore = 0;

            if (classification.getComputationMethod() == 0) {
                currentScore = Math.min(100, Math.max(0, 100 - classification.getScore()));
            } else if (classification.getComputationMethod() == 1) {
                currentScore = Math.min(100, classification.getScore());
            }

            if (classification.getType() == 0) {
                systemTrustScoreSum = systemTrustScoreSum + classification.getScore();

                switch (classification.getID()) {
                    case 1:
                        computationResult.setSystemBreachClass(classification);
                        computationResult.setSystemBreachScore(currentScore);
                        break;
                    case 2:
                        computationResult.setSystemPolicyClass(classification);
                        computationResult.setSystemPolicyScore(currentScore);
                        if (currentScore < 100) {
                            systemPolicyViolation = true;
                        }
                        break;
                    case 3:
                        computationResult.setSystemSecurityClass(classification);
                        computationResult.setSystemSecurityScore(currentScore);
                        break;
                    default:
                        break;
                }

                systemIssues.addAll(classification.getIssues());
                systemSuggestions.addAll(classification.getSuggestions());
                systemSubClassStatuses.addAll(classification.getStatus());

                systemTrustFactorsAttributingToScore.addAll(classification.getTrustFactorsTriggered());
                systemTrustFactorsNotLearned.addAll(classification.getTrustFactorsNotLearned());
                systemTrustFactorsWithErrors.addAll(classification.getTrustFacotrsWithErrors());
                systemAllTrustFactors.addAll(classification.getTrustFactors());

                systemTrustFactorsToWhitelist.addAll(classification.getTrustFactorsToWhitelist());

                allTrustFactorsForTransparentAuthentication.addAll(classification.getTrustFactorsForTransparentAuthentication());
            } else {
                userTrustScoreSum = userTrustScoreSum + classification.getScore();


                switch (classification.getID()) {
                    case 4:
                        computationResult.setUserPolicyClass(classification);
                        computationResult.setUserPolicyScore(currentScore);
                        if (currentScore < 100) {
                            userPolicyViolation = true;
                        }
                        break;
                    case 5:
                        computationResult.setUserAnomalyClass(classification);
                        computationResult.setUserAnomalyScore(currentScore);
                        break;
                    default:
                        break;
                }

                userIssues.addAll(classification.getIssues());
                userSuggestions.addAll(classification.getSuggestions());
                userSubClassStatuses.addAll(classification.getStatus());
                userDynamicTwoFactors.addAll(classification.getAuthenticators());

                userTrustFactorsAttributingToScore.addAll(classification.getTrustFactorsTriggered());
                userTrustFactorsNotLearned.addAll(classification.getTrustFactorsNotLearned());
                userTrustFactorsWithErrors.addAll(classification.getTrustFacotrsWithErrors());
                userAllTrustFactors.addAll(classification.getTrustFactors());

                userTrustFactorsToWhitelist.addAll(classification.getTrustFactorsToWhitelist());

                allTrustFactorsForTransparentAuthentication.addAll(classification.getTrustFactorsForTransparentAuthentication());
            }
        }

        computationResult.setSystemIssues(systemIssues);
        computationResult.setSystemSuggestions(systemSuggestions);
        computationResult.setSystemAnalysisResults(systemSubClassStatuses);

        computationResult.setUserIssues(userIssues);
        computationResult.setUserSuggestions(userSuggestions);
        computationResult.setUserAnalysisResults(userSubClassStatuses);
        computationResult.setUserDynamicTwoFactors(userDynamicTwoFactors);

        computationResult.setTransparentAuthenticationTrustFactorOutputs(allTrustFactorsForTransparentAuthentication);

        computationResult.setUserTrustFactorWhitelist(userTrustFactorsToWhitelist);
        computationResult.setSystemTrustFactorWhitelist(systemTrustFactorsToWhitelist);


        //Debug data
        computationResult.setUserAllTrustFactors(userAllTrustFactors);
        computationResult.setSystemAllTrustFactors(systemAllTrustFactors);

        computationResult.setUserTrustFactorsAttributingToScore(userTrustFactorsAttributingToScore);
        computationResult.setSystemTrustFactorsAttributingToScore(systemTrustFactorsAttributingToScore);

        computationResult.setUserTrustFactorsNotLearned(userTrustFactorsNotLearned);
        computationResult.setSystemTrustFactorsNotLearned(systemTrustFactorsNotLearned);

        computationResult.setUserTrustFactorsWithErrors(userTrustFactorsWithErrors);
        computationResult.setSystemTrustFactorsWithErrors(systemTrustFactorsWithErrors);
        //


        if (systemPolicyViolation) {
            computationResult.setSystemScore(0);
        } else {
            computationResult.setSystemScore(Math.min(100, Math.max(0, 100 - systemTrustScoreSum)));
        }

        if (userPolicyViolation) {
            computationResult.setUserScore(0);
        } else {
            computationResult.setUserScore(Math.min(100, userTrustScoreSum));
        }

        computationResult.setDeviceScore((computationResult.getSystemScore() + computationResult.getUserScore()) / 2);

        return computationResult;
    }

    private static void addSuggestions(SentegrityClassification classification, SentegritySubclassification subclassification, List<String> suggestions, SentegrityTrustFactorOutput output) {
        switch (output.getStatusCode()) {
            case DNEStatusCode.UNAUTHORIZED:
                if (!TextUtils.isEmpty(subclassification.getDneUnauthorized())
                        && !suggestions.contains(subclassification.getDneUnauthorized())) {
                    suggestions.add(subclassification.getDneUnauthorized());
                }
                break;
            case DNEStatusCode.DISABLED:
                if (!TextUtils.isEmpty(subclassification.getDneDisabled())
                        && !suggestions.contains(subclassification.getDneDisabled())) {
                    suggestions.add(subclassification.getDneDisabled());
                }
                break;
            case DNEStatusCode.EXPIRED:
                if (!TextUtils.isEmpty(subclassification.getDneExpired())
                        && !suggestions.contains(subclassification.getDneExpired())) {
                    suggestions.add(subclassification.getDneExpired());
                }
                break;
            case DNEStatusCode.NO_DATA:
                if (!TextUtils.isEmpty(subclassification.getDneNoData())
                        && !suggestions.contains(subclassification.getDneNoData())) {
                    suggestions.add(subclassification.getDneNoData());
                }
                break;
            case DNEStatusCode.INVALID:
                if (!TextUtils.isEmpty(subclassification.getDneInvalid())
                        && !suggestions.contains(subclassification.getDneInvalid())) {
                    suggestions.add(subclassification.getDneInvalid());
                }
                break;
        }
    }

    private static void addSuggestionAndCalculateWeight(SentegrityClassification classification, SentegritySubclassification subclassification, List<String> suggestions, SentegrityPolicy policy, SentegrityTrustFactorOutput output) {
        double penaltyMod;
        switch (output.getStatusCode()) {
            case DNEStatusCode.ERROR:
                penaltyMod = policy.getDneModifiers().getError();
                break;
            case DNEStatusCode.UNAUTHORIZED:
                penaltyMod = policy.getDneModifiers().getUnauthorized();
                if (!TextUtils.isEmpty(subclassification.getDneUnauthorized())
                        && !suggestions.contains(subclassification.getDneUnauthorized())) {
                    suggestions.add(subclassification.getDneUnauthorized());
                }
                break;
            case DNEStatusCode.UNSUPPORTED:
                penaltyMod = policy.getDneModifiers().getUnsupported();
                if (!TextUtils.isEmpty(subclassification.getDneUnsupported())
                        && !suggestions.contains(subclassification.getDneUnsupported())) {
                    suggestions.add(subclassification.getDneUnsupported());
                }
                break;
            case DNEStatusCode.UNAVAILABLE:
                penaltyMod = policy.getDneModifiers().getUnavailable();
                if (!TextUtils.isEmpty(subclassification.getDneUnavailable())
                        && !suggestions.contains(subclassification.getDneUnavailable())) {
                    suggestions.add(subclassification.getDneUnavailable());
                }
                break;
            case DNEStatusCode.DISABLED:
                penaltyMod = policy.getDneModifiers().getDisabled();
                if (!TextUtils.isEmpty(subclassification.getDneDisabled())
                        && !suggestions.contains(subclassification.getDneDisabled())) {
                    suggestions.add(subclassification.getDneDisabled());
                }
                break;
            case DNEStatusCode.EXPIRED:
                penaltyMod = policy.getDneModifiers().getExpired();
                if (!TextUtils.isEmpty(subclassification.getDneExpired())
                        && !suggestions.contains(subclassification.getDneExpired())) {
                    suggestions.add(subclassification.getDneExpired());
                }
                break;
            case DNEStatusCode.NO_DATA:
                penaltyMod = policy.getDneModifiers().getNoData();
                if (!TextUtils.isEmpty(subclassification.getDneNoData())
                        && !suggestions.contains(subclassification.getDneNoData())) {
                    suggestions.add(subclassification.getDneNoData());
                }
                break;
            case DNEStatusCode.INVALID:
                penaltyMod = policy.getDneModifiers().getInvalid();
                if (!TextUtils.isEmpty(subclassification.getDneInvalid())
                        && !suggestions.contains(subclassification.getDneInvalid())) {
                    suggestions.add(subclassification.getDneInvalid());
                }
                break;
            default:
                penaltyMod = policy.getDneModifiers().getError();
                break;
        }

        int weight = (int) (output.getTrustFactor().getWeight() * penaltyMod);

        subclassification.setScore(subclassification.getScore() + weight);

        output.setAppliedWeight(weight);

        output.setPercentAppliedWeight(1);
    }
}
