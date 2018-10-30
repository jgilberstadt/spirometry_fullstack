package com.spirometry.homespirometry;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.classes.NewParcelable;


/**
 * Created by ASUS on 6/21/2018.
 */

public class Q4Activity extends AppCompatActivity {

    NewParcelable mBundleData;

    private RadioGroup initialRadioGroup;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;
    private int questionState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_q4);

        mBundleData = getIntent().getParcelableExtra("bundle-data");


        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        populatePreviousAnswers();

        initialRadioGroup = (RadioGroup) findViewById(R.id.initialRadioGroup);
        radioGroup1 = (RadioGroup) findViewById(R.id.q1_rg);
        radioGroup2 = (RadioGroup) findViewById(R.id.q2_rg);
        radioGroup3 = (RadioGroup) findViewById(R.id.q3_rg);

    }

    public void populatePreviousAnswers() {
        int previousState = mBundleData.getQuestionStates(2);
        Log.d("hyunrae", Integer.toString(previousState));
        if (previousState == 1) {
            questionState = 1;
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
                if (answer == 0) {
                    RadioButton checkAnswer = (RadioButton) findViewById(noId);
                    checkAnswer.setChecked(true);
                } else if (answer == 1) {
                    RadioButton checkAnswer = (RadioButton) findViewById(yesId);
                    checkAnswer.setChecked(true);
                }

            }
        } else if (previousState == 0) {
            RadioButton checkAnswer = (RadioButton) findViewById(R.id.c2);
            checkAnswer.setChecked(true);
        }
    }


    public void onClickNext (View v) {
        if (questionState == 0) {
            if (initialRadioGroup.getCheckedRadioButtonId() == -1) {
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.skip_warning);
                dialog.setTitle("Title...");
                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.txt_dia);
                text.setText("Are you sure you want to skip this question?");
                Button yesBtn = (Button) dialog.findViewById(R.id.btn_yes);
                Button noBtn = (Button) dialog.findViewById(R.id.btn_no);
                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < 3; i++) {
                            mBundleData.setSurveyAnswers(16 + i, 0);
                        }
                        mBundleData.setQuestionStates(2, -1);
                        Intent intent = new Intent(Q4Activity.this, Q3Activity.class);
                        intent.putExtra("bundle-data", mBundleData);
                        startActivity(intent);
                    }
                });
                return;///''

            }

            RadioButton initialAnswer = (RadioButton) findViewById(initialRadioGroup.getCheckedRadioButtonId());
            if (initialAnswer.getText().toString().equals(("Yes"))) {
                questionState = 1;
                mBundleData.setSymptomsExist(1);
                ScrollView answersSV = (ScrollView) findViewById(R.id.answersSV);
                answersSV.setVisibility(View.VISIBLE);
                LinearLayout initialAnswers = (LinearLayout) findViewById(R.id.initialRadio);
                initialAnswers.setVisibility(View.GONE);
                TextView questionTV = (TextView) findViewById(R.id.Q4text);
                questionTV.setText(R.string.ifYes);
                return;
            } else if (initialAnswer.getText().toString().equals(("No"))) {
                for (int i = 0; i < 3; i++) {
                    mBundleData.setSurveyAnswers(16 + i, 1);
                }
                mBundleData.setQuestionStates(2, 0);
                Intent intent = new Intent(Q4Activity.this, TestCompleteActivity.class);
                intent.putExtra("bundle-data", mBundleData);
                startActivity(intent);
            }
        } else if (questionState == 1) {
            int selectedId1 = radioGroup1.getCheckedRadioButtonId();
            int selectedId2 = radioGroup2.getCheckedRadioButtonId();
            int selectedId3 = radioGroup3.getCheckedRadioButtonId();

            int[] selectedIdArr = {selectedId1, selectedId2, selectedId3};
            Boolean skipped = false;

            for (int i = 0; i < selectedIdArr.length; i++) {
                if (selectedIdArr[i] == -1) {
                    skipped = true;
                    mBundleData.setSurveyAnswers(16 + i, 0);
                } else {
                    RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                    if (radioButton.getText().toString().equals("Yes")) {
                        mBundleData.setSurveyAnswers(16 + i, 2);
                    } else {
                        mBundleData.setSurveyAnswers(16+ i, 1);
                    }
                }

            }

            if (skipped == true) {
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.skip_warning);
                dialog.setTitle("Title...");
                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.txt_dia);
                text.setText("Are you sure you want to skip this question?");
                Button yesBtn = (Button) dialog.findViewById(R.id.btn_yes);
                Button noBtn = (Button) dialog.findViewById(R.id.btn_no);
                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBundleData.setQuestionStates(2, 1);
                        Intent intent = new Intent(Q4Activity.this, TestCompleteActivity.class);
                        intent.putExtra("bundle-data", mBundleData);
                        startActivity(intent);
                    }
                });
            } else {
                mBundleData.setQuestionStates(2, 1);
                Intent intent = new Intent(Q4Activity.this, TestCompleteActivity.class);
                intent.putExtra("bundle-data", mBundleData);
                startActivity(intent);
            }
        }
    }

    public void onClickBack (View v) {
        int selectedId1 = radioGroup1.getCheckedRadioButtonId();
        int selectedId2 = radioGroup2.getCheckedRadioButtonId();
        int selectedId3 = radioGroup3.getCheckedRadioButtonId();

        int[] selectedIdArr = {selectedId1, selectedId2, selectedId3 };
        Boolean skipped = false;

        for (int i = 0; i < selectedIdArr.length; i++) {
            if (selectedIdArr[i] == -1) {
                skipped = true;
                mBundleData.setSurveyAnswers(16 + i, 0);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                if (radioButton.getText().toString().equals("Yes")) {
                    mBundleData.setSurveyAnswers(16 + i, 2);
                } else {
                    mBundleData.setSurveyAnswers(16 + i, 1);
                }
            }

        }

        if (skipped == true) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.skip_warning);
            dialog.setTitle("Title...");
            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.txt_dia);
            text.setText("Are you sure you want to skip this question?");
            Button yesBtn = (Button) dialog.findViewById(R.id.btn_yes);
            Button noBtn = (Button) dialog.findViewById(R.id.btn_no);
            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Q4Activity.this, Q3Activity.class);
                    intent.putExtra("bundle-data", mBundleData);
                    startActivity(intent);
                }
            });
        } else {
            Intent intent = new Intent(Q4Activity.this, Q3Activity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }


    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(Q4Activity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

}
