package com.spirometry.homespirometry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.spirometry.homespirometry.classes.NewParcelable;


/**
 * Created by ASUS on 6/21/2018.
 */

public class Q2Activity extends AppCompatActivity {

    NewParcelable mBundleData;

    private RadioGroup initialRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_q2);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        populatePreviousAnswers();

        initialRadioGroup = (RadioGroup) findViewById(R.id.initialRadioGroup);

    }

    public void populatePreviousAnswers() {
        int previousState = mBundleData.getQuestionStates(1);
        if(previousState == 1){
            RadioButton checkAnswer = (RadioButton) findViewById(R.id.c1);
            checkAnswer.setChecked(true);
        }
        else if (previousState == 0){
            RadioButton checkAnswer = (RadioButton) findViewById(R.id.c2);
            checkAnswer.setChecked(true);
        }

    }

    public void onClickNext (View v){
        RadioButton initialAnswer = (RadioButton)findViewById(initialRadioGroup.getCheckedRadioButtonId());
        if (initialAnswer.getText().toString().equals(("Yes"))) {
            mBundleData.setQuestionStates(1, 1);
            Intent intent = new Intent(Q2Activity.this, Q2SubActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
        else if (initialAnswer.getText().toString().equals(("No"))) {
            for (int i = 0; i < 4; i++) {
                mBundleData.setSurveyAnswers(7+i, 1);
            }
            mBundleData.setQuestionStates(1, 0);
            Intent intent = new Intent(Q2Activity.this, Q3Activity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }

    public void onClickBack (View v) {
        Intent intent = new Intent(Q2Activity.this, Q1SubActivity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(Q2Activity.this, HelpActivity.class);
        startActivityForResult(intent, 1);
    }

}
