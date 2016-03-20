package com.sentegrity.android.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;

import java.util.Random;

/**
 * Created by dmestrov on 20/03/16.
 */
public class DeviceInfoActivity extends InformationActivity {

    private Random r = new Random();
    private LinearLayout infoHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.setTitle("DeviceScore");
        scoreLayout.animatePercentage(r.nextInt(100) + 1);

        infoHolder = (LinearLayout) findViewById(R.id.device_info_data);

        infoHolder.addView(createInfo("Issues", InfoType.TITLE));
        infoHolder.addView(createInfo("Malicious filesystems artifacts", InfoType.FAIL));
        infoHolder.addView(createInfo("Sandbox integrity violated", InfoType.FAIL));

        infoHolder.addView(createInfo("\nSuggestions", InfoType.TITLE));
        infoHolder.addView(createInfo("Reinstall operating system", InfoType.SUCCESS));

        infoHolder.addView(createInfo("\nAnalysis", InfoType.TITLE));
        infoHolder.addView(createInfo("Wifi check complete", InfoType.SUCCESS));
        infoHolder.addView(createInfo("Cellular check complete", InfoType.SUCCESS));
        infoHolder.addView(createInfo("Platform check complete", InfoType.FAIL));
        infoHolder.addView(createInfo("Cellular check complete", InfoType.SUCCESS));
        infoHolder.addView(createInfo("Platform check complete", InfoType.FAIL));
    }
}
