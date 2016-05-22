package com.sentegrity.android.activities;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.sentegrity.android.R;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;

/**
 * Created by dmestrov on 20/03/16.
 */
public abstract class DebugActivity extends Activity implements View.OnClickListener {

    protected SentegrityTrustScoreComputation computationResult;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        computationResult = CoreDetection.getInstance().getComputationResult();

        TextView activityTitle = (TextView) findViewById(R.id.activity_title);
        if(activityTitle != null)
            activityTitle.setText(getActivityTitle());

        TextView debugInfo = (TextView) findViewById(R.id.debug_data);
        if(debugInfo != null)
            debugInfo.setText(getDebugInfo());

        View close = findViewById(R.id.close_btn);
        if(close != null)
            close.setOnClickListener(this);
    }

    protected abstract String getActivityTitle();
    protected abstract String getDebugInfo();

    @Override
    public void onClick(View v) {
        onBackPressed();
    }
}
