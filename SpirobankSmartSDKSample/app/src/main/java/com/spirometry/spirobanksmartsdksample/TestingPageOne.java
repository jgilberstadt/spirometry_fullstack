package com.spirometry.spirobanksmartsdksample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class TestingPageOne extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_page_one);
        Log.d(TAG, "connection complete?: ");
        Toast.makeText(getApplicationContext(), "We are now on the TestingPage One!", Toast.LENGTH_LONG).show();


    }
}
