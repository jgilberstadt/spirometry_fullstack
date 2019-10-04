package com.spirometry.homespirometry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.homespirometry.classes.SuperActivity;


/**
 * Created by ASUS on 6/21/2018.
 */

public class Q4Activity extends SuperActivity {

    NewParcelable mBundleData;

    private RadioGroup initialRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_q4);
        super.onCreate(savedInstanceState);
        mBundleData = getIntent().getParcelableExtra("bundle-data");

        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        populatePreviousAnswers();

        initialRadioGroup = (RadioGroup) findViewById(R.id.initialRadioGroup);
    }

    public void populatePreviousAnswers() {
        int previousState = mBundleData.getQuestionStates(3);
        if(previousState == 1){
            RadioButton checkAnswer = (RadioButton) findViewById(R.id.c1);
            checkAnswer.setChecked(true);
        }
        else if (previousState == 0){
            RadioButton checkAnswer = (RadioButton) findViewById(R.id.c2);
            checkAnswer.setChecked(true);
        }
    }

    public void onClickNext (View v) {
        RadioButton initialAnswer = (RadioButton) findViewById(initialRadioGroup.getCheckedRadioButtonId());
        if (initialAnswer.getText().toString().equals(("Yes"))) {
            mBundleData.setQuestionStates(3, 1);
            Intent intent = new Intent(Q4Activity.this, Q4SubActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
        else if (initialAnswer.getText().toString().equals(("No"))) {
            for (int i = 0; i < 3; i++) {
                mBundleData.setSurveyAnswers(16 + i, 1);
            }
            mBundleData.setQuestionStates(3, 0);
            Intent intent = new Intent(Q4Activity.this, TestCompleteActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }

    public void onClickBack (View v) {
        Intent intent = new Intent(Q4Activity.this, Q3SubActivity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(Q4Activity.this, HelpActivity.class);
        startActivityForResult(intent, 1);
    }
}
