package com.sentegrity.android.activities;

import android.os.Bundle;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;

import java.util.Random;

/**
 * Created by dmestrov on 20/03/16.
 */
public class DeviceInfoActivity extends InformationActivity {

    private Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.setTitle("DeviceScore");
        scoreLayout.animatePercentage(r.nextInt(100) + 1);
    }
}
