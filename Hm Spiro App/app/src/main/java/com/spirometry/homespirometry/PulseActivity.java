package com.spirometry.homespirometry;

/*

This activity comes after PulseConnectinActivity after the pulse oximeter has been connected.
This activity uses the iHealthSDK to receive data from the device and save data to the bundle.
Once we get 60 seconds of the pulse data, we move onto QuestionnaireInstructionActivity.

 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ihealth.communication.control.Po3Control;
import com.ihealth.communication.control.PoProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.R;
import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.homespirometry.classes.SuperActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DeflaterOutputStream;


public class PulseActivity extends SuperActivity {

    private static final String TAG = PulseActivity.class.getSimpleName();

    NewParcelable mBundleData;
    private int clientId;
    private Po3Control mPo3Control;
    private String deviceMac;

    TextView pulseNumber;
    TextView countDown;
    TextView secondsRemaining;
    boolean startTest = false;

    CountDownTimer myCountDownTimer;

    //LinkedList pulseData = new LinkedList<String[]>();
    String pulseData = "";
    private int minHeartRate = Integer.MAX_VALUE;
    private int maxHeartRate = Integer.MIN_VALUE;
    private int lowestSat = Integer.MAX_VALUE;
    private int timeAbnormal = 0;
    private int timeMinRate = 0;
    private int normOxygen = 90;
    private int timeLeft = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_pulse);
        super.onCreate(savedInstanceState);
        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");
        if (mBundleData == null) {
            mBundleData = new NewParcelable();
        }
        deviceMac = getIntent().getStringExtra("mac");
        //deviceMac = "94E36D555D31";

        // See below for the callback definition
        clientId = iHealthDevicesManager.getInstance().registerClientCallback(mIHealthDeviceCallback);

        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(clientId,
                iHealthDevicesManager.TYPE_PO3);

        pulseNumber = (TextView) findViewById(R.id.pulseNumber);
        countDown = (TextView) findViewById(R.id.countDown);
        secondsRemaining = (TextView) findViewById(R.id.secondsRemaining);

        Log.d("mac addr", deviceMac);

        // Use the deviceMac from the last activity and get the device
        mPo3Control = iHealthDevicesManager.getInstance().getPo3Control(deviceMac);

        mPo3Control.getHistoryData();
        mPo3Control.startMeasure();

    }


    iHealthDevicesCallback mIHealthDeviceCallback = new iHealthDevicesCallback() {

        //Callback for connecting and disconnecting. NEED to handle the disconnect so that it returns to the previous activity and connects again.
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID) {
            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                Log.d(TAG, "device is connected");
                mPo3Control = iHealthDevicesManager.getInstance().getPo3Control(deviceMac);
                Log.d(TAG, "deviceMac:" + deviceMac + "--mPo3Control:" + mPo3Control);
                //mPo3Control.startMeasure();

            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                Log.d(TAG, "disconnected");
            }
        }

        public void onDeviceNotify(String mac, String deviceType, String action, String message) {

            JSONTokener jsonTokener = new JSONTokener(message);

            switch (action) {
                case PoProfile.ACTION_OFFLINEDATA_PO:
//                    try {
//                        JSONObject object = (JSONObject) jsonTokener.nextValue();
//                        JSONArray jsonArray = object.getJSONArray(PoProfile.OFFLINEDATA_PO);
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
//                            String dataId = jsonObject.getString(PoProfile.DATAID);
//                            String dateString = jsonObject.getString(PoProfile.MEASURE_DATE_PO);
//                            int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
//                            int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
//                            JSONArray jsonArray1 = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
//                            int[] wave = new int[jsonArray1.length()];
//                            for (int j = 0; j < jsonArray1.length(); j++) {
//                                wave[j] = jsonArray1.getInt(j);
//                            }
//                            Log.i(TAG, "dataId:" + dataId + "--date:" + dateString + "--oxygen:" + oxygen + "--pulseRate:" + pulseRate
//                                    + "-wave1:"
//                                    + wave[0]
//                                    + "-wave2:" + wave[1] + "--wave3:" + wave[2]);
//
//                        }
//
//                    } catch (JSONException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
                    break;
                // This is only important case because we're getting live data
                case PoProfile.ACTION_LIVEDA_PO:
                    try { // this one only tells on LOG.I, so not really important
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                        // All the data we get from the device
                        int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                        int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                        float PI = (float) jsonObject.getDouble(PoProfile.PI_PO);
                        int pulseStrength = jsonObject.getInt(PoProfile.PULSE_STRENGTH_PO);
                        JSONArray jsonArray = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                        int[] wave = new int[3];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            wave[i] = jsonArray.getInt(i);
                        }

                        pulseNumber.setTextSize(45);
                        Message wow = new Message();
                        wow.what = 1;

                        Message dataMsg = new Message();
                        dataMsg.what = 1;

                        // For displaying the heartrate and spo2 to the patient. NOTE: We might not want to display this to the patient
                        // This is due to the fact that doctors do not want to show too much data to the patient
                        String stOxygen = Integer.toString(oxygen);
                        String stPulseRate = Integer.toString(pulseRate);
                        dataMsg.obj = ("spO2%: " + stOxygen + "      PR bpm: " + stPulseRate);
                        mHandler.sendMessage(dataMsg);

                        // This is for generating a string to with each piece of the data and commas as seperators
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < wave.length; i++) {
                            builder.append(wave[i]);
                            if (i != wave.length - 1) {
                                builder.append(",");
                            }
                        }
                        String waveString = builder.toString();

                        String pulseRateString = "";
                        if (pulseRate < 100) {
                            pulseRateString = "0" + pulseRate;
                        }

                        String pulseStrengthString = "";
                        if (pulseStrength < 10) {
                            pulseStrengthString = "0" + pulseStrength;
                        }

                        //String[] dataArr = {pulseRateString, Integer.toString(oxygen), pulseStrengthString, Float.toString(PI), waveString};
                        String dataArr = pulseRateString + " " + Integer.toString(oxygen) + " " + pulseStrengthString + " " + Float.toString(PI) + " " + waveString + "\n";

                        // Different pieces of data that we want to store separately and need to maintain
                        if (oxygen < lowestSat && oxygen > 70) {
                            lowestSat = oxygen;
                            timeMinRate = 1;
                        } else if (oxygen == lowestSat) {
                            timeMinRate++;
                        }

                        if (pulseRate > maxHeartRate) {
                            maxHeartRate = pulseRate;
                        }
                        if (pulseRate < minHeartRate) {
                            minHeartRate = pulseRate;
                        }

                        // <90 for longer consecutive 20s considered as abnormal
                        if (oxygen < normOxygen) {
                            timeAbnormal++;
                        }

                        //pulseData.add(dataArr);
                        pulseData += dataArr;

                        // For making sure the test lasts 60 seconds
                        if (startTest == false) {
                            startTest = true;

                            myCountDownTimer = new CountDownTimer(timeLeft*1000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    int countDown = (int) (millisUntilFinished / 1000);
                                    secondsRemaining.setText(String.valueOf(countDown));
                                }

                                public void onFinish() {
                                    countDown.setText("Finished");
                                    secondsRemaining.setVisibility(View.GONE);
                                    mBundleData.setPulseData(pulseData);
                                    mBundleData.setMinHeartrate(minHeartRate);
                                    mBundleData.setMaxHeartrate(maxHeartRate);
                                    mBundleData.setLowestSat(lowestSat);
                                    mBundleData.setTimeAbnormal(timeAbnormal);
                                    mBundleData.setTimeMinRate(timeMinRate);

                                    iHealthDevicesManager.getInstance().destroy();

                                    Intent intent = new Intent(PulseActivity.this, SpirometerConnectingActivity.class); //PulseConnectingActivity
                                    //  Intent intent = new Intent(BlowActivity.this, PulseConnectingActivity.class);
                                    intent.putExtra("bundle-data", mBundleData);
                                    startActivity(intent);

                                    // We want to go to questionnaire if abnormal or by random. We want the randomizer to also be random.

                                    // if out of normal range or random

                                    /*

                                    Random r = new Random();
                                    int subRandom = r.nextInt(5);
                                    boolean var1 = (getMaxFev1(mBundleData.getBlowDataArray()) < mBundleData.getMinNRange()) || (getMaxFev1(mBundleData.getBlowDataArray()) > mBundleData.getMaxNRange());

                                    mBundleData.setVarianceExists(var1?1:0);

                                    //if fev is anomalous, or if random questionnaire is assigned
                                    if (subRandom == 5 || mBundleData.getVarianceExists()==1) {

                                        Intent intent = new Intent(PulseActivity.this, QuestionnaireInstructionActivity.class);
                                        intent.putExtra("bundle-data", mBundleData);
                                        intent.putExtra("mac", deviceMac);
                                        startActivity(intent);
                                        finish();
                                        //sample
                                    }else{
                                        Intent intent = new Intent(PulseActivity.this, TestCompleteActivity.class);
                                        intent.putExtra("bundle-data", mBundleData);
                                        intent.putExtra("mac", deviceMac);
                                        startActivity(intent);
                                    }
                                    */
                                }
                            }.start();
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_RESULTDATA_PO:
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                        myCountDownTimer.cancel();
                        if (!(PulseActivity.this).isFinishing()) {
                            //show dialog
                            AlertDialog.Builder alert = new AlertDialog.Builder(PulseActivity.this);

                            TextView title = new TextView(getApplicationContext());
                            title.setHeight(80);
                            int darkBlue = Color.parseColor("#000080");
                            title.setBackgroundColor(darkBlue);
                            title.setText("Notification");
                            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                            title.setTextColor(Color.WHITE);
                            title.setGravity(Gravity.CENTER);
                            alert.setCustomTitle(title);

                            alert.setMessage("You need to measure your pulse oximeter for 60 seconds. You will need to retry.");
                            alert.setPositiveButton("OK", null);
                            final AlertDialog dialog = alert.create();
                            dialog.show();
                            TextView messageSize = (TextView) dialog.findViewById(android.R.id.message);
                            messageSize.setTextSize(30);

                            final Timer ticking = new Timer();
                            ticking.schedule(new TimerTask() {
                                public void run() {
                                    dialog.dismiss(); // when the task active then close the dialog
                                    ticking.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                                }
                            }, 6000); // after 2 second (or 2000 miliseconds), the task will be active.
                        }

                        startTest = false;
                        secondsRemaining.setText("60");


                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;

                case PoProfile.ACTION_NO_OFFLINEDATA_PO:
                    //noticeString = "no history data";
                    noticeString = "N/A";
                    Message message2 = new Message();
                    message2.what = 1;
                    message2.obj = noticeString;
                    mHandler.sendMessage(message2);
                    break;

                case PoProfile.ACTION_BATTERY_PO:
                    JSONObject jsonobject;
                    try {
                        jsonobject = (JSONObject) jsonTokener.nextValue();
                        int battery = jsonobject.getInt(PoProfile.BATTERY_PO);
                        Log.d(TAG, "battery:" + battery);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Message message3 = new Message();
                    message3.what = 1;
                    message3.obj = message;
                    mHandler.sendMessage(message3);
                    break;
                default:
                    break;
            }
        }
    };

    String noticeString = "";
    //handler for displaying message
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    pulseNumber.setText((String) msg.obj);
                    break;

                case 2:
                    pulseNumber.setText(msg.arg1);
                    break;

                default:
                    break;
            }
        }

        ;
    };

    public float getMaxFev1(String blowDataArray) {
        float max = 0;

        String [] single_blows = blowDataArray.split("\n");
        for(String sb:single_blows) {
            float fev1 = Float.parseFloat(sb.split(" ")[1]);
            if(fev1 > max) { max = fev1; }
        }

        return max;

    }

}



