package com.spirometry.homespirometry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ihealth.communication.control.Po3Control;
import com.ihealth.communication.control.PoProfile;
import com.ihealth.communication.manager.DiscoveryTypeEnum;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.communication.manager.iHealthDevicesUpgradeManager;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.BuildConfig;
import com.spirometry.homespirometry.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class PulseConnectingActivity extends AppCompatActivity{
    MyParcelable mBundleData;

    private String deviceMac;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_connecting);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        iHealthDevicesManager.getInstance().init(this, Log.VERBOSE, Log.ASSERT);

        iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);


        try {
            InputStream is = getAssets().open("com_spirometry_homespirometry_android.pem");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            boolean isPass = iHealthDevicesManager.getInstance().sdkAuthWithLicense(buffer);
            Log.i("hyunrae", "isPass:    " + isPass);
            iHealthDevicesManager.getInstance().startDiscovery(DiscoveryTypeEnum.PO3);
        } catch (IOException e) {
            Log.d("hyunrae", e.toString());
            e.printStackTrace();
        }
    }


    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onScanDevice(String mac, String deviceType, int rssi, Map manufactorData) {
            Log.i("hyunrae", "onScanDevice - mac:" + mac + " - deviceType:" + deviceType + " - rssi:" + rssi + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            if (manufactorData != null) {
               // Log.d("hyunrae", "onScanDevice mac suffix = " + manufactorData.get(HsProfile.SCALE_WIFI_MAC_SUFFIX));
            }
            deviceMac = mac;
            Boolean success = iHealthDevicesManager.getInstance().connectDevice("test", mac, deviceType);
            if (success) {
                Intent intent = new Intent(PulseConnectingActivity.this, PulseActivity.class);
                intent.putExtra("bundle-data", mBundleData);
                intent.putExtra("mac", deviceMac);
                startActivity(intent);
            }

            Log.d("hyunrae2", Boolean.toString(success));
        }

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactorData) {
            Log.e("hyunrae", "mac:" + mac + " deviceType:" + deviceType + " status:" + status + " errorid:" + errorID + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
            }
            msg.setData(bundle);
        }

        @Override
        public void onUserStatus(String username, int userStatus) {
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userstatus", userStatus + "");
            Message msg = new Message();
            msg.setData(bundle);
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            Log.d("hyunrae", "ondevicenotify");
        }

        @Override
        public void onScanFinish() {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            Log.d("hyunrae", "onScanFinish");
        }

        @Override
        public void onScanError(String reason, long latency) {
            super.onScanError(reason, latency);
        }
    };


    public void onClickConnect(View view){
        iHealthDevicesManager.getInstance().startDiscovery(1000);
        this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }
}
