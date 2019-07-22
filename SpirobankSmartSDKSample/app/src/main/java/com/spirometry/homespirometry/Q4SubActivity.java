package com.spirometry.homespirometry;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.spirometry.homespirometry.classes.NewParcelable;

public class Q4SubActivity extends AppCompatActivity {

    NewParcelable mBundleData;

    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_q4);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        populatePreviousAnswers();

        radioGroup1 = (RadioGroup) findViewById(R.id.q1_rg);
        radioGroup2 = (RadioGroup) findViewById(R.id.q2_rg);
        radioGroup3 = (RadioGroup) findViewById(R.id.q3_rg);
    }

    public void populatePreviousAnswers() {
        ScrollView answersSV = (ScrollView) findViewById(R.id.answersSV);
        answersSV.setVisibility(View.VISIBLE);
        LinearLayout initialAnswers = (LinearLayout) findViewById(R.id.initialRadio);
        initialAnswers.setVisibility(View.GONE);
        TextView questionTV = (TextView) findViewById(R.id.Q4text);
        questionTV.setText(R.string.ifYes);

        Resources res = getResources();

        for (int i = 0; i < 3; i++) {
            int yesId = res.getIdentifier("c" + (i+1) + "1", "id", getApplicationContext().getPackageName());
            int noId = res.getIdentifier("c" + (i+1) + "2", "id", getApplicationContext().getPackageName());

            int answer = mBundleData.getSurveyAnswers(16+i);
            if (answer == 1) {
                RadioButton checkAnswer = (RadioButton) findViewById(noId);
                checkAnswer.setChecked(true);
            } else if (answer == 2) {
                RadioButton checkAnswer = (RadioButton) findViewById(yesId);
                checkAnswer.setChecked(true);
            }
        }
    }


    public void onClickNext (View v) {
        int selectedId1 = radioGroup1.getCheckedRadioButtonId();
        int selectedId2 = radioGroup2.getCheckedRadioButtonId();
        int selectedId3 = radioGroup3.getCheckedRadioButtonId();

        int[] selectedIdArr = {selectedId1, selectedId2, selectedId3};
        boolean skipped = false;
        boolean subSymptom = false;

        for (int i = 0; i < selectedIdArr.length; i++) {
            if (selectedIdArr[i] == -1) {
                skipped = true;
                mBundleData.setSurveyAnswers(16 + i, 0);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                if (radioButton.getText().toString().equals("Yes")) {
                    subSymptom = true;
                    mBundleData.setSurveyAnswers(16 + i, 2);
                } else {
                    mBundleData.setSurveyAnswers(16+ i, 1);
                }
            }
        }

        if(subSymptom){
            mBundleData.setQuestionStates(3, 1);
        }

        if (skipped) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.skip_warning);
            Button continueBtn = (Button) dialog.findViewById(R.id.contBtn);
            dialog.show();
            continueBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        else {
            Intent intent = new Intent(Q4SubActivity.this, TestCompleteActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }

    }

    public void onClickBack (View v) {
        Intent intent = new Intent(Q4SubActivity.this, Q4Activity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(Q4SubActivity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }
}
