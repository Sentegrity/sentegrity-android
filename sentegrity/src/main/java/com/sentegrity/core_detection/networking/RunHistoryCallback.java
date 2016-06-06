package com.sentegrity.core_detection.networking;

/**
 * Created by dmestrov on 24/05/16.
 */
public interface RunHistoryCallback {
    void onFinish(boolean successfullyExecuted, boolean successfullyUploaded, boolean newPolicyDownloaded);
}
