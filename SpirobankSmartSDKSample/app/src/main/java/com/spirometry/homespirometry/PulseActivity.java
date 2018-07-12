package com.spirometry.homespirometry;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.R;

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

