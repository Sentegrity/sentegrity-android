package com.sentegrity.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.sentegrity.android.R;

/**
 * Created by dmestrov on 20/03/16.
 */
public class PieChart extends RelativeLayout {
    private RectF rect = new RectF();
    private Paint paint = new Paint();
    private int percentage = 0;

    private int mainColor;
    private int bkgColor;

    public PieChart(Context context) {
        this(context, null);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        mainColor = getResources().getColor(R.color.colorPrimaryDark);
        bkgColor = getResources().getColor(R.color.colorGray);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(40);

        setWillNotDraw(false);
    }

    /**
     * Sets colors for the pie chart
     *
     * @param mainColor main color - defined by the percentage number
     * @param bkgColor standard background color of the chart
     */
    public void setColors(int mainColor, int bkgColor){
        this.mainColor = mainColor;
        this.bkgColor = bkgColor;
    }

    /**
     * Sets pie width in pixels
     *
     * @param stroke
     */
    public void setStroke(float stroke){
        paint.setStrokeWidth(stroke);
    }

    public int getPercentage() {
        return percentage;
    }

    /**
     * Updates current percentage and redraws view
     *
     * @param percentage new percentage
     */
    public void updatePercentage(int percentage) {
        this.percentage = percentage;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        w = (int) (w - paint.getStrokeWidth());
        h = (int) (h - paint.getStrokeWidth());
        if (w > h) {
            rect.set(w / 2 - h / 2 + paint.getStrokeWidth(), 0 + paint.getStrokeWidth() / 2, w / 2 + h / 2, h);
        } else {
            rect.set(0 + paint.getStrokeWidth() / 2, h / 2 - w / 2 + paint.getStrokeWidth() / 2, w, h / 2 + w / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(mainColor);
        canvas.drawArc(rect, 90, 360 * percentage / 100, false, paint);

        paint.setColor(bkgColor);
        canvas.drawArc(rect, 360 * percentage / 100 + 90, 360 - 360 * percentage / 100, false, paint);
    }
}