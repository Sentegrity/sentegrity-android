package com.sentegrity.core_detection.dispatch.trust_factors.helpers.root;

/**
 * Created by dmestrov on 24/04/16.
 */
public class RootDetection {

    public Boolean isRootAvailable;
    public Boolean isBusyBoxAvailable;
    public Boolean isAccessGiven;

    public boolean hasData(){
        return isRootAvailable != null && isBusyBoxAvailable != null && isAccessGiven != null;
    }
}
