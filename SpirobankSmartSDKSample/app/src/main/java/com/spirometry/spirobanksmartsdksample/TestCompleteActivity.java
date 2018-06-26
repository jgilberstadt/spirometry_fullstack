package com.spirometry.spirobanksmartsdksample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

public class TestCompleteActivity extends AppCompatActivity {

    String[][] arraya; //6 data storing 4 String Values; +-
    private MyParcelable mBundleData;
    private static final String TAG = BlowActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just
        arraya = mBundleData.getBlowDataArray();

        Log.d(TAG, "result blow1" + arraya[4][3]);
        Log.d(TAG, "result blow2" + arraya[0][0]);
        Log.d(TAG, "result blow3" + arraya[1][2]);
        Log.d(TAG, "result blow4" + arraya[2][1]);
        Log.d(TAG, "result blow5" + arraya[3][3]);


    }

}
