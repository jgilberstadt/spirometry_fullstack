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

import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import java.util.ArrayList;

public class SpirometerConnectingActivity extends AppCompatActivity{

    private MyParcelable mBundleData;
    private static final String TAG = SpirometerConnectingActivity.class.getSimpleName();
    DeviceManager deviceManager;
    Device currDevice;
    ArrayList<String> arr;
    DeviceInfo selectedDeviceInfo;
    //ArrayList<String> infoList = new ArrayList<>();
    TextView tvConnecting;
    ProgressBar progressBar;
    TextView bluetoothNotConnected;
    TextView directionTextView;
    Button tryAgainButton;
    DeviceInfo discoveredDeviceInfo;
    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    String localInfo = "";
    int numberOfDisconnect = 0;
    String fullInfo = "", result = "", infoDisconnect = "", strProgress ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

    /*    new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 4000); */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spirometer_connecting);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvConnecting = (TextView) findViewById(R.id.tvConnecting);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bluetoothNotConnected = (TextView) findViewById(R.id.bluetoothNotConnected);
        tryAgainButton = (Button) findViewById(R.id.tryAgainButton);
        directionTextView = (TextView) findViewById(R.id.directionTextView);

        //get device manager istance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just

        arr = mBundleData.getDeviceInfo();

        if(arr != null && !arr.isEmpty()) {
             selectedDeviceInfo = new DeviceInfo(arr.get(0), arr.get(1), arr.get(2), arr.get(3), arr.get(4));
             Log.d("deviceInfo", "Device Info1: " + selectedDeviceInfo.toString());
             progressBar.setVisibility(View.VISIBLE);
             tvConnecting.setVisibility(View.VISIBLE);
             bluetoothNotConnected.setVisibility(View.INVISIBLE);
             tryAgainButton.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    deviceManager.connect(getApplicationContext(), selectedDeviceInfo);
                }
            }, 3000);
            }else{
             Log.d(TAG,"There is no device info sent from the loginActivity, so it is an empty array");
             progressBar.setVisibility(View.INVISIBLE);
             tvConnecting.setVisibility(View.INVISIBLE);
             bluetoothNotConnected.setVisibility(View.VISIBLE);
             tryAgainButton.setVisibility(View.VISIBLE);
             Toast.makeText(getApplicationContext(), "Your Bluetooth Device is Not Connected", Toast.LENGTH_SHORT).show();
         }

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//버튼 클릭했을떄 동작하는 코드를 여기에 넣는다.
                deviceManager.disconnect();
                Log.d(TAG, "Start Discovery!");
                deviceManager.startDiscovery(SpirometerConnectingActivity.this);
                progressBar.setVisibility(View.VISIBLE);
                tvConnecting.setVisibility(View.VISIBLE);
                bluetoothNotConnected.setVisibility(View.INVISIBLE);
                directionTextView.setVisibility(View.INVISIBLE);
                tryAgainButton.setVisibility(View.INVISIBLE);
                handlerWait.post(runWait);
            }
        });
    }

    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
            Log.d(TAG, "Some sort of device connected");
                    //I did this so that you don't reconnect with different device.
                    if(deviceInfo.getAddress().matches("00:26:33:CD:28:EB")) {
                        discoveredDeviceInfo = deviceInfo;
                        handleUpdateListScan.post(runUpdateListScan);
                        Log.d(TAG, "Your Specific Device Connected");
                        String success = "Success!";
                        tvConnecting.setText(success);
                        progressBar.setVisibility(View.VISIBLE);
                        tvConnecting.setVisibility(View.VISIBLE);
                        bluetoothNotConnected.setVisibility(View.INVISIBLE);
                        tryAgainButton.setVisibility(View.INVISIBLE);

                    }
                    else{
                        bluetoothNotConnected.setVisibility(View.VISIBLE);
                        tryAgainButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        tvConnecting.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "Connection Worked, but this is not your spiro device" + deviceInfo.getAdvertisementDataName());
                        Log.d(TAG, "deviceDiscovered: " + deviceInfo.getAddress());
                     //   Toast.makeText(getApplicationContext(), "Connection Worked, but this is not your spiro device", Toast.LENGTH_LONG).show();
                    }
                }


        @Override
        public void deviceConnected(Device device) {
            currDevice = device;
            localInfo = "connected";
            handleUpdateInfo.post(runUpdateInfo);
            Log.d(TAG, "Device Connected");
            //String finish = "Success";
            //tvConnecting.setText(finish); // this was a bug I couldn't find, I still don't know about this logic

            //infoList.add("devConnected");
            //currDevice.setDeviceCallback(deviceCallback);
        }
        @Override
        public void deviceDisconnected(Device device) {
            currDevice = null;
            deviceManager.startDiscovery(SpirometerConnectingActivity.this);
            Log.d(TAG, "deviceDisconnected");
            progressBar.setVisibility(View.INVISIBLE);
            tvConnecting.setVisibility(View.INVISIBLE);
            tvConnecting.setVisibility(View.VISIBLE);
            bluetoothNotConnected.setVisibility(View.VISIBLE);


        }
        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice=null;
            Log.d(TAG, "did it not work?: we found the address for your device, but it can't connect" + currDevice);
            tryAgainButton.setVisibility(View.VISIBLE);
            bluetoothNotConnected.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            tvConnecting.setVisibility(View.INVISIBLE);


        }
        @Override
        public void bluetoothLowEnergieIsNotSupported() {
        }
        @Override
        public void bluetoothIsPoweredOFF() {
            Log.d(TAG, "Please ReConnect to the Bluetooth");

        }
        @Override
        public void accessCoarseLocationPermissionRequired(){
            //Android M runtime authorizzation
            infoDisconnect = "Access Coarse Location Permission Required";
            //handleUpdateInfo.post(runUpdateInfo);

            //For this request you need to implement callback
            deviceManager.requestCoarseLocationPermission(SpirometerConnectingActivity.this,PERMISSION_REQUEST_COARSE_LOCATION);
        }
    };

    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
            //handleUpdateInfo.post(runUpdateInfo);
        } //+++
    };

    Handler handleUpdateInfo = new Handler();
    Runnable runUpdateInfo = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(SpirometerConnectingActivity.this, BlowActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            SpirometerConnectingActivity.this.startActivity(intent);
            finish();
            // tvConnecting.setText();
         //   tvConnecting.setText(success);
        }
    };
    Handler handlerWait = new Handler();
    Runnable runWait = new Runnable() {
        @Override
        public void run() {
    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "no connection");
            deviceManager.stopDiscovery();
            if(localInfo.equals("connected")){
                Log.d(TAG, "the device is connect, nothing to show");
                handleUpdateInfo.post(runUpdateInfo);
            }else {
                progressBar.setVisibility(View.INVISIBLE);
                tvConnecting.setVisibility(View.INVISIBLE);
                bluetoothNotConnected.setVisibility(View.VISIBLE);
                tryAgainButton.setVisibility(View.VISIBLE);
                directionTextView.setVisibility(View.VISIBLE);
                if(numberOfDisconnect >=3){
                    directionTextView.setText("Contact Pulmonary Function Lab. Phone: 999-999-9999");
                    directionTextView.setTextColor(Color.parseColor("#0000FF"));

                    directionTextView.setVisibility(View.VISIBLE);
                }
                numberOfDisconnect++;
                Toast.makeText(getApplicationContext(), "Your Bluetooth Device is Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }, 9000);
        }
    };


}
