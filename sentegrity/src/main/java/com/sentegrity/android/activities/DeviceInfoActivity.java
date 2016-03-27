package com.sentegrity.android.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;

import java.util.Random;

/**
 * Created by dmestrov on 20/03/16.
 */
public class DeviceInfoActivity extends InformationActivity {

    private LinearLayout infoHolder;
    private SentegrityTrustScoreComputation computationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        computationResult = CoreDetection.getInstance().getComputationResult();

        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.setTitle("DeviceScore");
        scoreLayout.animatePercentage(computationResult.getSystemScore());

        infoHolder = (LinearLayout) findViewById(R.id.device_info_data);


        TextView systemInfoText = (TextView) findViewById(R.id.device_info_text);
        ImageView systemInfoImage = (ImageView) findViewById(R.id.device_info_image);


        systemInfoText.setText(computationResult.getSystemGUIIconText());

        if(computationResult.getSystemGUIIconID() == 0){
            systemInfoImage.setImageResource(R.drawable.ic_verified_user_black_48dp);
        }


        if(computationResult.getSystemGUIIssues() != null && computationResult.getSystemGUIIssues().size() > 0){
            infoHolder.addView(createInfo("Issues", InfoType.TITLE));

            for(String issue : computationResult.getSystemGUIIssues()){
                infoHolder.addView(createInfo(issue, InfoType.FAIL));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
        if(computationResult.getSystemGUISuggestions() != null && computationResult.getSystemGUISuggestions().size() > 0){
            infoHolder.addView(createInfo("Suggestion", InfoType.TITLE));

            for(String suggestion : computationResult.getSystemGUISuggestions()){
                infoHolder.addView(createInfo(suggestion, InfoType.SUCCESS));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
        if(computationResult.getSystemGUIAnalysis() != null && computationResult.getSystemGUIAnalysis().size() > 0){
            infoHolder.addView(createInfo("Analysis", InfoType.TITLE));

            for(String analysis : computationResult.getSystemGUIAnalysis()){
                infoHolder.addView(createInfo(analysis, analysis.contains("complete") ? InfoType.SUCCESS : InfoType.FAIL));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
    }
}
