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


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceCallback;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdk.Patient;
import com.spirometry.spirobanksmartsdk.ResultsFvc;
import com.spirometry.spirobanksmartsdk.ResultsPefFev1;
import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BlowActivity extends AppCompatActivity {

    String[][] blowDataStore = new String[6][4]; //6 data storing 4 String Values; +-

   /* ArrayList<Patientsss> PatientBlowInfo = new ArrayList<>();
    String pef;
    String fev1;
    String peftime;
    String evol; */

  //  Patientsss patientResults = new Patientsss(pef, fev1, peftime, evol); // something I can add in the arraylist

    private static final String TAG = BlowActivity.class.getSimpleName();
    int whileLoop = 0;

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

    private int numBlows = 0;
    private int numBlowsFvc = 0;
    private int messageNumber = 6;
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

        currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
        currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)40);
        if(numBlows > 6) {
            currDevice.stopTest(getApplicationContext());
        }

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
                currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
                currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)40);
                buttonReBlow.setVisibility(View.INVISIBLE);
                blowDirection.setVisibility(View.VISIBLE);
            }
        });
    }

    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
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

                handleUpdateListScan.post(runUpdateListScan); // I need this in next activity to connect
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
            handlerVisibilityChange.post(runVisibilityChange);
        }

        @Override
        public void resultsUpdated(ResultsPefFev1 resultsPefFev1) {
            numBlows++;
            messageNumber--;
            int overallNumBlows = numBlows -1;

            Log.d("hyunrae", "b");
            Log.d("hyunrae", "one more test added");
            String pef = String.valueOf(resultsPefFev1.getPef_cLs() * 60 / (float) 100);
            String fev1 = String.valueOf(resultsPefFev1.getFev1_cL() / (float) 100);
            String peftime = String.valueOf(resultsPefFev1.getPefTime_msec());
            String evol = String.valueOf(resultsPefFev1.geteVol_mL() );
            //String fef2575 = String.valueOf(resultsFvc.getFef2575_cLs()  / (float) 100);


            handlerTextViewNumberChange.post(runTextViewNumberChange);

            String [] resultArray = {pef, fev1, peftime, evol};
            Log.d("overallNumBlows",  "" + overallNumBlows);
            Log.d("resultArray",  "" + resultArray[0]);
            Log.d("resultArray",  "" + resultArray[1]);
            Log.d("resultArray",  "" + resultArray[2]);
            Log.d("resultArray",  "" + resultArray[3]);

            mBundleData.setBlowDataArray(overallNumBlows, resultArray);
            Log.d("PETER", mBundleData.getBlowDataArray()[0][0]);


        }

        @Override
        public void resultsUpdated(ResultsFvc resultsFvc) { // NOT USED
            numBlowsFvc++;
            messageNumberFvc--;
            int overallNumBlowsFvc = numBlowsFvc -1;

            String pef = String.valueOf(resultsFvc.getPef_cLs() * 60 / (float) 100);
            String fev1 = String.valueOf(resultsFvc.getFev1_cL() / (float) 100);
            String fvc = String.valueOf(resultsFvc.getFvc_cL() / (float) 100);
            String fev1_fvc = String.valueOf(Math.round(resultsFvc.getFev1_Fvc_pcnt() * 100)/(float)100);
            String fev6 = String.valueOf(resultsFvc.getFev6_cl() / (float) 100);
            String fef2575 = String.valueOf(resultsFvc.getFef2575_cLs()  / (float) 100);
        }

        @Override
        public void testRestarted(Device device) {
            Log.d("hyunrae", "dddd");
            if(numBlows >= 6){
                Log.d(TAG, "numBlows =6");
                currDevice.stopTest(getApplicationContext());
             //   handlerPostingResult2.post(runPostingResult2);
              //  handlerVisibilityChange.post(runVisibilityChange);
                handleIntentToTestComplete.post(runIntentToTestComplete);
            }else {
                handlerVisibilityChangeTwo.post(runVisibilityChangeTwo);
            }
        }

        @Override
        public void testStopped(Device device) {
            Log.d("hyunrae", "Test has been stopped");
            // here you want to display to the participant that he or she probably didn't blow so the test stopped.
            if (numBlows >=6) {
       /*         mBundleData.setBlowDataArray(blowDataStore); */ //+-
            //    handlerVisibilityChange.post(runVisibilityChange);
             //   handlerPostingResult.post(runPostingResult);
                Log.d("hyunrae", "test stopped and no more to do");
                // here you want to display to the participant to blow again
            }else {
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
            Intent intent = new Intent(BlowActivity.this, BlowDataUploadPage.class); //PulseConnectingActivity
            intent.putExtra("bundle-data", mBundleData);
            BlowActivity.this.startActivity(intent);
            finish();
            // tvConnecting.setText();
            //   tvConnecting.setText(success);
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

    Handler handlerPostingResult = new Handler();
    Runnable runPostingResult= new Runnable() {
        @Override
        public void run() {
            postingResult.setVisibility(View.VISIBLE);
        }
    };

    Handler handlerPostingResult2 = new Handler();
    Runnable runPostingResult2= new Runnable() {
        @Override
        public void run() {
            postingResult.setVisibility(View.VISIBLE);

        }
    };

   void upload_PefFev1(final String pef, final String fev1, final String peftime, final String evol) {
        // Tag used to cancel the request
        String tag_string_req = "req_response";
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_PEFFEV1_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Instance Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);


                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to response url
                Map<String, String> params = new HashMap<String, String>();
                params.put("patient_id", patient_id);
                params.put("pef", pef);
                params.put("fev1", fev1);
                params.put("peftime", peftime);
                params.put("evol", evol);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}


