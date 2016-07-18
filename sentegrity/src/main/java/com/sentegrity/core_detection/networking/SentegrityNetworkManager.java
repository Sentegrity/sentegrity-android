package com.sentegrity.core_detection.networking;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.policy.SentegrityPolicyParser;
import com.sentegrity.core_detection.startup.SentegrityHistoryObject;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityNetworkManager {

    public static void uploadRunHistoryObjectsAndCheckForNewPolicy(final RunHistoryCallback callback, Context context){

        final SentegrityStartup currentStartup = SentegrityStartupStore.getInstance().getStartupStore();

        if(currentStartup == null){
            if(callback != null){
                callback.onFinish(false, false, false);
            }
            return;
        }

        final SentegrityPolicy currentPolicy = SentegrityPolicyParser.getInstance().getPolicy();

        if(currentPolicy == null){
            if(callback != null){
                callback.onFinish(false, false, false);
            }
            return;
        }

        if(currentStartup.getRunHistoryObjects() == null || currentStartup.getRunHistoryObjects().size() == 0){
            if(callback != null){
                callback.onFinish(true, false, false);
            }
            return;
        }

        final int runCount = currentStartup.getRunCount();

        boolean needToUploadData = false;

        if(currentPolicy.getStatusUploadRunFrequency() <= (runCount - currentStartup.getRunCountAtLastUpload())){
            needToUploadData = true;
        }else if(currentPolicy.getStatusUploadTimeFrequency() * 86400 <= (System.currentTimeMillis() / 1000 - currentStartup.getTimeOfLastUpload())){
            needToUploadData = true;
        }

        if(!needToUploadData){
            if(callback != null){
                callback.onFinish(true, false, false);
            }
            return;
        }

        final List<SentegrityHistoryObject> runHistoryObjects = currentStartup.getRunHistoryObjects();


        String email = currentStartup.getEmail();
        if(TextUtils.isEmpty(email))
            email = "";

        final SentegrityUploadRequest request = new SentegrityUploadRequest();

        request.setPolicyID(currentPolicy.getPolicyID());
        request.setPolicyRevision(currentPolicy.getRevision() + "");
        request.setPlatform(currentPolicy.getPlatform());
        request.setEmail(email);
        request.setRunHistoryObjects(new ArrayList<SentegrityHistoryObject>());
        request.setDeviceSalt(currentStartup.getDeviceSaltString());
        request.setApplicationVersionID(currentPolicy.getAppID());
        //request.setDeviceName();


        SentegrityRestClient.postData(context, request, new NetworkCallback() {
            @Override
            public void onFinish(boolean success, String response) {
                if (!success) {
                    if (callback != null) {
                        callback.onFinish(false, false, false);
                    }
                    return;
                }

                currentStartup.setTimeOfLastUpload(System.currentTimeMillis() / 1000);
                currentStartup.setRunCountAtLastUpload(runCount);

                removeOldRunHistoryObjects(runHistoryObjects, currentStartup);

                SentegrityUploadResponse responseObject = new Gson().fromJson(response, SentegrityUploadResponse.class);
                if (responseObject == null) {
                    if (callback != null) {
                        callback.onFinish(false, false, false);
                    }
                    return;
                }

                SentegrityPolicy newPolicy = responseObject.getPolicy();

                if (newPolicy != null) {
                    //TODO: enable save policy when we start getting real android policies
                    /*if(!SentegrityPolicyParser.getInstance().saveNewPolicy(newPolicy)){
                        if(callback != null){
                            callback.onFinish(false, true, false);
                        }
                        return;
                    }*/


                    if (callback != null) {
                        if (newPolicy.getAllowPrivateAPIs() == 1) {
                            //TODO: allow apis? standard user defaults?
                            //allow private APIs
                        }
                        callback.onFinish(true, true, true);
                    }
                } else {
                    if (callback != null) {
                        callback.onFinish(true, true, false);
                    }
                }
            }
        });

    }

    public static void checkForNewPolicyWithEmail(String email, final CheckPolicyCallback callback, Context context){
        final SentegrityStartup currentStartup = SentegrityStartupStore.getInstance().getStartupStore();

        if(currentStartup == null){
            if(callback != null){
                callback.onFinish(false, false);
            }
            return;
        }

        final SentegrityPolicy currentPolicy = SentegrityPolicyParser.getInstance().getPolicy();

        if(currentPolicy == null){
            if(callback != null){
                callback.onFinish(false, false);
            }
            return;
        }

        final SentegrityUploadRequest request = new SentegrityUploadRequest();

        request.setPolicyID(currentPolicy.getPolicyID());
        request.setPolicyRevision(currentPolicy.getRevision() + "");
        request.setPlatform(currentPolicy.getPlatform());
        request.setEmail(email);
        request.setRunHistoryObjects(new ArrayList<SentegrityHistoryObject>());
        request.setDeviceSalt(currentStartup.getDeviceSaltString());
        request.setApplicationVersionID(currentPolicy.getAppID());
        //request.setDeviceName();

        SentegrityRestClient.uploadReport(context, request, new NetworkCallback() {
            @Override
            public void onFinish(boolean success, String response) {
                if(!success){
                    if(callback != null){
                        callback.onFinish(false, false);
                    }
                    return;
                }else{
                    SentegrityUploadResponse responseObject = new Gson().fromJson(response, SentegrityUploadResponse.class);

                    SentegrityPolicy policy = responseObject.getPolicy();

                    if(policy != null){
                        if(!SentegrityPolicyParser.getInstance().saveNewPolicy(policy)){
                            if(callback != null){
                                callback.onFinish(false, false);
                            }
                            return;
                        }

                        if (callback != null) {
                            if (policy.getAllowPrivateAPIs() == 1) {
                                //TODO: allow apis? standard user defaults?
                                //allow private APIs
                            }
                            callback.onFinish(true, true);
                        }
                    } else {
                        if (callback != null) {
                            callback.onFinish(true, false);
                        }
                    }
                }
            }
        });

    }

    private static void removeOldRunHistoryObjects(List<SentegrityHistoryObject> runHistoryObjects, SentegrityStartup currentStartup) {
        List<SentegrityHistoryObject> currentRunHistoryObjects = currentStartup.getRunHistoryObjects();
        currentRunHistoryObjects.removeAll(runHistoryObjects);
        currentStartup.setRunHistoryObjects(currentRunHistoryObjects);
    }
}
