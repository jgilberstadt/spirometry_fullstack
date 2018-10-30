package com.spirometry.homespirometry;

import android.bluetooth.BluetoothAdapter;
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

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.homespirometry.R;

import java.util.ArrayList;

public class SpirometerConnectingActivity extends AppCompatActivity{

    private NewParcelable mBundleData;
    String success = "Success!";
    private static final String TAG = SpirometerConnectingActivity.class.getSimpleName();
    DeviceManager deviceManager;
    Device currDevice;
    //ArrayList<String> arr;
    DeviceInfo selectedDeviceInfo;
    //ArrayList<String> infoList = new ArrayList<>();
    TextView tvConnecting;
    ProgressBar progressBar;
    TextView directionTextView;
    Button tryAgainButton;
    DeviceInfo discoveredDeviceInfo;
    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    Boolean localInfo = false;
    int numberOfDisconnect = 0;
    String fullInfo = "", result = "", infoDisconnect = "", strProgress ="";
    BluetoothAdapter bluetoothadapter;
    Intent bluetoothIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spirometer_connecting);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvConnecting = (TextView) findViewById(R.id.tvConnecting);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tryAgainButton = (Button) findViewById(R.id.tryAgainButton);
        directionTextView = (TextView) findViewById(R.id.directionTextView);

        //get device manager istance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just

        //arr = mBundleData.getDeviceInfo();

        DeviceInfo lastDeviceConnected = deviceManager.getLastConnectedDeviceInfo(getApplicationContext());
        deviceManager.connect(getApplicationContext(), lastDeviceConnected);


        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothadapter.isEnabled()) {
            bluetoothEnableRequest();
            return;
        }

        deviceManager.startDiscovery(SpirometerConnectingActivity.this);
        progressBar.setVisibility(View.VISIBLE);
        tvConnecting.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.INVISIBLE);
        handlerWait.post(runWait);

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    bluetoothEnableRequest();
                    return;
                }
               // deviceManager.disconnect();
                deviceManager.stopDiscovery();
                Log.d(TAG, "Start Discovery!33333");
                progressBar.setVisibility(View.VISIBLE);
                tvConnecting.setVisibility(View.VISIBLE);
                directionTextView.setVisibility(View.INVISIBLE);
                tryAgainButton.setVisibility(View.INVISIBLE);
                deviceManager.startDiscovery(SpirometerConnectingActivity.this);
                handlerWait.post(runWait);
            }
        });
    }

    public void bluetoothEnableRequest() {
        tvConnecting.setVisibility(View.INVISIBLE);
        directionTextView.setText(R.string.enable_bluetooth);
        directionTextView.setVisibility(View.VISIBLE);
        tryAgainButton.setText("Enable Bluetooth");
        tryAgainButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothIntent, 1);
                deviceManager.disconnect();
                Log.d(TAG, "Start Discovery!12222");
                progressBar.setVisibility(View.VISIBLE);
                tvConnecting.setVisibility(View.VISIBLE);
                directionTextView.setVisibility(View.INVISIBLE);
                tryAgainButton.setVisibility(View.INVISIBLE);
                deviceManager.startDiscovery(SpirometerConnectingActivity.this);
                handlerWait.post(runWait);
                tryAgainButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            bluetoothEnableRequest();
                            return;
                        }
                        deviceManager.disconnect();
                        Log.d(TAG, "Start Discovery!11111");
                        progressBar.setVisibility(View.VISIBLE);
                        tvConnecting.setVisibility(View.VISIBLE);
                        directionTextView.setVisibility(View.INVISIBLE);
                        tryAgainButton.setVisibility(View.INVISIBLE);
                        deviceManager.startDiscovery(SpirometerConnectingActivity.this);
                        handlerWait.post(runWait);
                    }
                });
            }
        });
    }

    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
            Log.d("hyunrae", deviceInfo.getSerialNumber());
            Log.d("hyunrae", deviceInfo.getAddress());
                    //I did this so that you don't reconnect with different device
                    /*if(deviceInfo.getAddress().matches("00:26:33:CD:28:EB") || deviceInfo.getAddress().matches("00:26:33:CD:28:F6")) {
                   //     localInfo = "success";
                        discoveredDeviceInfo = deviceInfo;
                        progressBar.setVisibility(View.VISIBLE);
                        tvConnecting.setVisibility(View.VISIBLE);
                        tryAgainButton.setVisibility(View.INVISIBLE);
                        handleUpdateListScan.post(runUpdateListScan);
                        Log.d(TAG, "Your Specific Device Connected");
                    }else{
                        Log.d(TAG, "Your Specific Device is not Connected");
                        progressBar.setVisibility(View.VISIBLE);
                        tvConnecting.setVisibility(View.VISIBLE);
                    }*/
            discoveredDeviceInfo = deviceInfo;
            progressBar.setVisibility(View.VISIBLE);
            tvConnecting.setVisibility(View.VISIBLE);
            tryAgainButton.setVisibility(View.INVISIBLE);
            handleUpdateListScan.post(runUpdateListScan);
            Log.d(TAG, "Your Specific Device Connected");
        }

        @Override
        public void deviceConnected(Device device) {
            localInfo = true;
            //     handleSuccess.post(runSuccess);
            currDevice = device;
            //handleUpdateInfo.post(runUpdateInfo);
            Log.d(TAG, "Device Connected");
 /*           if(localInfo.equals("success")) {
            }else{
                Log.d(TAG, "deviceConnected: "+ "Time was slow");
            } */
        }

        @Override
        public void deviceDisconnected(Device device) {
            currDevice = null;
            //deviceManager.startDiscovery(SpirometerConnectingActivity.this);
            Log.d(TAG, "deviceDisconnected");
            progressBar.setVisibility(View.INVISIBLE);
            tvConnecting.setVisibility(View.INVISIBLE);


        }
        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice=null;
            tryAgainButton.setVisibility(View.VISIBLE);
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
            deviceManager.stopDiscovery();
            Intent intent = new Intent(SpirometerConnectingActivity.this, SpirometerInstructionActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            SpirometerConnectingActivity.this.startActivity(intent);
            finish();
            // tvConnecting.setText();
         //   tvConnecting.setText(success);
        }
    };

    Handler handleSuccess = new Handler();
    Runnable runSuccess = new Runnable() {
        @Override
        public void run() {
           tvConnecting.setText(success);
       tvConnecting.setVisibility(View.VISIBLE);
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
           if(localInfo == true){
                Log.d(TAG, "the device is connect, nothing to show");
               handleUpdateInfo.post(runUpdateInfo);
           }else {
                localInfo = false;
                deviceManager.disconnect();
               DeviceInfo lastDeviceConnected = deviceManager.getLastConnectedDeviceInfo(getApplicationContext());
               deviceManager.connect(getApplicationContext(), lastDeviceConnected);
                tvConnecting.setVisibility(View.INVISIBLE);
                tryAgainButton.setText("RE-CONNECT");
                tryAgainButton.setVisibility(View.VISIBLE);
                directionTextView.setText(R.string.spirometer_not_connected);
                directionTextView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                if(numberOfDisconnect >=3){
                    directionTextView.setText("Contact Pulmonary Function Lab. Phone: 999-999-9999");
                    directionTextView.setTextColor(Color.parseColor("#0000FF"));
                    directionTextView.setVisibility(View.VISIBLE);
                }
                numberOfDisconnect++;
                Toast.makeText(getApplicationContext(), "Your Bluetooth Device is Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }, 5000);
        }
    };

    public void onClickHelp(View view) {
        Intent intent = new Intent(SpirometerConnectingActivity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

}
