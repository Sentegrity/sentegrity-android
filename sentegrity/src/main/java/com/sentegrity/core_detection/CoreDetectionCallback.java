package com.sentegrity.core_detection;

import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.logger.SentegrityError;

/**
 * Created by dmestrov on 23/03/16.
 */
public interface CoreDetectionCallback {
    void onFinish(SentegrityTrustScoreComputation computationResult, SentegrityError error, boolean success);
}
