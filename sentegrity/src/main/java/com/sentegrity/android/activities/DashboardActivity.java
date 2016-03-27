package com.sentegrity.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;

import java.util.Random;


/**
 * Created by dmestrov on 20/03/16.
 */
public class DashboardActivity extends MenuActivity implements View.OnClickListener {

    private SentegrityTrustScoreComputation computationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        computationResult = CoreDetection.getInstance().getComputationResult();


        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.animatePercentage(computationResult.getDeviceScore());
        scoreLayout.setTitle("TrustScore");

        findViewById(R.id.device_info).setOnClickListener(this);
        findViewById(R.id.user_info).setOnClickListener(this);

        TextView deviceInfoText = (TextView) findViewById(R.id.device_info_text);
        TextView userInfoText = (TextView) findViewById(R.id.user_info_text);
        ImageView deviceInfoImage = (ImageView) findViewById(R.id.device_info_image);
        ImageView userInfoImage = (ImageView) findViewById(R.id.user_info_image);


        deviceInfoText.setText(computationResult.getSystemGUIIconText());
        userInfoText.setText(computationResult.getUserGUIIconText());

        if(computationResult.getSystemGUIIconID() == 0){
            deviceInfoImage.setImageResource(R.drawable.ic_verified_user_black_48dp);
        }
        if(computationResult.getUserGUIIconID() == 0){
            userInfoImage.setImageResource(R.drawable.ic_verified_user_black_48dp);
        }


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
