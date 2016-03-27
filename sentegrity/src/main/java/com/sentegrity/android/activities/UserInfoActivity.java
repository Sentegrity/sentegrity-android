package com.sentegrity.android.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentegrity.android.R;
import com.sentegrity.android.widget.ScoreLayout;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityClassificationComputation;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;

import java.util.Random;

/**
 * Created by dmestrov on 20/03/16.
 */
public class UserInfoActivity extends InformationActivity {

    private LinearLayout infoHolder;
    private SentegrityTrustScoreComputation computationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        computationResult = CoreDetection.getInstance().getComputationResult();

        ScoreLayout scoreLayout = (ScoreLayout) findViewById(R.id.score_layout);
        scoreLayout.setTitle("UserScore");
        scoreLayout.animatePercentage(computationResult.getUserScore());

        infoHolder = (LinearLayout) findViewById(R.id.user_info_data);


        TextView userInfoText = (TextView) findViewById(R.id.user_info_text);
        ImageView userInfoImage = (ImageView) findViewById(R.id.user_info_image);


        userInfoText.setText(computationResult.getUserGUIIconText());

        if(computationResult.getUserGUIIconID() == 0){
            userInfoImage.setImageResource(R.drawable.ic_verified_user_black_48dp);
        }


        if(computationResult.getUserGUIAuthenticators() != null && computationResult.getUserGUIAuthenticators().size() > 0){
            infoHolder.addView(createInfo("Authenticators", InfoType.TITLE));

            for(String authenticator : computationResult.getUserGUIAuthenticators()){
                infoHolder.addView(createInfo(authenticator, InfoType.FAIL));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
        if(computationResult.getUserGUIIssues() != null && computationResult.getUserGUIIssues().size() > 0){
            infoHolder.addView(createInfo("Issues", InfoType.TITLE));

            for(String issue : computationResult.getUserGUIIssues()){
                infoHolder.addView(createInfo(issue, InfoType.FAIL));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
        if(computationResult.getUserGUISuggestions() != null && computationResult.getUserGUISuggestions().size() > 0){
            infoHolder.addView(createInfo("Suggestion", InfoType.TITLE));

            for(String suggestion : computationResult.getUserGUISuggestions()){
                infoHolder.addView(createInfo(suggestion, InfoType.SUCCESS));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
        if(computationResult.getUserGUIAnalysis() != null && computationResult.getUserGUIAnalysis().size() > 0){
            infoHolder.addView(createInfo("Analysis", InfoType.TITLE));

            for(String analysis : computationResult.getUserGUIAnalysis()){
                infoHolder.addView(createInfo(analysis, analysis.contains("complete") ? InfoType.SUCCESS : InfoType.FAIL));
            }
            infoHolder.addView(createInfo("", InfoType.TITLE));
        }
    }
}
