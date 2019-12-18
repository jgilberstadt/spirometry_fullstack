package com.spirometry.homespirometry;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.homespirometry.classes.SuperActivity;

public class Q1Activity extends SuperActivity {

    NewParcelable mBundleData;
    private RadioGroup initialRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_q1);
        super.onCreate(savedInstanceState);

        mBundleData = getIntent().getParcelableExtra("bundle-data");
        if (mBundleData == null) {
            mBundleData = new NewParcelable();
        }

        populatePreviousAnswers();

        //for keeping the device awake on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Each Radio Group represents a yes and no button option
        initialRadioGroup = (RadioGroup) findViewById(R.id.initialRadioGroup);
    }

    // This is if you move to another question but come back, get the values saved in the bundle, and select previous answers
    public void populatePreviousAnswers() {
        int previousState = mBundleData.getQuestionStates(0);
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
            mBundleData.setQuestionStates(0, 1);
            mBundleData.setSymptomsExist(1);
            Intent intent = new Intent(Q1Activity.this, Q1SubActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
        else if (initialAnswer.getText().toString().equals(("No"))) {
            for (int i = 0; i < 7; i++) {
                mBundleData.setSurveyAnswers(i, 1);
            }
            mBundleData.setQuestionStates(0, 0);
            Intent intent = new Intent(Q1Activity.this, Q2Activity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }

    public void onClickBack (View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.warning_popup);
        dialog.setTitle("Title...");
        Button yesBtn = (Button) dialog.findViewById(R.id.btn_yes);
        dialog.show();
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(Q1Activity.this, HelpActivity.class);
        startActivityForResult(intent, 1);
    }
}
