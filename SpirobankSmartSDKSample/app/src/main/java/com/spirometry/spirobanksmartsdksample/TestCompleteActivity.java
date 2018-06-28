package com.spirometry.spirobanksmartsdksample;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestCompleteActivity extends AppCompatActivity {

    String[][] arraya; //6 data storing 4 String Values; +-
    private MyParcelable mBundleData;
    private static final String TAG = BlowActivity.class.getSimpleName();
    TextView testingComplete;
    TextView nextAppointment;

    NotificationCompat.Builder notification;
    private static final int uniqueID = 123456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);

        testingComplete = (TextView) findViewById(R.id.testingComplete);
        nextAppointment = (TextView) findViewById(R.id.nextAppointment);
        nextAppointment.setVisibility(View.INVISIBLE);

             new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nextAppointment.setVisibility(View.VISIBLE);
                testingComplete.setVisibility(View.GONE);
            }
        }, 4000);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just
/*        arraya = mBundleData.getBlowDataArray();

        Log.d(TAG, "result blow1" + arraya[4][3]);
        Log.d(TAG, "result blow2" + arraya[0][0]);
        Log.d(TAG, "result blow3" + arraya[1][2]);
        Log.d(TAG, "result blow4" + arraya[2][1]);
        Log.d(TAG, "result blow5" + arraya[3][3]);  */

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        Log.d(TAG, "time bro: " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        Log.d(TAG, "time bro2: " + formattedDate);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true); // why cancel notification?


    }

}
