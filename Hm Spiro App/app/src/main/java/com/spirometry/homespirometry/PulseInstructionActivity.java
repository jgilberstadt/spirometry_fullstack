package com.spirometry.homespirometry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.homespirometry.classes.SuperActivity;

public class PulseInstructionActivity extends SuperActivity {
    private NewParcelable mBundleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_pulse_instruction);
        super.onCreate(savedInstanceState);

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
