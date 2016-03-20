package com.sentegrity.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sentegrity.android.R;

/**
 * Created by dmestrov on 20/03/16.
 */
public class ScoreLayout extends RelativeLayout {

    private PieChart chart;
    private TextView percentageCount;
    private TextView title;

    private int percentage = 0;

    public ScoreLayout(Context context) {
        this(context, null);
    }

    public ScoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(getContext()).inflate(R.layout.score_layout, this);
        chart = (PieChart) findViewById(R.id.pie);
        percentageCount = (TextView) findViewById(R.id.percentage);
        title = (TextView) findViewById(R.id.title);

        readAttributes(attrs);
    }

    private void readAttributes(AttributeSet attributeSet){
        TypedArray arr = getContext().obtainStyledAttributes(attributeSet, R.styleable.ScoreLayout);

        int pieMainColor = arr.getColor(R.styleable.ScoreLayout_pie_main_color, 0);
        int pieBackColor = arr.getColor(R.styleable.ScoreLayout_pie_back_color, 0);
        float pieStroke = arr.getDimension(R.styleable.ScoreLayout_pie_stroke, 0);
        float titleSize = arr.getDimension(R.styleable.ScoreLayout_title_text_size, 0);
        float percentageSize = arr.getDimension(R.styleable.ScoreLayout_percentage_text_size, 0);
        CharSequence titleString = arr.getString(R.styleable.ScoreLayout_title_text);

        if(pieBackColor == 0 || pieMainColor == 0 || pieStroke == 0 || titleSize == 0 || percentageSize == 0) {
            throw new IllegalArgumentException("Please provide all needed attributes for the score layout!");
        }

        chart.setColors(pieMainColor, pieBackColor);
        chart.setStroke(pieStroke);
        percentageCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, percentageSize);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        if(!TextUtils.isEmpty(titleString))
            title.setText(titleString);

        arr.recycle();
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void animatePercentage(final int percentage){
        if(percentage > 100 || percentage < 0){
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                PercentageAnimation animation = new PercentageAnimation(percentage);
                animation.setDuration(500);
                startAnimation(animation);
            }
        }, 250);
    }

    public int getPercentage() {
        return percentage;
    }

    private void setPercentage(int percentage) {
        this.percentage = percentage;
        percentageCount.setText(percentage + "");
        chart.updatePercentage(percentage);
    }

    class PercentageAnimation extends Animation {

        private float oldPercentage;
        private float newPercentage;

        public PercentageAnimation(int newPercentage) {
            this.oldPercentage = getPercentage();
            this.newPercentage = newPercentage;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            int percentage = (int) (oldPercentage + ((newPercentage - oldPercentage) * interpolatedTime));

            setPercentage(percentage);
            requestLayout();
        }
    }
}

