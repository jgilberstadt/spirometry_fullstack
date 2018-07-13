package com.spirometry.homespirometry;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.ihealth.communication.control.Po3Control;
import com.ihealth.communication.control.PoProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Random;


public class PulseActivity extends AppCompatActivity {

    MyParcelable mBundleData;
    private int clientId;
    private Po3Control mPo3Control;
    private String deviceMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        deviceMac = getIntent().getStringExtra("mac");

        clientId = iHealthDevicesManager.getInstance().registerClientCallback(mIHealthDeviceCallback);

        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(clientId,
                iHealthDevicesManager.TYPE_PO3);

        mPo3Control = iHealthDevicesManager.getInstance().getPo3Control(deviceMac);
        Log.d("hyunrae", "deviceMac:" + deviceMac + "--mPo3Control:" + mPo3Control);

        Random r = new Random();
        int subRandom = r.nextInt(5);
        int finalRandom = r.nextInt(subRandom);

        Boolean administerSurvey = finalRandom == 0;

        if (administerSurvey) {
            // administer survey

        } else {
            // skip survey

        }


    }

    iHealthDevicesCallback mIHealthDeviceCallback = new iHealthDevicesCallback() {

        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID) {
            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                Log.d("hyunrae", "connected");
                mPo3Control = iHealthDevicesManager.getInstance().getPo3Control(deviceMac);
                Log.d("hyunrae", "deviceMac:" + deviceMac + "--mPo3Control:" + mPo3Control);
                mPo3Control.startMeasure();

            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                Log.d("hyunrae", "disconnected");
            }
        }

        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            JSONTokener jsonTokener = new JSONTokener(message);
            Log.d("hyunrae", message);
            try {
                JSONObject object = (JSONObject) jsonTokener.nextValue();
                JSONArray jsonArray = object.getJSONArray(PoProfile.OFFLINEDATA_PO);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    String dataId = jsonObject.getString(PoProfile.DATAID);
                    String dateString = jsonObject.getString(PoProfile.MEASURE_DATE_PO);
                    int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                    int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                    JSONArray jsonArray1 = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                    int[] wave = new int[jsonArray1.length()];
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        wave[j] = jsonArray1.getInt(j);
                    }
                    Log.i("hyunrae", "dataId:" + dataId + "--date:" + dateString + "--oxygen:" + oxygen + "--pulseRate:" + pulseRate
                            + "-wave1:"
                            + wave[0]
                            + "-wave2:" + wave[1] + "--wave3:" + wave[2]);
                }
                Message message2 = new Message();
                message2.what = 1;
                message2.obj = message;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    };

}

