package com.sentegrity.core_detection.networking;

/**
 * Created by dmestrov on 18/07/16.
 */
public interface CheckPolicyCallback {
    void onFinish(boolean successfullyExecuted, boolean newPolicyDownloaded);
}
