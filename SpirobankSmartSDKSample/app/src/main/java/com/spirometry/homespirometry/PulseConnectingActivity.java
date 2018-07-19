package com.spirometry.homespirometry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.spirometry.spirobanksmartsdk.DeviceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class PulseConnectingActivity extends AppCompatActivity{
    MyParcelable mBundleData;
    private static final String TAG = PulseConnectingActivity.class.getSimpleName();

    private String deviceMac;

    DeviceManager deviceManager;

    TextView directionTV;
    Button retryButton;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_connecting);

        //deviceManager.disconnect();
        directionTV = (TextView) findViewById(R.id.directionTextView);
        retryButton  = (Button) findViewById(R.id.retryButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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
            if (!success) {
                Toast.makeText(PulseConnectingActivity.this, "Haven’t permission to connect this device or the mac is not valid", Toast.LENGTH_LONG).show();

            }else { //if(success)
                Log.d(TAG, "onScanDevice: " + "Bro 3.3 Scanned Device Successfully");
            //    Intent intent = new Intent(PulseConnectingActivity.this, PulseActivity.class);
            //    intent.putExtra("bundle-data", mBundleData);
            //    intent.putExtra("mac", deviceMac);
            //    startActivity(intent);
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
            msg.setData(bundle);

            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                Log.d(TAG, "onDeviceConnectionStateChange: " + "Bro 3.4 Connected to device successfully");
                Intent intent = new Intent(PulseConnectingActivity.this, PO3.class);
                intent.putExtra("bundle-data", mBundleData);
                intent.putExtra("mac", deviceMac);
                startActivity(intent);

            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                Log.d(TAG, "onDeviceConnectionStateChange: " + "Bro the Device Is Disconnected");
            }
        }

        @Override
        public void onUserStatus(String username, int userStatus) {
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userstatus", userStatus + "");
            Message msg = new Message();
            msg.setData(bundle);
            Log.d(TAG, "onUserStatus: " + "Bro2");
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            // not needed
        }

        @Override
        public void onScanFinish() {
            progressBar.setVisibility(View.GONE);
            directionTV.setText(R.string.pulse_not_found);
            retryButton.setVisibility(View.VISIBLE);
            Log.d(TAG, "onScanFinish: "  + "Bro3");
        }

        @Override
        public void onScanError(String reason, long latency) {
            super.onScanError(reason, latency);
        }
    };

    public void onClickConnect(View view){
        //iHealthDevicesManager.getInstance().startDiscovery();
      //  iHealthDevicesManager.getInstance().startDiscovery(1000);
        iHealthDevicesManager.getInstance().stopDiscovery();
       iHealthDevicesManager.getInstance().startDiscovery(DiscoveryTypeEnum.PO3);
        Log.d(TAG, "onClickConnect " + "Connecting ....");

        progressBar.setVisibility(View.VISIBLE);
        directionTV.setText(R.string.search_for_pulse);
        retryButton.setVisibility(View.GONE);
    }
}
