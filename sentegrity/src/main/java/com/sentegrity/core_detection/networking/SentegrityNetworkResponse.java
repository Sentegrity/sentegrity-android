package com.sentegrity.core_detection.networking;

import com.google.gson.annotations.SerializedName;
import com.sentegrity.core_detection.policy.SentegrityPolicy;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityNetworkResponse {

    @SerializedName("data")
    private Data data;

    @SerializedName("system")
    private System system;

    public SentegrityPolicy getPolicy(){
        if(data == null)
            return null;
        return data.newPolicy;
    }

    public class System{
        @SerializedName("metadata")
        private String metadata;

        @SerializedName("error")
        private String error;

        @SerializedName("developer")
        private String developer;
    }

    public class Data{
        @SerializedName("newPolicy")
        private SentegrityPolicy newPolicy;
    }
}
