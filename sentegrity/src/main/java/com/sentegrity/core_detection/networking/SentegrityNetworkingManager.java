package com.sentegrity.core_detection.networking;

import android.content.Context;

import com.google.gson.Gson;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.policy.SentegrityPolicyParser;
import com.sentegrity.core_detection.startup.SentegrityHistoryObject;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.util.List;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityNetworkingManager {

    public static void upload(final RunHistoryCallback callback, Context context){

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

        SentegrityNetworkRequest networkRequest = new SentegrityNetworkRequest(currentStartup, currentPolicy);

        SentegrityHTTPSessionManager.postData(context, networkRequest, new NetworkCallback() {
            @Override
            public void onFinish(boolean success, String response) {
                if(!success){
                    if(callback != null){
                        callback.onFinish(false, false, false);
                    }
                    return;
                }

                currentStartup.setTimeOfLastUpload(System.currentTimeMillis() / 1000);
                currentStartup.setRunCountAtLastUpload(runCount);

                removeOldRunHistoryObjects(runHistoryObjects, currentStartup);

                SentegrityNetworkResponse responseObject = new Gson().fromJson(response, SentegrityNetworkResponse.class);
                if(responseObject == null){
                    if(callback != null){
                        callback.onFinish(false, false, false);
                    }
                    return;
                }

                SentegrityPolicy newPolicy = responseObject.getPolicy();

                if(newPolicy != null){
                    if(!SentegrityPolicyParser.getInstance().saveNewPolicy(newPolicy)){
                        if(callback != null){
                            callback.onFinish(false, true, false);
                        }
                        return;
                    }


                    if(callback != null){
                        if(newPolicy.getAllowPrivateAPIs() == 1){
                            //TODO: allow apis? standard user defaults?
                            //allow private APIs
                        }
                        callback.onFinish(true, true, true);
                    }
                }else{
                    if(callback != null){
                        callback.onFinish(true, true, false);
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
