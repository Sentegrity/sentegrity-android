package com.sentegrity.core_detection.utilities;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
    private static final String TAG = "checksum";

    public enum Type {
        MD5("MD5", 32),
        SHA1("SHA-1", 40),
        SHA256("SHA-256", 64);

        private String name;
        private int length;

        Type(String name, int length) {
            this.name = name;
            this.length = length;
        }

        public String getName() {
            return name;
        }

        public int getLength() {
            return length;
        }
    }

    public static boolean checkMD5(String checksum, Type type, File updateFile) {
        if (TextUtils.isEmpty(checksum) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateChecksum(updateFile, type);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        Log.v(TAG, "Calculated digest: " + calculatedDigest);
        Log.v(TAG, "Provided digest: " + checksum);

        return calculatedDigest.equalsIgnoreCase(checksum);
    }


    public static String calculateChecksum(File updateFile, Type type) {
        long start = System.currentTimeMillis();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(type.getName());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] checksum = digest.digest();
            BigInteger bigInt = new BigInteger(1, checksum);
            String output = bigInt.toString(16);

            output = padHash(output, type.getLength());

            Log.d(TAG, "duration for: " + type.getName() + " " + (System.currentTimeMillis() - start) + ": " + output);
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for " + type.getName(), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing " + type.getName() + " input stream", e);
            }
        }
    }

    private static String padHash(String initial, int len) {
        while (initial.length() < len)
            initial = "0" + initial;
        return initial;
    }
}