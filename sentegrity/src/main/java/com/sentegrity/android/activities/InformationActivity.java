package com.sentegrity.android.activities;

import android.util.TypedValue;
import android.widget.TextView;

import com.sentegrity.android.R;

/**
 * Created by dmestrov on 20/03/16.
 */
public class InformationActivity extends MenuActivity {

    protected enum InfoType{
        TITLE, FAIL, SUCCESS
    }

    protected TextView createInfo(String text, InfoType type){
        TextView tv = new TextView(this);
        tv.setText(text);

        int textSize = 15;
        int textColor = getResources().getColor(R.color.gray_dark);

        switch (type){
            case TITLE:
                textSize = 14;
                textColor = getResources().getColor(R.color.gray_light);
                break;
            case FAIL:
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_close_black_24dp, 0, 0, 0);
                break;
            case SUCCESS:
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_black_24dp, 0, 0, 0);
                break;
        }

        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        tv.setTextColor(textColor);

        return tv;
    }

}
