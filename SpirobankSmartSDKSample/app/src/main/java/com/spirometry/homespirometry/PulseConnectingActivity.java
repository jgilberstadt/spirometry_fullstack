package com.spirometry.homespirometry;

/*
    This activity comes after PulseInstructionActivity and connects the application to the pulse oximeter.
    Once connected, the user will be taken to PulseActivity for measurement.
 */

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
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
import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.spirobanksmartsdk.DeviceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class PulseConnectingActivity extends AppCompatActivity{
    private NewParcelable mBundleData;
    private static final String TAG = PulseConnectingActivity.class.getSimpleName();

    private String deviceMac;

    DeviceManager deviceManager;

    TextView directionTV;
    Button retryButton;
    ProgressBar progressBar;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

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
        if (mBundleData == null) {
            mBundleData = new NewParcelable();
        }

        // Initialize the device manager
        iHealthDevicesManager.getInstance().init(this, Log.VERBOSE, Log.VERBOSE);

        // Register callback. See below
        iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);


        try {
            // Get the key in the assets folder that allows us to use the iHealth SDK. When given a new key, you must upload it to the assets folder and update below accordingly.
            InputStream is = getAssets().open("com_spirometry_homespirometry_android.pem");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // authenticate with the key
            boolean isPass = iHealthDevicesManager.getInstance().sdkAuthWithLicense(buffer);
            Log.i(TAG, "isPass:    " + isPass);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // Start discovery for device. PO3 is the device type of a pulse oximeter.
                    iHealthDevicesManager.getInstance().startDiscovery(DiscoveryTypeEnum.PO3);
                    Log.i(TAG, "Request Permission Pass");
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    Log.i(TAG, "Request Permission Fail");
                }
            }
        }
    }

    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        // Once we get a mac from scanning, then try conecting to that device.
        public void onScanDevice(String mac, String deviceType, int rssi, Map manufactorData) {
            Log.i(TAG, "onScanDevice - mac:" + mac + " - deviceType:" + deviceType + " - rssi:" + rssi + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            if (manufactorData != null) {
            }
            deviceMac = mac;
            Boolean success = iHealthDevicesManager.getInstance().connectDevice("test", mac, deviceType);
            if (!success) {
                //Toast.makeText(PulseConnectingActivity.this, "Haven’t permission to connect this device or the mac is not valid", Toast.LENGTH_LONG).show();

            }else { //if(success)
                Log.d(TAG, "onScanDevice: " + "Scanned Device Successfully");
            }

            Log.d(TAG, Boolean.toString(success));
        }

        @Override
        // Callback for when the device is detected to connect or disconnect
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactorData) {
            Log.e(TAG, "mac:" + mac + " deviceType:" + deviceType + " status:" + status + " errorid:" + errorID + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            msg.setData(bundle);

            // Once connected, take it to PulseActivity and pass the deviceMac which will help us identify which device we are referencing in the next activity
            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                Log.d(TAG, "onDeviceConnectionStateChange: " + "Connected to device successfully");
                Intent intent = new Intent(getApplicationContext(), PulseActivity.class);
                intent.putExtra("bundle-data", mBundleData);
                intent.putExtra("mac", deviceMac);
                startActivity(intent);

            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                Log.d(TAG, "onDeviceConnectionStateChange: " + "Device Disconnected");
            }
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
            // not needed
        }

        @Override
        // If this callback is reached, then it means we have not found any pulse oximeter.
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

    // Linked to Connect button so that when pressed, it scans for the device once again.
    public void onClickConnect(View view){
        //iHealthDevicesManager.getInstance().startDiscovery();
        //  iHealthDevicesManager.getInstance().startDiscovery(1000);
        //iHealthDevicesManager.getInstance().stopDiscovery();
        iHealthDevicesManager.getInstance().startDiscovery(DiscoveryTypeEnum.PO3);
        Log.d(TAG, "onClickConnect " + "Connecting ....");

        progressBar.setVisibility(View.VISIBLE);
        directionTV.setText(R.string.search_for_pulse);
        retryButton.setVisibility(View.GONE);
    }
}