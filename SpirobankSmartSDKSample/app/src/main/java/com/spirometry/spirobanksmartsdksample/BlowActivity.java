package com.spirometry.spirobanksmartsdksample;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceCallback;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdk.ResultsFvc;
import com.spirometry.spirobanksmartsdk.ResultsPefFev1;
import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class BlowActivity extends AppCompatActivity {

    private static final String TAG = BlowActivity.class.getSimpleName();

    DeviceManager deviceManager;
    TextView blowDirection;
    TextView blowMessage;
    TextView numberCount;

    private MyParcelable mBundleData;

    Context myContext;
    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    String deviceInfoStringAddress;
    String deviceInfoStringName;
    String deviceInfoStringProtocol;
    String deviceInfoStringSerialNumber;
    String deviceInfoStringAdvertisementDataName;
    ArrayList<String> deviceInfoArray = new ArrayList<>();

    ArrayList<String> infoList = new ArrayList<>();
    Device currDevice;
    String infoDisconnect;

    DeviceInfo discoveredDeviceInfo;

    private int numBlows = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("HYUNRAE", "running oncreate");

        mBundleData = new MyParcelable();
        
        blowDirection = (TextView) findViewById(R.id.blowDirection);
        blowMessage = (TextView) findViewById(R.id.blowMessage);
        numberCount = (TextView) findViewById(R.id.numberCount);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blow);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //get device manager instance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);

        deviceManager.startDiscovery(BlowActivity.this);

    }


    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
            Log.d("HYUNRAE", "running");

            Log.d(TAG, "deviceDiscovered: " + deviceInfo.getAddress());
            //peter: this is a hardCode. I was first told to focus on connecting only one device using whatever I want to implement incluing Hardcoding.
            //the if statement looks for the device's address number so 00:26:33:CD:28:F6 is a Z008182 address.
            if (deviceInfo.getAddress().matches("00:26:33:CD:28:F6")) {
                //if you find the device, then send the bluetooth information over.
                Log.d(TAG, "When HardCode Device Matches: " + deviceInfo);
                discoveredDeviceInfo = deviceInfo;
                deviceInfoStringAddress = discoveredDeviceInfo.getAddress();
                deviceInfoStringName = discoveredDeviceInfo.getName();
                deviceInfoStringProtocol = discoveredDeviceInfo.getProtocol();
                deviceInfoStringSerialNumber = discoveredDeviceInfo.getSerialNumber();
                deviceInfoStringAdvertisementDataName = discoveredDeviceInfo.getAdvertisementDataName();


                handleUpdateListScan.post(runUpdateListScan);// I need this in next activity to connect
                // deviceInfoArray[0] = deviceInfoStringAdvertisementDataName;
                deviceInfoArray.add(deviceInfoStringAddress);
                deviceInfoArray.add(deviceInfoStringName);
                deviceInfoArray.add(deviceInfoStringProtocol);
                deviceInfoArray.add(deviceInfoStringSerialNumber);
                deviceInfoArray.add(deviceInfoStringAdvertisementDataName);


                mBundleData.setDeviceInfoArray(deviceInfoArray);
                Log.d("deviceInfo", "Hello: " + discoveredDeviceInfo.toString());
               /* deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
                handleUpdateInfo.post(runUpdateInfo);*/ //I put this inside the handlerUpdateListScan

            } else {
                Log.d(TAG, "Device Not Found: " + deviceInfo.getAdvertisementDataName());
                //deviceManager.startDiscovery(MainActivity.this);
            }
        }

        @Override
        public void deviceConnected(Device device) {
            currDevice = device;
            Log.d(TAG, "Checkcheck");

            currDevice.setDeviceCallback(deviceCallback);
            infoList.add("devConnected");
            // handleUpdateInfo.post(runUpdateInfo);
            //  if (dialogConnection != null) dialogConnection.dismiss();

        }

        @Override
        public void deviceDisconnected(Device device) {
            infoDisconnect = "Disconnected \n" + device.getDeviceInfo().getAdvertisementDataName();
            currDevice = null;
            //  handleUpdateInfo.post(runUpdateInfo);
        }

        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice = null;
            infoDisconnect = deviceInfo.getAdvertisementDataName() + " Connection Fail";
            //  handleUpdateInfo.post(runUpdateInfo);
            //  if (dialogConnection != null) dialogConnection.dismiss();
        }

        @Override
        public void bluetoothLowEnergieIsNotSupported() {
            infoDisconnect = "Bluetooth Low Energie Is Not Supported";
            //  handleUpdateInfo.post(runUpdateInfo);
            //   if (dialogConnection != null) dialogConnection.dismiss();
        }

        @Override
        public void bluetoothIsPoweredOFF() {
            infoDisconnect = "Bluetooth Is Powered OFF";
            // handleUpdateInfo.post(runUpdateInfo);
            //  if (dialogConnection != null) dialogConnection.dismiss();

            deviceManager.turnOnBluetooth(myContext);
        }

        @Override
        public void accessCoarseLocationPermissionRequired() {
            //Android M runtime authorizzation
            infoDisconnect = "Access Coarse Location Permission Required";
            //handleUpdateInfo.post(runUpdateInfo);

            //For this request you need to implement callback
            deviceManager.requestCoarseLocationPermission(BlowActivity.this, PERMISSION_REQUEST_COARSE_LOCATION);
        }


    };

    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {

            //   deviceInfoArray.add(discoveredDeviceInfo); // I need this for sure
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
            //  handleUpdateInfo.post(runUpdateInfo);
        } //+++
    };

    DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void flowUpdated(Device device, float flow, int stepVolume, boolean isFirstPackage) {
            Log.d("hyunrae", "a");

        }

        @Override
        public void resultsUpdated(ResultsPefFev1 resultsPefFev1) {
            Log.d("hyunrae", "b");

        }

        @Override
        public void resultsUpdated(ResultsFvc resultsFvc) {
            Log.d("hyunrae", "c");

        }

        @Override
        public void testRestarted(Device device) {
            Log.d("hyunrae", "d");

        }

        @Override
        public void testStopped(Device device) {
            Log.d("hyunrae", "e");

        }

        @Override
        public void softwareUpdateProgress(float progress, Device.UpdateStatus status, String error) {
            Log.d("hyunrae", "f");

        }
    };

}
