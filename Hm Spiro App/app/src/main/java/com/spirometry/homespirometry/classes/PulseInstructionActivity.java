package com.spirometry.homespirometry.classes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.spirometry.homespirometry.HelpActivity;
import com.spirometry.homespirometry.PulseConnectingActivity;
import com.spirometry.homespirometry.Q1Activity;
import com.spirometry.homespirometry.QuestionnaireInstructionActivity;
import com.spirometry.homespirometry.R;

public class PulseInstructionActivity extends AppCompatActivity {
    private NewParcelable mBundleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_instruction);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

    }

    public void onClickNext(View view) {
        Intent intent = new Intent(PulseInstructionActivity.this, PulseConnectingActivity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(PulseInstructionActivity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }


}
