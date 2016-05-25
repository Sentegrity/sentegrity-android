package com.sentegrity.core_detection.networking;

import android.content.Context;

import com.sentegrity.core_detection.policy.SentegrityPolicyParser;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityNetworkingManager {

    public static void upload(Context context){
        //sample call
        /*SentegrityHTTPSessionManager.postData(context, new SentegrityNetworkRequest(SentegrityStartupStore.getInstance().getStartupStore(), SentegrityPolicyParser.getInstance().getPolicy()), new NetworkCallback() {
            @Override
            public void onFinish(boolean success, String response) {

            }
        });*/
    }
}
