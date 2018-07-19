package com.spirometry.homespirometry;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceCallback;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdk.Patient;
import com.spirometry.spirobanksmartsdk.ResultsFvc;
import com.spirometry.spirobanksmartsdk.ResultsPefFev1;
import com.spirometry.homespirometry.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BlowActivity extends AppCompatActivity {

    private static final String TAG = BlowActivity.class.getSimpleName();

    DeviceManager deviceManager;
    TextView blowDirection;
    TextView blowMessage;
    TextView numberCount;
    ProgressBar loadingBlow;
    ImageView imageView;
    TextView numberOutOf;
    Button buttonReBlow;
    TextView postingResult;

    //This is a MyParcelable object that contains data / objects to be passed between activities
    private MyParcelable mBundleData;

    Patient patient;

    Context myContext;
    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    String deviceInfoStringAddress;
    String deviceInfoStringName;
    String deviceInfoStringProtocol;
    String deviceInfoStringSerialNumber;
    String deviceInfoStringAdvertisementDataName;
    ArrayList<String> deviceInfoArray = new ArrayList<>();
    ArrayList<String> arr;

    ArrayList<String> infoList = new ArrayList<>();
    Device currDevice;
    String infoDisconnect;

    DeviceInfo discoveredDeviceInfo;

    int numBlows = 0;
    int value = numBlows + 1;
    private int messageNumber = 7;
    private int messageNumberFvc = 6;

    private String patient_id = "000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mBundleData = getIntent().getParcelableExtra("bundle-data");
        arr = mBundleData.getDeviceInfo();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blow);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //get device manager instance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);

        //currDevice is already connected device from the previous Intent
        currDevice = deviceManager.getDeviceConnected();

        //Having callbacks: startTest() results to return
        currDevice.setDeviceCallback(deviceCallback);

        currDevice.startTest(getApplicationContext(), Device.TestType.Fvc, (byte)50);

        blowDirection = (TextView) findViewById(R.id.blowDirection);
        blowMessage = (TextView) findViewById(R.id.blowMessage);
        numberCount = (TextView) findViewById(R.id.numberCount);
        loadingBlow = (ProgressBar) findViewById(R.id.loadingBlow);
        imageView = (ImageView) findViewById(R.id.imageView);
        numberOutOf = (TextView) findViewById(R.id.numberOutOf);
        buttonReBlow = (Button) findViewById(R.id.buttonReBlow);
        postingResult = (TextView) findViewById(R.id.postingResult);

        findViewById(R.id.buttonReBlow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numBlows <6) {
                    currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)50);
                    buttonReBlow.setVisibility(View.INVISIBLE);
                    blowDirection.setVisibility(View.VISIBLE);
                }else{
                    currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
                    buttonReBlow.setVisibility(View.INVISIBLE);
                    blowDirection.setVisibility(View.VISIBLE);
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
            if (deviceInfo.getAddress().matches("00:26:33:CD:28:EB")) {
                //if you find the device, then send the bluetooth information over.
                Log.d(TAG, "When HardCode Device Matches: " + deviceInfo);
      /*          discoveredDeviceInfo = deviceInfo;
                deviceInfoStringAddress = discoveredDeviceInfo.getAddress();
                deviceInfoStringName = discoveredDeviceInfo.getName();
                deviceInfoStringProtocol = discoveredDeviceInfo.getProtocol();
                deviceInfoStringSerialNumber = discoveredDeviceInfo.getSerialNumber();
                deviceInfoStringAdvertisementDataName = discoveredDeviceInfo.getAdvertisementDataName(); */

                handleUpdateListScan.post(runUpdateListScan); // I need this in next activity to connect
                // deviceInfoArray[0] = deviceInfoStringAdvertisementDataName;
        /*        deviceInfoArray.add(deviceInfoStringAddress);
                deviceInfoArray.add(deviceInfoStringName);
                deviceInfoArray.add(deviceInfoStringProtocol);
                deviceInfoArray.add(deviceInfoStringSerialNumber);
                deviceInfoArray.add(deviceInfoStringAdvertisementDataName);

                mBundleData.setDeviceInfoArray(deviceInfoArray); */
         //       Log.d("deviceInfo", "Hello: " + discoveredDeviceInfo.toString());
               /* deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
                handleUpdateInfo.post(runUpdateInfo);*/ //I put this inside the handlerUpdateListScan
            } else {
                Log.d(TAG, "Device Not Found: " + deviceInfo.getAdvertisementDataName());
            }
        }

        @Override
        public void deviceConnected(Device device) {
            currDevice = device;
            Log.d(TAG, "Checkcheck");
            infoList.add("devConnected");
            deviceManager.stopDiscovery();
            currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
            // handleUpdateInfo.post(runUpdateInfo);
            //  if (dialogConnection != null) dialogConnection.dismiss();
        }

        @Override
        public void deviceDisconnected(Device device) {
            infoDisconnect = "Disconnected \n" + device.getDeviceInfo().getAdvertisementDataName();
            deviceManager.startDiscovery(BlowActivity.this);
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
            //Android M runtime authorization
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
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
        }
    };

    DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void flowUpdated(Device device, float flow, int stepVolume, boolean isFirstPackage) {
            Log.d("hyunrae", "a");
            value = numBlows;
            handlerVisibilityChange.post(runVisibilityChange);
        }

        @Override
        public void resultsUpdated(ResultsPefFev1 resultsPefFev1) {
            numBlows++;
            messageNumber--;

            Log.d("hyunrae", "one more test added");
            String pef = String.valueOf(resultsPefFev1.getPef_cLs() * 60 / (float) 100);
            String fev1 = String.valueOf(resultsPefFev1.getFev1_cL() / (float) 100);
            String peftime = String.valueOf(resultsPefFev1.getPefTime_msec());
            String evol = String.valueOf(resultsPefFev1.geteVol_mL() );

            String [] resultArrayPefFev1 = {pef, fev1, peftime, evol};

            mBundleData.setBlowDataArrayPefFev1(0, resultArrayPefFev1);

            handlerTextViewNumberChange.post(runTextViewNumberChange);

            Log.d(TAG, "wow: " + String.valueOf(resultsPefFev1.getPef_cLs() * 60 / (float) 100));
            currDevice.stopTest(getApplicationContext());
        }

        @Override
        public void resultsUpdated(ResultsFvc resultsFvc) {
            numBlows++;
            messageNumber--;
            handlerTextViewNumberChange.post(runTextViewNumberChange);

            int overallNumBlows = numBlows -1;

            String pef = String.valueOf(resultsFvc.getPef_cLs() * 60 / (float) 100);
            String fev1 = String.valueOf(resultsFvc.getFev1_cL() / (float) 100);
            String fvc = String.valueOf(resultsFvc.getFvc_cL() / (float) 100);
            String fev1_fvc = String.valueOf(Math.round(resultsFvc.getFev1_Fvc_pcnt() * 100)/(float)100);
            String fev6 = String.valueOf(resultsFvc.getFev6_cl() / (float) 100);
            String fef2575 = String.valueOf(resultsFvc.getFef2575_cLs()  / (float) 100);

            String [] resultArray = {pef, fev1, fvc, fev1_fvc, fev6, fef2575};

            Log.d("overallNumBlows",  "" + overallNumBlows);
            Log.d("resultArray",  "" + resultArray[0]);
            Log.d("resultArray",  "" + resultArray[1]);
            Log.d("resultArray",  "" + resultArray[2]);
            Log.d("resultArray",  "" + resultArray[3]);

            mBundleData.setBlowDataArray(overallNumBlows, resultArray);

            Log.d(TAG, "wow2: " + String.valueOf(resultsFvc.getPef_cLs() * 60 / (float) 100));

            if(numBlows <6) {
                handlerWaitandStartFvc.post(runWaitandStartFvc); // the thing here is that what happen if the data upload doesn't upload?
                //like the data upload time is longer than the delay time I set... :( that would be loss in data
                // and it will crash
            }else{
                handlerWaitandStartPefFev1.post(runWaitandStartPefFev1); // the thing here is that what happen if the data upload doesn't upload?
            }
        }

        @Override
        public void testRestarted(Device device) {
            Log.d("hyunrae", "dddd");
            Log.d("peter", " " + numBlows);
            if(numBlows >=6) {
                currDevice.stopTest(getApplicationContext());
                Log.d("done with all 7 tests", "done with all 7 tests");
                handlerVisibilityChange.post(runVisibilityChange);
                handleIntentToTestComplete.post(runIntentToTestComplete); // the thing here is that what happen if the data upload doesn't upload?
            }else {
                handlerVisibilityChangeTwo.post(runVisibilityChangeTwo);
            }
        }

        @Override
        public void testStopped(Device device) {
           // numBlowsFvc++;
            Log.d("Stopped", "Test has been stopped");
            if (numBlows > 6) {
                Log.d("final stopped", "Test has been stopped last");
            }else if(value == numBlows){
                handlerVisibilityChange.post(runVisibilityChange);
                handlerVisibilityChangeTwoWaitOneSecond.post(runVisibilityChangeTwoWaitOneSecond);
            }
            else{
                Log.d(TAG, "numBlowsCounts: " + numBlows);
                handlerVisibilityChangeTwo.post(runVisibilityChangeTwo);
                handlerButton.post(runButton);
            }
        }

        @Override
        public void softwareUpdateProgress(float progress, Device.UpdateStatus status, String error) {
            Log.d("hyunrae", "f");
        }
    };
    Handler handlerTextViewNumberChange = new Handler();
    Runnable runTextViewNumberChange = new Runnable() {
        @Override
        public void run() {
            blowMessage.setText("You Have " + messageNumber + " Blows Left");
            numberCount.setText("" + numBlows);
        } //+++
    };

    Handler handlerVisibilityChange = new Handler();
    Runnable runVisibilityChange = new Runnable() {
        @Override
        public void run() {
            loadingBlow.setVisibility(View.VISIBLE);
            blowDirection.setVisibility(View.INVISIBLE);
            blowMessage.setVisibility(View.INVISIBLE);
            numberCount.setVisibility(View.INVISIBLE);
            numberOutOf.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
    };

    Handler handlerVisibilityChangeTwo = new Handler();
    Runnable runVisibilityChangeTwo = new Runnable() {
        @Override
        public void run() {
            loadingBlow.setVisibility(View.INVISIBLE);
            blowDirection.setVisibility(View.VISIBLE);
            blowMessage.setVisibility(View.VISIBLE);
            numberCount.setVisibility(View.VISIBLE);
            numberOutOf.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
        } //+++
    };

    Handler handleIntentToTestComplete = new Handler();
    Runnable runIntentToTestComplete= new Runnable() {
        @Override
        public void run() {
            deviceManager.disconnect();
            //Intent intent = new Intent(BlowActivity.this, BlowDataUploadPage.class); //PulseConnectingActivity
            Intent intent = new Intent(BlowActivity.this, PulseConnectingActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            BlowActivity.this.startActivity(intent);
            finish();
        }
    };

    Handler handlerButton = new Handler();
    Runnable runButton= new Runnable() {
        @Override
        public void run() {
            buttonReBlow.setVisibility(View.VISIBLE);
            blowDirection.setVisibility(View.INVISIBLE);
        }
    };

    Handler handlerWaitandStartFvc = new Handler();
    Runnable runWaitandStartFvc = new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    currDevice.startTest(getApplicationContext(), Device.TestType.Fvc, (byte)50);
                    Log.d(TAG, "bruh fvc it worked?");
                }
            }, 1000);
        }
    };

    Handler handlerWaitandStartPefFev1 = new Handler();
    Runnable runWaitandStartPefFev1= new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
                    Log.d(TAG, "bruh peffev1 it worked?");
                }
            }, 1000);        }
    };

    Handler handlerVisibilityChangeTwoWaitOneSecond = new Handler();
    Runnable runVisibilityChangeTwoWaitOneSecond= new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handlerVisibilityChangeTwo.post(runVisibilityChangeTwo);
                }
            }, 1000);        }
    };

    public void onClickHelp(View view) {
        Intent intent = new Intent(BlowActivity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

}
