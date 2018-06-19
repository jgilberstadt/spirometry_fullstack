package com.spirometry.spirobanksmartsdksample;

import android.content.Intent;
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

public class ConnectingActivity extends AppCompatActivity{

    private MyParcelable mBundleData;
    private static final String TAG = LoginActivity.class.getSimpleName();
    DeviceManager deviceManager;
    Device currDevice;
    ArrayList<String> arr;
    DeviceInfo selectedDeviceInfo;
    //ArrayList<String> infoList = new ArrayList<>();
    TextView tvConnecting;
    ProgressBar progressBar;
    TextView bluetoothNotConnected;
    Button tryAgainButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

    /*    new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 4000); */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvConnecting = (TextView) findViewById(R.id.tvConnecting);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bluetoothNotConnected = (TextView) findViewById(R.id.bluetoothNotConnected);
        tryAgainButton = (Button) findViewById(R.id.tryAgainButton);

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
             Handler handler = new Handler();
             handler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
             deviceManager.connect(getApplicationContext(), selectedDeviceInfo);
           }
         }, 3000);
            }else{
             Log.d(TAG,"There is no device info sent from the loginActivity, so it is an empty array");
              tvConnecting.setVisibility(View.INVISIBLE);
             bluetoothNotConnected.setVisibility(View.VISIBLE);
             tryAgainButton.setVisibility(View.VISIBLE);
             Toast.makeText(getApplicationContext(), "Your Bluetooth Device is Not Connected", Toast.LENGTH_SHORT).show();

         }

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//버튼 클릭했을떄 동작하는 코드를 여기에 넣는다.
                deviceManager.startDiscovery(ConnectingActivity.this);
                progressBar.setVisibility(View.VISIBLE);
                tvConnecting.setVisibility(View.VISIBLE);
                bluetoothNotConnected.setVisibility(View.INVISIBLE);
                tryAgainButton.setVisibility(View.INVISIBLE);

                Log.d(TAG, "Start Discovery!");
                //wait(100);
            }
        });
    }

    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(final DeviceInfo deviceInfo) {
            Log.d(TAG, deviceInfo.getAddress());
                    //I did this so that you don't reconnect with different device.
                    if(deviceInfo.getAddress().matches("00:26:33:CD:28:F6")) {
                        Log.d(TAG, "Some sort of device is discovered");
                        progressBar.setVisibility(View.INVISIBLE);
                        String success = "Success!";
                        tvConnecting.setText(success);
                        tvConnecting.setVisibility(View.VISIBLE);
                        bluetoothNotConnected.setVisibility(View.INVISIBLE);
                        tryAgainButton.setVisibility(View.INVISIBLE);
                        deviceManager.connect(getApplicationContext(), deviceInfo);
                    }
                    else{
                        Log.d(TAG, "Device Not Found: " + deviceInfo.getAdvertisementDataName());
                        progressBar.setVisibility(View.INVISIBLE);
                        tvConnecting.setVisibility(View.INVISIBLE);
                        bluetoothNotConnected.setVisibility(View.VISIBLE);
                        tryAgainButton.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "Your Bluetooth Device is STILL Not Connected", Toast.LENGTH_LONG).show();
                    }
                }


        @Override
        public void deviceConnected(Device device) {
            currDevice = device;
            String success = "Success";
            tvConnecting.setText(success);
            progressBar.setVisibility(View.GONE);
            tvConnecting.setVisibility(View.GONE);
            Log.d(TAG, "Device Connected");
            handleUpdateInfo.post(runUpdateInfo);
            //infoList.add("devConnected");
            //currDevice.setDeviceCallback(deviceCallback);
        }
        @Override
        public void deviceDisconnected(Device device) {
            Log.d(TAG, "deviceDisconnected");
        }
        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice=null;
            Log.d(TAG, "did it not work?: ");

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
        }
    };



    Handler handleUpdateInfo = new Handler();
    Runnable runUpdateInfo = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(ConnectingActivity.this, TestingPageOne.class);
            ConnectingActivity.this.startActivity(intent);
           // tvConnecting.setText();
         //   tvConnecting.setText(success);
        }
    };


}
