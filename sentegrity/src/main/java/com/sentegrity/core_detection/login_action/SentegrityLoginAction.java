package com.sentegrity.core_detection.login_action;

import android.hardware.fingerprint.FingerprintManager;
import android.text.TextUtils;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.assertion_storage.SentegrityAssertionStore;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredTrustFactor;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.constants.AuthenticationResult;
import com.sentegrity.core_detection.constants.PostAuthAction;
import com.sentegrity.core_detection.constants.PreAuthAction;
import com.sentegrity.core_detection.crypto.SentegrityCrypto;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.startup.SentegrityTransparentAuthObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityLoginAction {

    private static SentegrityLoginAction sInstance;

    public static SentegrityLoginAction getInstance() {
        if (sInstance == null) {
            sInstance = new SentegrityLoginAction();
        }
        return sInstance;
    }

    public SentegrityLoginResponseObject attemptLoginWithUserInput(String userInput) {
        SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        SentegrityLoginResponseObject loginResponseObject = new SentegrityLoginResponseObject();

        if (computationResults.getPreAuthenticationAction() == PreAuthAction.TRANSPARENTLY_AUTHENTICATE) {

            computationResults.setDecryptedMasterKey(SentegrityCrypto.getInstance().decryptMasterKeyUsingTransparentAuthentication());

            if (computationResults.getDecryptedMasterKey() != null) {

                loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.SUCCESS);
                loginResponseObject.setResponseLoginTitle("");
                loginResponseObject.setResponseLoginDescription("");
                loginResponseObject.setDecryptedMasterKey(computationResults.getDecryptedMasterKey());

                if (!performPostAuthenticationAction()) {

                    loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.RECOVERABLE_ERROR);
                    loginResponseObject.setResponseLoginTitle("");
                    loginResponseObject.setResponseLoginDescription("");
                    loginResponseObject.setDecryptedMasterKey(computationResults.getDecryptedMasterKey());

                }
            } else {

                loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.IRRECOVERABLE_ERROR);
                loginResponseObject.setResponseLoginTitle("");
                loginResponseObject.setResponseLoginDescription("");
                loginResponseObject.setDecryptedMasterKey(null);

            }
        } else if (computationResults.getPreAuthenticationAction() == PreAuthAction.PROMPT_USER_FOR_PASSWORD
                || computationResults.getPreAuthenticationAction() == PreAuthAction.PROMPT_USER_FOR_PASSWORD_AND_WARN) {

            SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

            if (startup == null) {

                loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.IRRECOVERABLE_ERROR);
                loginResponseObject.setResponseLoginTitle("Authentication Error");
                loginResponseObject.setResponseLoginDescription("An error occured during authentication, please reinstall the application");
                loginResponseObject.setDecryptedMasterKey(null);

                return loginResponseObject;
            }

            byte[] userKey = SentegrityCrypto.getInstance().getUserKeyForPassword(userInput);

            String candidateUserHashKey = SentegrityCrypto.getInstance().createSHA1HashOfData(userKey);

            String storedUserKeyHash = startup.getUserKeyHash();

            if (TextUtils.equals(candidateUserHashKey, storedUserKeyHash)) {

                computationResults.setDecryptedMasterKey(SentegrityCrypto.getInstance().decryptMasterKeyUsingUserKey(userKey));

                if (computationResults.getDecryptedMasterKey() != null) {

                    loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.SUCCESS);
                    loginResponseObject.setResponseLoginTitle("");
                    loginResponseObject.setResponseLoginDescription("");
                    loginResponseObject.setDecryptedMasterKey(computationResults.getDecryptedMasterKey());

                    if (!performPostAuthenticationAction()) {

                        loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.RECOVERABLE_ERROR);
                        loginResponseObject.setResponseLoginTitle("");
                        loginResponseObject.setResponseLoginDescription("");
                        loginResponseObject.setDecryptedMasterKey(computationResults.getDecryptedMasterKey());

                    }
                } else {

                    loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.IRRECOVERABLE_ERROR);
                    loginResponseObject.setResponseLoginTitle("Authentication Error");
                    loginResponseObject.setResponseLoginDescription("An error occured during authentication, please reinstall the application");
                    loginResponseObject.setDecryptedMasterKey(null);

                }
            } else {

                loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.INCORRECT_LOGIN);
                loginResponseObject.setResponseLoginTitle("Incorrect password");
                loginResponseObject.setResponseLoginDescription("Please retry your password");
                loginResponseObject.setDecryptedMasterKey(null);

            }
        } else if (computationResults.getPreAuthenticationAction() == PreAuthAction.BLOCK_AND_WARN) {

            loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.INCORRECT_LOGIN);
            loginResponseObject.setResponseLoginTitle("Access Denied");
            loginResponseObject.setResponseLoginDescription("This device has exceeded it's risk threshold.");
            loginResponseObject.setDecryptedMasterKey(null);

        } else {

            loginResponseObject.setAuthenticationResponseCode(AuthenticationResult.IRRECOVERABLE_ERROR);
            loginResponseObject.setResponseLoginTitle("Authentication error");
            loginResponseObject.setResponseLoginDescription("An error occurred during authentication, please reinstall the application.");
            loginResponseObject.setDecryptedMasterKey(null);

        }

        return loginResponseObject;
    }

    private boolean performPostAuthenticationAction() {
        SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        List<SentegrityTrustFactorOutput> trustFactorsToWhitelist;

        switch (computationResults.getPostAuthenticationAction()){
            case PostAuthAction.WHITELIST_USER_ASSERTIONS:
                trustFactorsToWhitelist = new ArrayList<>();
                trustFactorsToWhitelist.addAll(computationResults.getUserTrustFactorWhitelist());

                if(trustFactorsToWhitelist != null && trustFactorsToWhitelist.size() > 0){
                    if(!whitelistAttributingTrustFactorOutputObjects(trustFactorsToWhitelist)){
                        return false;
                    }
                }
                return true;

            case PostAuthAction.WHITELIST_USER_AND_SYSTEM_ASSERTIONS:
                trustFactorsToWhitelist = new ArrayList<>();
                trustFactorsToWhitelist.addAll(computationResults.getUserTrustFactorWhitelist());
                trustFactorsToWhitelist.addAll(computationResults.getSystemTrustFactorWhitelist());

                if(trustFactorsToWhitelist.size() > 0){
                    if(!whitelistAttributingTrustFactorOutputObjects(trustFactorsToWhitelist)){
                        return false;
                    }
                }
                return true;

            case PostAuthAction.WHITELIST_SYSTEM_ASSERTIONS:
                trustFactorsToWhitelist = new ArrayList<>();
                trustFactorsToWhitelist.addAll(computationResults.getSystemTrustFactorWhitelist());

                if(trustFactorsToWhitelist.size() > 0){
                    if(!whitelistAttributingTrustFactorOutputObjects(trustFactorsToWhitelist)){
                        return false;
                    }
                }
                return true;

            case PostAuthAction.WHITELIST_USER_ASSERTIONS_CREATE_TRANSPARENT_KEY:
                trustFactorsToWhitelist = new ArrayList<>();
                trustFactorsToWhitelist.addAll(computationResults.getUserTrustFactorWhitelist());

                if(trustFactorsToWhitelist.size() > 0){
                    if(!whitelistAttributingTrustFactorOutputObjects(trustFactorsToWhitelist)){
                        return false;
                    }
                }

                SentegrityTransparentAuthObject newTransparentObject1 = SentegrityCrypto.getInstance().createNewTransparentAuthKeyObject();

                if(newTransparentObject1 == null){
                    return false;
                }

                SentegrityStartup startup1 = SentegrityStartupStore.getInstance().getStartupStore();

                if(startup1 == null){
                    return false;
                }

                List<SentegrityTransparentAuthObject> currentTransparentAuthKeyObjects1 = startup1.getTransparentAuthKeyObjects();
                if(currentTransparentAuthKeyObjects1 == null)
                    currentTransparentAuthKeyObjects1 = new ArrayList<>();

                currentTransparentAuthKeyObjects1.add(newTransparentObject1);

                startup1.setTransparentAuthKeyObjects(currentTransparentAuthKeyObjects1);
                return true;

            case PostAuthAction.CREATE_TRANSPARENT_KEY:
                SentegrityTransparentAuthObject newTransparentObject2 = SentegrityCrypto.getInstance().createNewTransparentAuthKeyObject();

                if(newTransparentObject2 == null){
                    return false;
                }

                SentegrityStartup startup2 = SentegrityStartupStore.getInstance().getStartupStore();

                if(startup2 == null){
                    return false;
                }

                List<SentegrityTransparentAuthObject> currentTransparentAuthKeyObjects2 = startup2.getTransparentAuthKeyObjects();
                if(currentTransparentAuthKeyObjects2 == null)
                    currentTransparentAuthKeyObjects2 = new ArrayList<>();

                currentTransparentAuthKeyObjects2.add(newTransparentObject2);

                startup2.setTransparentAuthKeyObjects(currentTransparentAuthKeyObjects2);
                return true;

            case PostAuthAction.DO_NOTHING:
                return true;

            default:
                return false;
        }
    }

    private boolean whitelistAttributingTrustFactorOutputObjects(List<SentegrityTrustFactorOutput> trustFactorsToWhitelist) {

        SentegrityAssertionStore localStore = SentegrityTrustFactorStore.getInstance().getAssertionStore();

        if(localStore == null){
            return false;
        }

        for(SentegrityTrustFactorOutput trustFactorOutput : trustFactorsToWhitelist){
            if(trustFactorOutput.getStoredTrustFactor().getAssertions() == null || trustFactorOutput.getStoredTrustFactor().getAssertions().size() == 0){
                trustFactorOutput.getStoredTrustFactor().setAssertions(trustFactorOutput.getCandidateAssertionObjects());
            }else{
                trustFactorOutput.getStoredTrustFactor().getAssertions().addAll(trustFactorOutput.getCandidateAssertionObjectsForWhitelisting());
            }

            SentegrityStoredTrustFactor storedTrustFactor = localStore.getStoredTrustFactor(trustFactorOutput.getTrustFactor().getID());

            if(storedTrustFactor == null){
                continue;
            }

            if(!localStore.replaceInStore(trustFactorOutput.getStoredTrustFactor())){
                continue;
            }
        }

        SentegrityTrustFactorStore.getInstance().setAssertionStore();
        return true;
    }
}
