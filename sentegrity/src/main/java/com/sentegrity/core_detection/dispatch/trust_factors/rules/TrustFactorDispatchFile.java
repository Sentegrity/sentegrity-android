package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchFile {

    public static SentegrityTrustFactorOutput blacklisted(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        for(Object objectPath : payload){
            String path = (String) objectPath;

            File f = new File(path);
            if(!f.exists())
                continue;

            outputList.add(path);
        }

        output.setOutput(outputList);

        return output;
    }

    @Deprecated
    public static SentegrityTrustFactorOutput sizeChange(List<Object> payload){
        return new SentegrityTrustFactorOutput();
    }
}
