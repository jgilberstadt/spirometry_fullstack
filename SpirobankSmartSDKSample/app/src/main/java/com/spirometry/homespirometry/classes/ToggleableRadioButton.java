package com.spirometry.homespirometry.classes;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioGroup;

/**
 * Created by micha on 2/26/2018.
 */

public class ToggleableRadioButton extends android.support.v7.widget.AppCompatRadioButton {


    public ToggleableRadioButton(Context context) {
        super(context);
    }

    public ToggleableRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleableRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void toggle() {
        if(isChecked()) {
            if(getParent() instanceof RadioGroup) {
                ((RadioGroup)getParent()).clearCheck();
            }
        } else {
            setChecked(true);
        }
    }
}