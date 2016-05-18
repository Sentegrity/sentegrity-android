package com.sentegrity.core_detection.crypto;

import android.text.TextUtils;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.startup.SentegrityTransparentAuthObject;

import java.util.Random;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityCrypto {

    private static SentegrityCrypto sInstance;

    public static SentegrityCrypto getInstance(){
        if(sInstance == null){
            sInstance = new SentegrityCrypto();
        }
        return sInstance;
    }

    public byte[] getTransparentKeyForTrustFactorOutput(String output){
        if(TextUtils.isEmpty(output)){
            return null;
        }

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        byte[] transparentKeySaltData = convertHexStringToData(startup.getTransparentAuthGlobalPBKDF2SaltString());

        if(transparentKeySaltData == null){
            return null;
        }

        int transparentRounds = startup.getTransparentAuthPBKDF2rounds();

        byte[] derivedTransparentKey = createPBKDF2Key(output, transparentKeySaltData, transparentRounds);

        if(derivedTransparentKey == null){
            return null;
        }

        return derivedTransparentKey;
    }

    public byte[] getUserKeyForPassword(String password){
        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        byte[] userKeySaltData = convertHexStringToData(startup.getUserKeySaltString());

        if(userKeySaltData == null){
            return null;
        }

        int userRounds = startup.getUserKeyPBKDF2rounds();

        byte[] derivedUserKey = createPBKDF2Key(password, userKeySaltData, userRounds);

        if(derivedUserKey == null){
            return null;
        }

        return derivedUserKey;
    }

    public byte[] decryptMasterKeyUsingUserKey(byte[] userPBKDF2Key){

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        if(startup == null){
            return null;
        }

        String userKeyEncryptedMasterKeyBlobString = startup.getUserKeyEncryptedMasterKeyBlobString();
        String userKeyEncryptedMasterKeySaltString = startup.getUserKeySaltString();

        byte[] decryptedMasterKey = decryptString(userKeyEncryptedMasterKeyBlobString, userPBKDF2Key, userKeyEncryptedMasterKeySaltString);
        if(decryptedMasterKey == null){
            return null;
        }

        return decryptedMasterKey;
    }

    public byte[] decryptMasterKeyUsingTransparentAuthentication(){

        SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        if(computationResults == null){
            return null;
        }

        String masterKeyBlobString = computationResults.getMatchingTransparentAuthenticationObject().getTransparentKeyEncryptedMasterKeyBlobString();
        String masterKeySaltString = computationResults.getMatchingTransparentAuthenticationObject().getTransparentKeyEncryptedMasterKeySaltString();

        byte[] decryptedMasterKey = decryptString(masterKeyBlobString, computationResults.getCandidateTransparentKey(), masterKeySaltString);
        if(decryptedMasterKey == null){
            return null;
        }

        return decryptedMasterKey;
    }

    public SentegrityTransparentAuthObject createNewTransparentAuthKeyObject(){
        SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        byte[] transparentKeymasterKeySaltData = generateSalt256();

        String transparentKeyEncryptedMasterKeyDataBlob = encryptData(computationResults.getDecryptedMasterKey(), computationResults.getCandidateTransparentKey(), transparentKeymasterKeySaltData);

        if(transparentKeyEncryptedMasterKeyDataBlob == null){
            return null;
        }

        String transparentKeyMasterKeySaltString = convertDataToHexString(transparentKeymasterKeySaltData);

        if(transparentKeyMasterKeySaltString == null){
            return null;
        }

        SentegrityTransparentAuthObject newTransparentObject = new SentegrityTransparentAuthObject();
        newTransparentObject.setTransparentKeyEncryptedMasterKeyBlobString(transparentKeyEncryptedMasterKeyDataBlob);
        newTransparentObject.setTransparentKeyEncryptedMasterKeySaltString(transparentKeyMasterKeySaltString);
        newTransparentObject.setTransparentKeyPBKDF2HashString(computationResults.getCandidateTransparentKeyHashString());

        newTransparentObject.setDecayMetric(1);
        newTransparentObject.setHitCount(1);
        newTransparentObject.setLastTime(SentegrityTrustFactorDatasets.getInstance().getRunTime());
        newTransparentObject.setCreated(SentegrityTrustFactorDatasets.getInstance().getRunTime());

        return newTransparentObject;
    }

    public String provisionNewUserKeyAndCreateMasterKeyWithPassword(String userPassword){
        String newMasterKeyString;

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        if(startup == null){
            return null;
        }

        byte[] userSaltData = convertHexStringToData(startup.getUserKeySaltString());

        if(userSaltData == null){
            return null;
        }

        byte[] userKeyData = createPBKDF2Key(userPassword, userSaltData, startup.getUserKeyPBKDF2rounds());

        if(userKeyData == null){
            return null;
        }

        String userKeyPBKDF2HashString = createSHA1HashOfData(userKeyData);

        if(userKeyPBKDF2HashString == null){
            return null;
        }

        byte[] newMasterKey = generateSalt256();

        String userKeyEncryptedMasterKeyBlobString = encryptData(newMasterKey, userKeyData, userSaltData);

        if(userKeyEncryptedMasterKeyBlobString == null){
            return null;
        }

        startup.setUserKeyEncryptedMasterKeyBlobString(userKeyEncryptedMasterKeyBlobString);

        newMasterKeyString = convertDataToHexString(newMasterKey);

        return newMasterKeyString;
    }

    public boolean updateUserKeyForExistingMasterKeyWithPassword(String userPassword, byte[] masterKey){
        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        if(startup == null){
            return false;
        }

        byte[] userSaltData = convertHexStringToData(startup.getUserKeySaltString());

        byte[] userKeyData = createPBKDF2Key(userPassword, userSaltData, startup.getUserKeyPBKDF2rounds());

        if(userKeyData == null){
            return false;
        }

        String userKeyPBKDF2HashString = createSHA1HashOfData(userKeyData);

        if(userKeyPBKDF2HashString == null){
            return false;
        }

        startup.setUserKeyHash(userKeyPBKDF2HashString);

        String userKeyEncryptedMasterKeyBlobString = encryptData(masterKey, userKeyData, userSaltData);

        if(userKeyEncryptedMasterKeyBlobString == null){
            return false;
        }

        startup.setUserKeyEncryptedMasterKeyBlobString(userKeyEncryptedMasterKeyBlobString);

        return true;
    }

    public byte[] generateSalt256() {
        String salt = "";
        Random r = new Random();
        for(int i = 0; i < 32; i++){
            salt += String.valueOf(Character.highSurrogate(r.nextInt()));
        }
        return salt.getBytes();
    }

    public byte[] decryptString(String encryptedDataString, byte[] keyData, String saltString) {
        byte[] encryptedDataData = convertHexStringToData(encryptedDataString);

        if(encryptedDataData == null){
            return null;
        }

        byte[] saltData = convertHexStringToData(saltString);

        if(saltData == null){
            return null;
        }

        byte[] decryptedData = new byte[32];
        return null;
    }

    public String encryptData(byte[] decryptedMasterKey, byte[] candidateTransparentKey, byte[] transparentKeymasterKeySaltData) {
        return null;
    }

    public String createSHA1HashOfData(byte[] userKeyData) {
        return null;
    }

    public String convertDataToHexString(byte[] transparentKeymasterKeySaltData) {
        return null;
    }

    public byte[] convertHexStringToData(String transparentAuthGlobalPBKDF2SaltString) {
        return null;
    }

    public byte[] createPBKDF2Key(String output, byte[] transparentKeySaltData, int transparentRounds) {
        return null;
    }

    public int benchmarkPBKDF2UsingExampleString(String example, int timeMillis){
        return new Random().nextInt(10);
    }


}
