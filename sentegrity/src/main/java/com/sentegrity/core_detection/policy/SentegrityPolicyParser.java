package com.sentegrity.core_detection.policy;

import android.content.Context;

import com.google.gson.Gson;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.startup.SentegrityStartup;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by dmestrov on 17/05/16.
 */
public class SentegrityPolicyParser {

    private static SentegrityPolicyParser sInstance;
    private final Context context;

    private SentegrityPolicy currentPolicy;

    public SentegrityPolicyParser(Context context) {
        this.context = context;
    }

    public static synchronized void initialize(Context context) {
        sInstance = new SentegrityPolicyParser(context);
    }

    public static SentegrityPolicyParser getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }

    public SentegrityPolicy getPolicy() {
        if (currentPolicy == null) {

            File f = new File(policyFilePathInDocumentsFolder());

            if (!f.exists()) {
                SentegrityPolicy policy = loadPolicyFromMainBundle();

                if (policy == null) {
                    currentPolicy = null;
                    return null;
                }

                if (saveNewPolicy(policy)) {
                    currentPolicy = policy;
                } else {
                    currentPolicy = null;
                    return null;
                }
                return currentPolicy;
            } else {
                SentegrityPolicy policy = loadPolicyWithPath(policyFilePathInDocumentsFolder());

                if (policy == null) {
                    SentegrityError error = SentegrityError.UNKNOWN_ERROR;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Parse policy failed").setFailureReason("Unable to parse policy, unknown error").setRecoverySuggestion("Try passing a valid policy path and valid policy"));

                    Logger.INFO("Parse policy failed", error);
                    return null;
                } else {
                    currentPolicy = policy;
                    return currentPolicy;
                }
            }
        } else {
            return currentPolicy;
        }
    }

    private SentegrityPolicy loadPolicyFromMainBundle() {

        if (!policyFileInAssets()) {
            SentegrityError error = SentegrityError.INVALID_POLICY_PATH;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Parse policy failed").setFailureReason("Invalid policy path was provided").setRecoverySuggestion("Try passing a valid policy path"));

            Logger.INFO("Parse policy failed", error);
            return null;
        }

        SentegrityPolicy policy = loadPolicyFromAssets();

        if (policy == null) {
            SentegrityError error = SentegrityError.UNKNOWN_ERROR;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Parse policy failed").setFailureReason("Unable to parse policy, unknown error").setRecoverySuggestion("Try passing a valid policy path and valid policy"));

            Logger.INFO("Parse policy failed", error);
            return null;
        } else {
            return policy;
        }
    }

    private SentegrityPolicy loadPolicyFromAssets() {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = context.getAssets().open(SentegrityConstants.CORE_DETECTION_POLICY_FILE_NAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();

            SentegrityPolicy policy;
            String policyJson = buf.toString();

            policy = new Gson().fromJson(policyJson, SentegrityPolicy.class);
            return policy;
        } catch (IOException e) {
            Logger.INFO("Policy file json formatting problem");
            return null;
        }
    }

    public boolean saveNewPolicy(SentegrityPolicy policy) {
        if (policy == null) {
            SentegrityError error = SentegrityError.INVALID_POLICY_PATH;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Setting policy file unsuccessful").setFailureReason("Policy class reference is invalid").setRecoverySuggestion("Try passing a valid policy object instance"));

            Logger.INFO("Parse policy failed", error);
            return false;
        }

        String stringJson = new Gson().toJson(policy);

        File f = new File(policyFilePathInDocumentsFolder());

        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileUtils.write(f, stringJson);
            return true;
        } catch (IOException e) {
            SentegrityError error = SentegrityError.UNABLE_TO_WRITE_STORE;
            error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to write policy file").setFailureReason("Unable to write policy file").setRecoverySuggestion("Try providing correct policy to write out"));

            Logger.INFO("Failed to Write Startup Store", error);
        }
        return false;
    }

    private boolean policyFileInAssets() {
        try {
            InputStream stream = context.getAssets().open(SentegrityConstants.CORE_DETECTION_POLICY_FILE_NAME);
            stream.close();
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    private String policyFilePathInDocumentsFolder() {
        return context.getFilesDir().getAbsolutePath() + File.separator + SentegrityConstants.CORE_DETECTION_POLICY_FILE_NAME;
    }

    private SentegrityPolicy loadPolicyWithPath(String s) {
        File f = new File(s);

        if (!f.exists()) {
            return null;
        }

        SentegrityPolicy policy;
        String policyJson;
        try {
            policyJson = FileUtils.readFileToString(f);
            policy = new Gson().fromJson(policyJson, SentegrityPolicy.class);
            return policy;
        } catch (IOException e) {
            Logger.INFO("Policy file json formatting problem");
        }
        return null;
    }

    public void setCurrentPolicy(SentegrityPolicy currentPolicy) {
        this.currentPolicy = currentPolicy;
    }
}
