package com.spirometry.spirobanksmartsdksample;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import java.util.Random;


public class PulseActivity extends AppCompatActivity {

    MyParcelable mBundleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        Random r = new Random();
        int subRandom = r.nextInt(5);
        int finalRandom = r.nextInt(subRandom);

        Boolean administerSurvey = finalRandom == 0;

        if (administerSurvey) {
            // administer survey

        } else {
            // skip survey

        }

        Intent intent = new Intent(PulseActivity.this, QuestionnaireInstructionActivity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

}

