package com.sentegrity.android.activities;

import android.app.Activity;
import android.os.Bundle;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.PieChart;
import com.sentegrity.android.widget.ScoreLayout;

/**
 * Created by dmestrov on 20/03/16.
 */
public class DashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ScoreLayout score = (ScoreLayout) findViewById(R.id.score_layout);
        score.animatePercentage(90);
        score.setTitle("TrustScore");
    }
}
