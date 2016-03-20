package com.sentegrity.android.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;

import java.util.Random;

/**
 * Created by dmestrov on 20/03/16.
 */
public class UserInfoActivity extends InformationActivity {

    private Random r = new Random();
    private LinearLayout infoHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.setTitle("UserScore");
        scoreLayout.animatePercentage(r.nextInt(100) + 1);

        infoHolder = (LinearLayout) findViewById(R.id.user_info_data);

        infoHolder.addView(createInfo("Issues", InfoType.TITLE));
        infoHolder.addView(createInfo("Unknown access time", InfoType.FAIL));
        infoHolder.addView(createInfo("Unknown power consumption", InfoType.FAIL));
        infoHolder.addView(createInfo("Unknown battery state", InfoType.FAIL));
        infoHolder.addView(createInfo("Unknown device orientation", InfoType.FAIL));

        infoHolder.addView(createInfo("\nSuggestions", InfoType.TITLE));
        infoHolder.addView(createInfo("Access device during normal hours", InfoType.SUCCESS));
        infoHolder.addView(createInfo("Charge device regularly", InfoType.SUCCESS));
        infoHolder.addView(createInfo("Enable location services", InfoType.SUCCESS));

        infoHolder.addView(createInfo("\nAnalysis", InfoType.TITLE));
        infoHolder.addView(createInfo("Wifi check complete", InfoType.SUCCESS));
        infoHolder.addView(createInfo("Location check disabled", InfoType.FAIL));
        infoHolder.addView(createInfo("Position check unsupported", InfoType.FAIL));
        infoHolder.addView(createInfo("Time check complete", InfoType.SUCCESS));
    }
}
