package com.sentegrity.core_detection.login_action;

/**
 * Created by dmestrov on 15/05/16.
 */
public class SentegrityLoginResponseObject {

    private int authenticationResponseCode;

    private byte[] decryptedMasterKey;

    private String responseLoginTitle;

    private String responseLoginDescription;

    public int getAuthenticationResponseCode() {
        return authenticationResponseCode;
    }

    public void setAuthenticationResponseCode(int authenticationResponseCode) {
        this.authenticationResponseCode = authenticationResponseCode;
    }

    public byte[] getDecryptedMasterKey() {
        return decryptedMasterKey;
    }

    public void setDecryptedMasterKey(byte[] decryptedMasterKey) {
        this.decryptedMasterKey = decryptedMasterKey;
    }

    public String getResponseLoginTitle() {
        return responseLoginTitle;
    }

    public void setResponseLoginTitle(String responseLoginTitle) {
        this.responseLoginTitle = responseLoginTitle;
    }

    public String getResponseLoginDescription() {
        return responseLoginDescription;
    }

    public void setResponseLoginDescription(String responseLoginDescription) {
        this.responseLoginDescription = responseLoginDescription;
    }
}
