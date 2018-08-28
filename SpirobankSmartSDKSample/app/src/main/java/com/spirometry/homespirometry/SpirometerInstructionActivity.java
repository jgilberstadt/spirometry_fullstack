package com.spirometry.homespirometry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;

import java.util.ArrayList;

public class SpirometerInstructionActivity extends AppCompatActivity {

    private static final String TAG = SpirometerInstructionActivity.class.getSimpleName();

    //This is a MyParcelable object that contains data / objects to be passed between activities
    private MyParcelable mBundleData;

    DeviceManager deviceManager;
    DeviceInfo discoveredDeviceInfo;
    Device currDevice;

    Button startTest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spirometer_instruction);

        startTest2 = (Button) findViewById(R.id.startTest2);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //get device manager instance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);

        //currDevice is already connected device from the previous Intent
        currDevice = deviceManager.getDeviceConnected();

        startTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpirometerInstructionActivity.this, BlowActivity.class);
                intent.putExtra("peripheralAddress", getIntent().getStringExtra("peripheralAddress"));
                intent.putExtra("bundle-data", mBundleData);
                SpirometerInstructionActivity.this.startActivity(intent);
                finish();
            }
        });
    }

    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
            Log.d(TAG, "Some sort of device connected");
            if (deviceInfo.getAddress().matches("00:26:33:CD:28:EB")) {
                discoveredDeviceInfo = deviceInfo;
                Log.d(TAG, "Your Specific Device Connected");
            } else {
                Log.d(TAG, "Your Specific Device is not Connected");
            }
        }

        @Override
        public void deviceConnected(Device device) {
            currDevice = device;
            Log.d(TAG, "Device Connected");
        }

        @Override
        public void deviceDisconnected(Device device) {
            currDevice = null;
            finish();
            //deviceManager.startDiscovery(SpirometerConnectingActivity.this);

        }

        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice = null;
        }

        @Override
        public void bluetoothLowEnergieIsNotSupported() {
        }

        @Override
        public void bluetoothIsPoweredOFF() {
            Log.d(TAG, "Please ReConnect to the Bluetooth");

        }

        @Override
        public void accessCoarseLocationPermissionRequired() {
        }
    };
}

