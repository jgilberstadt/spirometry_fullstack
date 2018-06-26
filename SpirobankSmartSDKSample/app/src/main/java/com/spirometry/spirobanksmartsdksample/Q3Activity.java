package com.spirometry.spirobanksmartsdksample;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;


/**
 * Created by ASUS on 6/21/2018.
 */

public class Q3Activity extends AppCompatActivity {

    MyParcelable mBundleData;

    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_q3);

        mBundleData = getIntent().getParcelableExtra("bundle-data");


        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        radioGroup1 = (RadioGroup) findViewById(R.id.q1_rg);
        radioGroup2 = (RadioGroup) findViewById(R.id.q2_rg);
        radioGroup3 = (RadioGroup) findViewById(R.id.q3_rg);

        final ScrollView answersSV = (ScrollView) findViewById(R.id.answersSV);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) radioGroup2.getBottom()).setDuration(2000).start();
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
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) radioGroup3.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });

    }

    public void onClickNext (View v){
        int selectedId1 = radioGroup1.getCheckedRadioButtonId();
        int selectedId2 = radioGroup2.getCheckedRadioButtonId();
        int selectedId3 = radioGroup3.getCheckedRadioButtonId();

        int[] selectedIdArr = {selectedId1, selectedId2, selectedId3 };
        Boolean skipped = false;

        for (int i = 0; i < selectedIdArr.length; i++) {
            if (selectedIdArr[i] == -1) {
                skipped = true;
                mBundleData.setSurveyAnswers(11 + i, -1);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                if (radioButton.getText().toString() == "Yes") {
                    mBundleData.setSurveyAnswers(11 + i, 1);
                } else {
                    mBundleData.setSurveyAnswers(11 + i, 0);
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

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Q3Activity.this, Q4Activity.class);
                    intent.putExtra("bundle-data", mBundleData);
                    startActivity(intent);
                }
            });
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
                mBundleData.setSurveyAnswers(11 + i, -1);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                if (radioButton.getText().toString() == "Yes") {
                    mBundleData.setSurveyAnswers(11 + i, 1);
                } else {
                    mBundleData.setSurveyAnswers(11 + i, 0);
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

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Q3Activity.this, Q4Activity.class);
                    intent.putExtra("bundle-data", mBundleData);
                    startActivity(intent);
                }
            });
        }


    }



}
