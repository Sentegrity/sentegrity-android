package com.sentegrity.core_detection.networking;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityNetworkResponse {

    @SerializedName("data")
    private Object data;

    @SerializedName("system")
    private System system;

    public class System{
        @SerializedName("metadata")
        private String metadata;

        @SerializedName("error")
        private String error;

        @SerializedName("developer")
        private String developer;
    }
}
