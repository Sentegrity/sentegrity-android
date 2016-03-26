package com.sentegrity.core_detection.computation;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.policy.SentegrityPolicy;

import java.util.List;

/**
 * Created by dmestrov on 21/03/16.
 */
public class SentegrityTrustScoreComputation {

    @SerializedName("policy")
    private SentegrityPolicy policy;

    private List<SentegrityTrustFactorOutput> userTrustFactorsNotLearned;
    private List<SentegrityTrustFactorOutput> systemTrustFactorsNotLearned;

    private List<SentegrityTrustFactorOutput> userTrustFactorsAttributingToScore;
    private List<SentegrityTrustFactorOutput> systemTrustFactorsAttributingToScore;

    private List<SentegrityTrustFactorOutput> userTrustFactorsWithErrors;
    private List<SentegrityTrustFactorOutput> systemTrustFactorsWithErrors;

    private List<SentegrityTrustFactorOutput> userAllTrustFactorOutputObjects;
    private List<SentegrityTrustFactorOutput> systemAllTrustFactorOutputObjects;

    @SerializedName("systemBreachScore")
    private int systemBreachScore;

    @SerializedName("systemPolicyScore")
    private int systemPolicyScore;

    @SerializedName("systemSecurityScore")
    private int systemSecurityScore;

    @SerializedName("userPolicyScore")
    private int userPolicyScore;

    @SerializedName("userAnomalyScore")
    private int userAnomalyScore;

    @SerializedName("systemScore")
    private int systemScore;

    @SerializedName("systemTrusted")
    private boolean systemTrusted;

    @SerializedName("systemGUIIconID")
    private int systemGUIIconID;

    @SerializedName("systemGUIIconText")
    private String systemGUIIconText;

    @SerializedName("systemGUIIssues")
    private List<String> systemGUIIssues;

    @SerializedName("systemGUISuggestions")
    private List<String> systemGUISuggestions;

    @SerializedName("systemGUIAnalysis")
    private List<String> systemGUIAnalysis;

    @SerializedName("userScore")
    private int userScore;

    @SerializedName("userTrusted")
    private boolean userTrusted;

    @SerializedName("userGUIIconID")
    private int userGUIIconID;

    @SerializedName("userGUIIconText")
    private String userGUIIconText;

    @SerializedName("userGUIIssues")
    private List<String> userGUIIssues;

    @SerializedName("userGUISuggestions")
    private List<String> userGUISuggestions;

    @SerializedName("userGUIAuthenticators")
    private List<String> userGUIAuthenticators;

    @SerializedName("userGUIAnalysis")
    private List<String> userGUIAnalysis;

    @SerializedName("deviceScore")
    private int deviceScore;

    @SerializedName("deviceTrusted")
    private boolean deviceTrusted;

    @SerializedName("protectModeClassID")
    private int protectModeClassID;

    @SerializedName("protectModeAction")
    private int protectModeAction;

    @SerializedName("protectModeMessage")
    private String protectModeMessage;

    @SerializedName("protectModeWhitelist")
    private List<SentegrityTrustFactorOutput> protectModeWhitelist;

    @SerializedName("protectModeUserWhitelist")
    private List<SentegrityTrustFactorOutput> protectModeUserWhitelist;

    @SerializedName("protectModeSystemWhitelist")
    private List<SentegrityTrustFactorOutput> protectModeSystemWhitelist;

    @SerializedName("transparentAuthenticationTrustFactors")
    private List<SentegrityTrustFactorOutput> transparentAuthenticationTrustFactors;

    @SerializedName("attemptTransparentAuthentication")
    private boolean attemptTransparentAuthentication;


    public static SentegrityTrustScoreComputation performTrustFactorComputation(SentegrityPolicy policy, List<SentegrityTrustFactorOutput> trustFactorOutputs){
        return new SentegrityTrustScoreComputation();
    }

    public SentegrityPolicy getPolicy() {
        return policy;
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

    public List<SentegrityTrustFactorOutput> getUserAllTrustFactorOutputObjects() {
        return userAllTrustFactorOutputObjects;
    }

    public List<SentegrityTrustFactorOutput> getSystemAllTrustFactorOutputObjects() {
        return systemAllTrustFactorOutputObjects;
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

    public List<String> getSystemGUIIssues() {
        return systemGUIIssues;
    }

    public List<String> getSystemGUISuggestions() {
        return systemGUISuggestions;
    }

    public List<String> getSystemGUIAnalysis() {
        return systemGUIAnalysis;
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

    public List<String> getUserGUIIssues() {
        return userGUIIssues;
    }

    public List<String> getUserGUISuggestions() {
        return userGUISuggestions;
    }

    public List<String> getUserGUIAuthenticators() {
        return userGUIAuthenticators;
    }

    public List<String> getUserGUIAnalysis() {
        return userGUIAnalysis;
    }

    public boolean isAttemptTransparentAuthentication() {
        return attemptTransparentAuthentication;
    }

    public int getDeviceScore() {
        return deviceScore;
    }

    public boolean isDeviceTrusted() {
        return deviceTrusted;
    }

    public int getProtectModeClassID() {
        return protectModeClassID;
    }

    public int getProtectModeAction() {
        return protectModeAction;
    }

    public String getProtectModeMessage() {
        return protectModeMessage;
    }

    public List<SentegrityTrustFactorOutput> getProtectModeWhitelist() {
        return protectModeWhitelist;
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

    public void setUserAllTrustFactorOutputObjects(List<SentegrityTrustFactorOutput> userAllTrustFactorOutputObjects) {
        this.userAllTrustFactorOutputObjects = userAllTrustFactorOutputObjects;
    }

    public void setSystemAllTrustFactorOutputObjects(List<SentegrityTrustFactorOutput> systemAllTrustFactorOutputObjects) {
        this.systemAllTrustFactorOutputObjects = systemAllTrustFactorOutputObjects;
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

    public void setSystemGUIIssues(List<String> systemGUIIssues) {
        this.systemGUIIssues = systemGUIIssues;
    }

    public void setSystemGUISuggestions(List<String> systemGUISuggestions) {
        this.systemGUISuggestions = systemGUISuggestions;
    }

    public void setSystemGUIAnalysis(List<String> systemGUIAnalysis) {
        this.systemGUIAnalysis = systemGUIAnalysis;
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

    public void setUserGUIIssues(List<String> userGUIIssues) {
        this.userGUIIssues = userGUIIssues;
    }

    public void setUserGUISuggestions(List<String> userGUISuggestions) {
        this.userGUISuggestions = userGUISuggestions;
    }

    public void setUserGUIAuthenticators(List<String> userGUIAuthenticators) {
        this.userGUIAuthenticators = userGUIAuthenticators;
    }

    public void setUserGUIAnalysis(List<String> userGUIAnalysis) {
        this.userGUIAnalysis = userGUIAnalysis;
    }

    public void setAttemptTransparentAuthentication(boolean attemptTransparentAuthentication) {
        this.attemptTransparentAuthentication = attemptTransparentAuthentication;
    }

    public void setDeviceScore(int deviceScore) {
        this.deviceScore = deviceScore;
    }

    public void setDeviceTrusted(boolean deviceTrusted) {
        this.deviceTrusted = deviceTrusted;
    }

    public void setProtectModeClassID(int protectModeClassID) {
        this.protectModeClassID = protectModeClassID;
    }

    public void setProtectModeAction(int protectModeAction) {
        this.protectModeAction = protectModeAction;
    }

    public void setProtectModeMessage(String protectModeMessage) {
        this.protectModeMessage = protectModeMessage;
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
}
