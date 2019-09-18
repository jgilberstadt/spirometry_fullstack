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

public class Q3Activity extends AppCompatActivity {

    NewParcelable mBundleData;

    private RadioGroup initialRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_q3);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        populatePreviousAnswers();

        initialRadioGroup = (RadioGroup) findViewById(R.id.initialRadioGroup);
    }

    public void populatePreviousAnswers() {
        int previousState = mBundleData.getQuestionStates(2);
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
        RadioButton initialAnswer = (RadioButton) findViewById(initialRadioGroup.getCheckedRadioButtonId());
        if (initialAnswer.getText().toString().equals(("Yes"))) {
            mBundleData.setQuestionStates(2, 1);
            Intent intent = new Intent(Q3Activity.this, Q3SubActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
        else if (initialAnswer.getText().toString().equals(("No"))) {
            for (int i = 0; i < 5; i++) {
                mBundleData.setSurveyAnswers(11 + i, 1);
            }
            mBundleData.setQuestionStates(2, 0);
            Intent intent = new Intent(Q3Activity.this, Q4Activity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }

    public void onClickBack (View v) {
        Intent intent = new Intent(Q3Activity.this, Q2SubActivity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(Q3Activity.this, HelpActivity.class);
        startActivityForResult(intent, 1);
    }
}
