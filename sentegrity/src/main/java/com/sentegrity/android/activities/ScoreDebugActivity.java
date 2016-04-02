package com.sentegrity.android.activities;

import android.os.Bundle;

import com.sentegrity.android.R;

/**
 * Created by dmestrov on 02/04/16.
 */
public class ScoreDebugActivity extends DebugActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected String getAcivityTitle() {
        return "Computation Debug";
    }

    @Override
    protected String getDebugInfo() {
        String complete = "";

        String policy = "\nPolicy Settings\n+++++++++++++++++++++++++++\n";

        policy += "\nSystem Threshold: " + computationResult.getPolicy().getSystemThreshold() + "\n"
                + "User Threshold: " + computationResult.getPolicy().getUserThreshold() + "\n";

        complete += policy;

        String systemSubScores = "\nSystem Sub Scores\n+++++++++++++++++++++++++++\n";

        systemSubScores += "\nBREACH_INDICATOR: " + computationResult.getSystemBreachScore() + "\n"
                + "POLICY_VIOLATION: " + computationResult.getSystemPolicyScore() + "\n"
                + "SYSTEM_SECURITY: " + computationResult.getSystemSecurityScore() + "\n";

        complete += systemSubScores;

        String userSubScores = "\nUser Sub Scores\n+++++++++++++++++++++++++++\n";

        userSubScores += "\nUSER_POLICY: " + computationResult.getUserPolicyScore() + "\n"
                + "USER_ANOMALY: " + computationResult.getUserAnomalyScore() + "\n";

        complete += userSubScores;

        return complete;
    }
}
