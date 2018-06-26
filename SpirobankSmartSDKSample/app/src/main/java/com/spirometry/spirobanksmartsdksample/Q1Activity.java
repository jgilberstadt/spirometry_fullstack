package com.spirometry.spirobanksmartsdksample;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
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

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;


/**
 * Created by ASUS on 6/21/2018.
 */

public class Q1Activity extends AppCompatActivity {

    MyParcelable mBundleData;

    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;
    private RadioGroup radioGroup4;
    private RadioGroup radioGroup5;
    private RadioGroup radioGroup6;
    private RadioGroup radioGroup7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_q1);

        mBundleData = getIntent().getParcelableExtra("bundle-data");
        if (mBundleData == null) {
            mBundleData = new MyParcelable();
        }


        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        radioGroup1 = (RadioGroup) findViewById(R.id.q1_rg);
        radioGroup2 = (RadioGroup) findViewById(R.id.q2_rg);
        radioGroup3 = (RadioGroup) findViewById(R.id.q3_rg);
        radioGroup4 = (RadioGroup) findViewById(R.id.q4_rg);
        radioGroup5 = (RadioGroup) findViewById(R.id.q5_rg);
        radioGroup6 = (RadioGroup) findViewById(R.id.q6_rg);
        radioGroup7 = (RadioGroup) findViewById(R.id.q7_rg);


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
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) marginSize + questionLayout.getHeight() + radioGroup3.getBottom()).setDuration(2000).start();
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
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) (3*(questionLayout.getHeight() + marginSize)) + radioGroup5.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });
        radioGroup5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) (4*(questionLayout.getHeight() + marginSize)) + radioGroup6.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });
        radioGroup6.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                answersSV.post(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(answersSV, "scrollY", (int) (5*(questionLayout.getHeight() + marginSize)) +radioGroup7.getBottom()).setDuration(2000).start();
                    }
                });
            }
        });


    }

    public void onClickNext (View v){
        int selectedId1 = radioGroup1.getCheckedRadioButtonId();
        int selectedId2 = radioGroup2.getCheckedRadioButtonId();
        int selectedId3 = radioGroup3.getCheckedRadioButtonId();
        int selectedId4 = radioGroup4.getCheckedRadioButtonId();
        int selectedId5 = radioGroup5.getCheckedRadioButtonId();
        int selectedId6 = radioGroup6.getCheckedRadioButtonId();
        int selectedId7 = radioGroup7.getCheckedRadioButtonId();


        int[] selectedIdArr = {selectedId1, selectedId2, selectedId3, selectedId4, selectedId5, selectedId6, selectedId7 };

        Boolean skipped = false;

        for (int i = 0; i < selectedIdArr.length; i++) {
            if (selectedIdArr[i] == -1) {
                skipped = true;
                mBundleData.setSurveyAnswers(i, -1);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(selectedIdArr[i]);
                if (radioButton.getText().toString().equals("Yes")) {
                    mBundleData.setSurveyAnswers(i, 1);
                } else {
                    mBundleData.setSurveyAnswers(i, 0);
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
                    Intent intent = new Intent(Q1Activity.this, Q2Activity.class);
                    intent.putExtra("bundle-data", mBundleData);
                    startActivity(intent);
                }
            });
        } else {
            Intent intent = new Intent(Q1Activity.this, Q2Activity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }

    public void onClickBack (View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.warning_popup);
        dialog.setTitle("Title...");
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.txt_dia);
        text.setText("You are at the beginning of the questionnaire");
        Button yesBtn = (Button) dialog.findViewById(R.id.btn_yes);
        dialog.show();

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



}
