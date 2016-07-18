package com.sentegrity.core_detection.policy;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dmestrov on 18/07/16.
 */
public class SentegrityPasswordRequirements {

    @SerializedName("minLenght")
    private int minLength;

    @SerializedName("alphaNumeric")
    private int alphaNumeric;

    @SerializedName("mixedCase")
    private int mixedCase;

    @SerializedName("specialCharacter")
    private int specialCharacter;

}
