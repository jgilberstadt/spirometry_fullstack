package com.spirometry.homespirometry;

import android.animation.ObjectAnimator;
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
import com.spirometry.homespirometry.classes.SuperActivity;

public class Q3SubActivity extends SuperActivity {

    NewParcelable mBundleData;

    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;
    private RadioGroup radioGroup4;
    private RadioGroup radioGroup5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_q3);
        super.onCreate(savedInstanceState);
        mBundleData = getIntent().getParcelableExtra("bundle-data");

        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        populatePreviousAnswers();

        radioGroup1 = (RadioGroup) findViewById(R.id.q1_rg);
        radioGroup2 = (RadioGroup) findViewById(R.id.q2_rg);
        radioGroup3 = (RadioGroup) findViewById(R.id.q3_rg);
        radioGroup4 = (RadioGroup) findViewById(R.id.q4_rg);
        radioGroup5 = (RadioGroup) findViewById(R.id.q5_rg);

        final ScrollView answersSV = (ScrollView) findViewById(R.id.answersSV);
        final LinearLayout questionLayout = (LinearLayout) findViewById(R.id.q1_layout);
        final int marginSize = 50;

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) (30 + radioGroup2.getBottom())).setDuration(2000).start();
                    }
                });
            }
        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", marginSize + questionLayout.getHeight() + radioGroup3.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });
        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", (2*(questionLayout.getHeight() + marginSize)) + radioGroup4.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });
        radioGroup4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", 3*(questionLayout.getHeight() + marginSize) + radioGroup5.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });
    }

    public void populatePreviousAnswers() {
        ScrollView answersSV = (ScrollView) findViewById(R.id.answersSV);
        answersSV.setVisibility(View.VISIBLE);
        LinearLayout initialAnswers = (LinearLayout) findViewById(R.id.initialRadio);
        initialAnswers.setVisibility(View.GONE);
        TextView questionTV = (TextView) findViewById(R.id.Q3text);
        questionTV.setText(R.string.ifYes);

        Resources res = getResources();

        for (int i = 0; i < 5; i++) {
            int yesId = res.getIdentifier("c" + (i+1) + "1", "id", getApplicationContext().getPackageName());
            int noId = res.getIdentifier("c" + (i+1) + "2", "id", getApplicationContext().getPackageName());

            int answer = mBundleData.getSurveyAnswers(11+i);
            if (answer == 1) {
                RadioButton checkAnswer = (RadioButton) findViewById(noId);
                checkAnswer.setChecked(true);
            } else if (answer == 2) {
                RadioButton checkAnswer = (RadioButton) findViewById(yesId);
                checkAnswer.setChecked(true);
            }

        }
    }


    public void onClickNext (View v){
        int selectedId1 = radioGroup1.getCheckedRadioButtonId();
        int selectedId2 = radioGroup2.getCheckedRadioButtonId();
        int selectedId3 = radioGroup3.getCheckedRadioButtonId();
        int selectedId4 = radioGroup4.getCheckedRadioButtonId();
        int selectedId5 = radioGroup5.getCheckedRadioButtonId();

        int[] selectedIdArr = {selectedId1, selectedId2, selectedId3, selectedId4, selectedId5};
        boolean skipped = false;
        boolean subSymptom = false;

        for (int i = 0; i < selectedIdArr.length; i++) {
            if (selectedIdArr[i] == -1) {
                skipped = true;
                mBundleData.setSurveyAnswers(11 + i, 0);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                if (radioButton.getText().toString().equals("Yes")) {
                    mBundleData.setSurveyAnswers(11 + i, 2);
                    subSymptom = true;
                } else {
                    mBundleData.setSurveyAnswers(11 + i, 1);
                }
            }
        }

        if(subSymptom){
            mBundleData.setQuestionStates(2, 1);
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
            Intent intent = new Intent(Q3SubActivity.this, Q4Activity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }


    public void onClickBack (View v) {
        Intent intent = new Intent(Q3SubActivity.this, Q3Activity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }


    public void onClickHelp(View view) {
        Intent intent = new Intent(Q3SubActivity.this, HelpActivity.class);
        startActivityForResult(intent, 1);
    }
}
