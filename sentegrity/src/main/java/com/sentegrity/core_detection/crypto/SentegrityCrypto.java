package com.sentegrity.core_detection.crypto;

import android.text.TextUtils;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.startup.SentegrityTransparentAuthObject;

import org.apache.commons.io.Charsets;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Formatter;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityCrypto {

    private static SentegrityCrypto sInstance;

    public static SentegrityCrypto getInstance() {
        if (sInstance == null) {
            sInstance = new SentegrityCrypto();
        }
        return sInstance;
    }

    public byte[] getTransparentKeyForTrustFactorOutput(String output) {
        if (TextUtils.isEmpty(output)) {
            return null;
        }

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        byte[] transparentKeySaltData = convertHexStringToData(startup.getTransparentAuthGlobalPBKDF2SaltString());

        if (transparentKeySaltData == null) {
            return null;
        }

        int transparentRounds = startup.getTransparentAuthPBKDF2rounds();

        byte[] derivedTransparentKey = createPBKDF2Key(output, transparentKeySaltData, transparentRounds);

        if (derivedTransparentKey == null) {
            return null;
        }

        return derivedTransparentKey;
    }

    public byte[] getUserKeyForPassword(String password) {
        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        byte[] userKeySaltData = convertHexStringToData(startup.getUserKeySaltString());

        if (userKeySaltData == null) {
            return null;
        }

        int userRounds = startup.getUserKeyPBKDF2rounds();

        byte[] derivedUserKey = createPBKDF2Key(password, userKeySaltData, userRounds);

        if (derivedUserKey == null) {
            return null;
        }

        return derivedUserKey;
    }

    public byte[] decryptMasterKeyUsingUserKey(byte[] userPBKDF2Key) {

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        if (startup == null) {
            return null;
        }

        String userKeyEncryptedMasterKeyBlobString = startup.getUserKeyEncryptedMasterKeyBlobString();
        String userKeyEncryptedMasterKeySaltString = startup.getUserKeySaltString();

        byte[] decryptedMasterKey = decryptString(userKeyEncryptedMasterKeyBlobString, userPBKDF2Key, userKeyEncryptedMasterKeySaltString);
        if (decryptedMasterKey == null) {
            return null;
        }

        return decryptedMasterKey;
    }

    public byte[] decryptMasterKeyUsingTransparentAuthentication() {

        SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        if (computationResults == null) {
            return null;
        }

        String masterKeyBlobString = computationResults.getMatchingTransparentAuthenticationObject().getTransparentKeyEncryptedMasterKeyBlobString();
        String masterKeySaltString = computationResults.getMatchingTransparentAuthenticationObject().getTransparentKeyEncryptedMasterKeySaltString();

        byte[] decryptedMasterKey = decryptString(masterKeyBlobString, computationResults.getCandidateTransparentKey(), masterKeySaltString);
        if (decryptedMasterKey == null) {
            return null;
        }

        return decryptedMasterKey;
    }

    public SentegrityTransparentAuthObject createNewTransparentAuthKeyObject() {
        SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        byte[] transparentKeymasterKeySaltData = generateSalt256();

        String transparentKeyEncryptedMasterKeyDataBlob = encryptData(computationResults.getDecryptedMasterKey(), computationResults.getCandidateTransparentKey(), transparentKeymasterKeySaltData);

        if (transparentKeyEncryptedMasterKeyDataBlob == null) {
            return null;
        }

        String transparentKeyMasterKeySaltString = convertDataToHexString(transparentKeymasterKeySaltData);

        if (transparentKeyMasterKeySaltString == null) {
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

    public String provisionNewUserKeyAndCreateMasterKeyWithPassword(String userPassword) {
        String newMasterKeyString;

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        if (startup == null) {
            return null;
        }

        byte[] userSaltData = convertHexStringToData(startup.getUserKeySaltString());

        if (userSaltData == null) {
            return null;
        }

        byte[] userKeyData = createPBKDF2Key(userPassword, userSaltData, startup.getUserKeyPBKDF2rounds());

        if (userKeyData == null) {
            return null;
        }

        String userKeyPBKDF2HashString = createSHA1HashOfData(userKeyData);

        if (userKeyPBKDF2HashString == null) {
            return null;
        }

        startup.setUserKeyHash(userKeyPBKDF2HashString);

        byte[] newMasterKey = generateSalt256();

        String userKeyEncryptedMasterKeyBlobString = encryptData(newMasterKey, userKeyData, userSaltData);

        if (userKeyEncryptedMasterKeyBlobString == null) {
            return null;
        }

        startup.setUserKeyEncryptedMasterKeyBlobString(userKeyEncryptedMasterKeyBlobString);

        newMasterKeyString = convertDataToHexString(newMasterKey);

        return newMasterKeyString;
    }

    public boolean updateUserKeyForExistingMasterKeyWithPassword(String userPassword, byte[] masterKey) {
        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        if (startup == null) {
            return false;
        }

        byte[] userSaltData = convertHexStringToData(startup.getUserKeySaltString());

        byte[] userKeyData = createPBKDF2Key(userPassword, userSaltData, startup.getUserKeyPBKDF2rounds());

        if (userKeyData == null) {
            return false;
        }

        String userKeyPBKDF2HashString = createSHA1HashOfData(userKeyData);

        if (userKeyPBKDF2HashString == null) {
            return false;
        }

        startup.setUserKeyHash(userKeyPBKDF2HashString);

        String userKeyEncryptedMasterKeyBlobString = encryptData(masterKey, userKeyData, userSaltData);

        if (userKeyEncryptedMasterKeyBlobString == null) {
            return false;
        }

        startup.setUserKeyEncryptedMasterKeyBlobString(userKeyEncryptedMasterKeyBlobString);

        return true;
    }

    public byte[] generateSalt256() {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(256);
            return gen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            String salt = "";
            Random r = new Random();
            for (int i = 0; i < 32; i++) {
                salt += String.valueOf(Character.highSurrogate(r.nextInt()));
            }
            byte[] bytes = new byte[32];
            byte[] randomBytes = salt.getBytes();
            for (int i = 0; i < 32; i++) {
                bytes[i] = randomBytes[randomBytes.length - 1 - i];
            }
            return bytes;
        }
    }

    public byte[] decryptString(String encryptedDataString, byte[] keyData, String saltString) {
        byte[] encryptedDataData = convertHexStringToData(encryptedDataString);

        if (encryptedDataData == null) {
            return null;
        }

        byte[] saltData = convertHexStringToData(saltString);

        if (saltData == null) {
            return null;
        }

        byte[] decryptedData = new byte[32];


        try {
            SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");
            Cipher decrypt = Cipher.getInstance("AES");
            decrypt.init(Cipher.DECRYPT_MODE, skeySpec, new SecureRandom(saltData));
            decryptedData = decrypt.doFinal(encryptedDataData);
        } catch (Exception ex) {
            return null;
        }

        return decryptedData;
    }

    public String encryptData(byte[] plainTextData, byte[] keyData, byte[] saltData) {

        byte[] cipherData;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");
            Cipher encrypt = Cipher.getInstance("AES");
            encrypt.init(Cipher.ENCRYPT_MODE, skeySpec, new SecureRandom(saltData));
            cipherData = encrypt.doFinal(encrypt.doFinal(plainTextData));

        } catch (Exception ex) {
            return null;
        }

        String encryptedDataString = convertDataToHexString(cipherData);

        return encryptedDataString;
    }

    public String createSHA1HashOfData(byte[] inputData) {
        Formatter formatter = new Formatter();
        int len = inputData.length;
        for (int j = 0; j < len; j++) {
            formatter.format("%02x", inputData[j]);
        }
        return formatter.toString();
    }

    public String createSHA1HashOfString(String inputData) {
        byte[] inputByte = inputData.getBytes(Charsets.UTF_8);
        return createSHA1HashOfData(inputByte);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String convertDataToHexString(byte[] inputData) {
        int len = inputData.length;
        char[] hexChars = new char[inputData.length * 2];
        for (int j = 0; j < len; j++) {
            int v = inputData[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] convertHexStringToData(String inputString) {
        int len = inputString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(inputString.charAt(i), 16) << 4)
                    + Character.digit(inputString.charAt(i+1), 16));
        }
        return data;
    }

    public byte[] createPBKDF2Key(String plaintextString, byte[] saltData, int rounds) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(plaintextString.toCharArray(), saltData, rounds, 32 * 8);
            SecretKey tmp = factory.generateSecret(spec);

            return tmp.getEncoded();
        }catch (Exception e){
            return null;
        }
    }

    public int benchmarkPBKDF2UsingExampleString(String example, int timeMillis) {
        return 1000 + new Random().nextInt(1000);
    }


    /*
        Cipher aesCipher = Cipher.getInstance("AES");
        SecretKeySpec skeySpec = new SecretKeySpec(SentegrityCrypto.getInstance().generateSalt256(), "AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        Cipher encrypt = Cipher.getInstance("AES");
        encrypt.init(Cipher.ENCRYPT_MODE, skeySpec);

        Cipher decrypt = Cipher.getInstance("AES");
        decrypt.init(Cipher.DECRYPT_MODE, skeySpec);
        decryptedData = decrypt.doFinal(encrypt.doFinal("abcdefg".getBytes(Charsets.UTF_8)));
     */
}
