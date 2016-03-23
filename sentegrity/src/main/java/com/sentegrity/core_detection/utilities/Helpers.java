package com.sentegrity.core_detection.utilities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmestrov on 23/03/16.
 */
public class Helpers {


    /**
     * Generate Hash (SHA-1) for the given string
     */
    public static String getSHA1Hash(String stringToHash) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // this won't happen for SHA-1
            e.printStackTrace();
            return stringToHash;
        }

        byte[] result;

        try {
            result = digest.digest(stringToHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // this won't happen for UTF-8
            e.printStackTrace();
            return stringToHash;
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}
