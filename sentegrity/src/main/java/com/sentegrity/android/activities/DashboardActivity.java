package com.sentegrity.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;

import java.util.Random;


/**
 * Created by dmestrov on 20/03/16.
 */
public class DashboardActivity extends MenuActivity implements View.OnClickListener {

    private Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.animatePercentage(r.nextInt(100) + 1);
        scoreLayout.setTitle("TrustScore");

        findViewById(R.id.device_info).setOnClickListener(this);
        findViewById(R.id.user_info).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.device_info:
                Intent deviceInfo = new Intent(this, DeviceInfoActivity.class);
                startActivity(deviceInfo);
                break;
            case R.id.user_info:
                Intent userInfo = new Intent(this, UserInfoActivity.class);
                startActivity(userInfo);
                break;
        }
    }
}
