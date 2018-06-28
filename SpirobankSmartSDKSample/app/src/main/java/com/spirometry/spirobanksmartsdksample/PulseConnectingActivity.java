package com.spirometry.spirobanksmartsdksample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;


public class PulseConnectingActivity extends AppCompatActivity{
    MyParcelable mBundleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_connecting);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

    }

    public void onClickConnect(){
        Intent intent = new Intent(PulseConnectingActivity.this, PulseActivity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }
}
