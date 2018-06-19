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
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity implements Serializable  {

    Button submitButton;
    EditText etPassword;
    ImageView spirometerImage;
    TextView spiroCheck;
    ProgressBar spiroProgressBar;
    String truePassword = "123456";

    String deviceInfoStringAddress;
    String deviceInfoStringName;
    String deviceInfoStringProtocol;
    String deviceInfoStringSerialNumber;
    String deviceInfoStringAdvertisementDataName;

    ArrayList<String> deviceInfoArray = new ArrayList<>();

    ArrayList<String> infoList = new ArrayList<>();
    Device currDevice;
    String fullInfo = "", result = "", infoDisconnect = "", strProgress ="";
    Context myContext;
    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    DeviceManager deviceManager;
    DeviceInfo discoveredDeviceInfo;

    private static final String TAG = LoginActivity.class.getSimpleName();
    //This is a MyParcelable object that contains data / objects to be passed between activities
    private MyParcelable mBundleData;

    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mBundleData = new MyParcelable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

            //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        submitButton = (Button) findViewById(R.id.submitButton);
        etPassword = (EditText) findViewById(R.id.etPassword);
        spirometerImage = (ImageView) findViewById(R.id.spirometerImage);
        int imageResource = getResources().getIdentifier("@drawable/spiro", null, this.getPackageName());
        spirometerImage.setImageResource(imageResource);
        spiroCheck = (TextView) findViewById(R.id.spiroCheck);
        spiroProgressBar = (ProgressBar) findViewById(R.id.spiroProgressBar);

        //get device manager instance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);


        //peter: this will look for specific user's device, at our case, it is Z008182
        deviceManager.startDiscovery(LoginActivity.this);


        new CountDownTimer(7000,1000){
            @Override
            public void onTick(long millisUntilFinished){
                submitButton.setVisibility(View.INVISIBLE);
                etPassword.setVisibility(View.INVISIBLE);
                spirometerImage.setVisibility(View.VISIBLE);
                spiroCheck.setVisibility(View.VISIBLE);
                spiroProgressBar.setVisibility(View.VISIBLE);
                }
            @Override
            public void onFinish(){
                //set the new Content of your activity
                submitButton.setVisibility(View.VISIBLE);
                etPassword.setVisibility(View.VISIBLE);
                spirometerImage.setVisibility(View.GONE);
                spiroCheck.setVisibility(View.GONE);
                spiroProgressBar.setVisibility(View.GONE);
            }
        }.start();


        //we want to create a login request, when the user actually clicks the login button, so onClickListener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(truePassword.equals(etPassword.getText().toString())) {

                    // do stuff
                    Intent intent = new Intent(LoginActivity.this, ConnectingActivity.class);
                    Log.d(TAG, "bundle-data" +mBundleData);
                    intent.putExtra("bundle-data", mBundleData);
                    //intent.putExtra("BlueTooth Connect Info", (Parcelable) discoveredDeviceInfo);
                    LoginActivity.this.startActivity(intent);
                    deviceManager.stopDiscovery();


                    //finish();

                }else{
                    Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_LONG).show();

                    //  Log.d(TAG, "TypedPassword" + typedPasswordOne);
                    // Log.d(TAG, "TruePassword" + stringTruePassword);
                }

            }
        });
    }

    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
            Log.d(TAG, "deviceDiscovered: " + deviceInfo.getAddress());
            //peter: this is a hardCode. I was first told to focus on connecting only one device using whatever I want to implement incluing Hardcoding.
            //the if statement looks for the device's address number so 00:26:33:CD:28:F6 is a Z008182 address.
            if(deviceInfo.getAddress().matches("00:26:33:CD:28:F6")) {
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
                Log.d("deviceInfo", "Hello: " +discoveredDeviceInfo.toString());
               /* deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
                handleUpdateInfo.post(runUpdateInfo);*/ //I put this inside the handlerUpdateListScan

            }else{
                Log.d(TAG, "Device Not Found: " + deviceInfo.getAdvertisementDataName());
                //deviceManager.startDiscovery(MainActivity.this);
            }
        }

        @Override
        public void deviceConnected(Device device) {
            currDevice = device;
            Log.d(TAG, "Checkcheck" );

            // currDevice.setDeviceCallback(deviceCallback);
            infoList.add("devConnected");
           // handleUpdateInfo.post(runUpdateInfo);
          //  if (dialogConnection != null) dialogConnection.dismiss();

        }

        @Override
        public void deviceDisconnected(Device device) {
            infoDisconnect = "Disconnected \n" + device.getDeviceInfo().getAdvertisementDataName();
            currDevice=null;
          //  handleUpdateInfo.post(runUpdateInfo);
        }

        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice=null;
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
        public void accessCoarseLocationPermissionRequired(){
            //Android M runtime authorizzation
            infoDisconnect = "Access Coarse Location Permission Required";
            //handleUpdateInfo.post(runUpdateInfo);

            //For this request you need to implement callback
            deviceManager.requestCoarseLocationPermission(LoginActivity.this,PERMISSION_REQUEST_COARSE_LOCATION);
        }
    };


    //This Call back need for new Android M runtime authorization
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deviceManager.startDiscovery(this);
                } else {
                    Snackbar.make(findViewById(R.id.main_layout), "Can't scan without Location authorization on Android M", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }


    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {

        //   deviceInfoArray.add(discoveredDeviceInfo); // I need this for sure
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
          //  handleUpdateInfo.post(runUpdateInfo);
        } //+++
    }; //여기에다가 listview를 에드하는듯 하다.

}
