package com.spirometry.homespirometry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.homespirometry.classes.SuperActivity;
import com.spirometry.homespirometry.classes.UrlConfig;
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceCallback;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdk.Patient;
import com.spirometry.spirobanksmartsdk.ResultsFvc;
import com.spirometry.spirobanksmartsdk.ResultsPefFev1;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends SuperActivity {

    DeviceManager deviceManager;
    Device currDevice;

    DeviceInfo discoveredDeviceInfo;

    Patient patient;

    ArrayAdapter<DeviceInfo> deviceInfoArray;
    ListView listView;

    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    String fullInfo = "", result = "", infoDisconnect = "", strProgress ="";
    int qualityMsgCode;
    int qualityRawCode;
    Float predictedPercentageOfTarget, actualPercentageOfTarget;

    Context myContext;
    private ProgressDialog dialogConnection;
    Spinner ageSpinner;

    ArrayList<String> infoList = new ArrayList<>();

    int volume;

    long startUpdate=0;

    private static final String TAG = MainActivity.class.getSimpleName();
    private String patient_id = "000000";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        myContext = this;

        // Get patient id
        EditText pidText= (EditText) findViewById(R.id.patientid_text);
        patient_id = pidText.getText().toString();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ageSpinner =(Spinner) findViewById(R.id.patientAge);

        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                switch ((int)id){
                    case 0:
                        calendar.add(Calendar.YEAR, -5);
                        patient = new Patient(calendar.getTime(), 100, 20, Patient.GENDER_MALE, Patient.ETHNICITY_CAUCASIAN);
                        break;
                    case 1:
                        calendar.add(Calendar.YEAR, -9);
                        patient = new Patient(calendar.getTime(), 150, 40, Patient.GENDER_MALE, Patient.ETHNICITY_CAUCASIAN);
                        break;
                    case 2:
                        calendar.add(Calendar.YEAR, -40);
                        patient = new Patient(calendar.getTime(), 180, 80, Patient.GENDER_MALE, Patient.ETHNICITY_CAUCASIAN);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.YEAR, -40);
                ageSpinner.setSelection(2);
                patient = new Patient(calendar.getTime(), 180, 80, Patient.GENDER_MALE, Patient.ETHNICITY_CAUCASIAN);
            }
        });


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -40);

        ageSpinner.setSelection(2);

        //set patient object: need for calculate target, quality message and test incentive
        patient = new Patient(calendar.getTime(), 180, 78, Patient.GENDER_MALE, Patient.ETHNICITY_CAUCASIAN);

        Log.d("PefPredicted", String.valueOf(patient.getPefPredicted_Ls())+" L/s");
        Log.d("PefPredicted", String.valueOf(patient.getPefPredicted_Lm())+" L/m");
        Log.d("FvcPredicted", String.valueOf(patient.getFvcPredicted_L())+" L");
        Log.d("Fev6Predicted", String.valueOf(patient.getFev6Predicted_L())+" L");
        Log.d("Fev1Predicted", String.valueOf(patient.getFev1Predicted_L())+" L");
        Log.d("Fef2575Predicted", String.valueOf(patient.getFef2575Predicted_Ls())+" L/s");
        Log.d("Fev1_FvcPredicted", String.valueOf(patient.getFev1_FvcPredicted_perc())+" %");

        //get device manager istance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);


        //set list view for discovered devices
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                infoList.add("Connecting " + deviceInfoArray.getItem(position).getAdvertisementDataName());
                handleUpdateInfo.post(runUpdateInfo);
                dialogConnection = new ProgressDialog(myContext);
                dialogConnection.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialogConnection.setCancelable(false);
                dialogConnection.setMessage("Connecting...");
                dialogConnection.show();
                deviceManager.connect(getApplicationContext(), deviceInfoArray.getItem(position));
            }
        });

        //allow to scroll info
        ((TextView) findViewById(R.id.tvInfo)).setMovementMethod(new ScrollingMovementMethod());

        //set array for listView
        deviceInfoArray = new ArrayAdapter<DeviceInfo>(this, R.layout.list_item);
        listView.setAdapter(deviceInfoArray);//여기다가 프린트하는거 같아 ex) Start Scan


        //peter: this will look for specific user's device, at our case, it is Z008182
       deviceManager.startDiscovery(MainActivity.this);

       /* deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
        handleUpdateInfo.post(runUpdateInfo); */ // this is inside DeviceDiscovered method


        findViewById(R.id.btnStartScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoList.add("Start Scan");
                deviceManager.startDiscovery(MainActivity.this);// so this basically looks for the device to connect

                deviceInfoArray.clear(); //이 어레이는 옛날에 나와있던 Z008182를 없애준다
                handleUpdateInfo.post(runUpdateInfo); //여기서 업데이트 해주는거 같은데

            }
        });

        findViewById(R.id.btnStopScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoList.add("Stop Scan");
                handleUpdateInfo.post(runUpdateInfo);

                deviceManager.stopDiscovery();
            }
        });

        findViewById(R.id.btnDirectConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceInfo lastDeviceConnected = deviceManager.getLastConnectedDeviceInfo(getApplicationContext());
                if (lastDeviceConnected != null) {
                    dialogConnection = new ProgressDialog(myContext);
                    dialogConnection.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialogConnection.setCancelable(false);
                    dialogConnection.setMessage("Connecting...");
                    dialogConnection.show();
                    infoList.add( "Connecting " + lastDeviceConnected.getAdvertisementDataName());
                    handleUpdateInfo.post(runUpdateInfo);

                    deviceManager.connect(getApplicationContext(), lastDeviceConnected);

                } else {
                    infoList.add("No device in memory");
                    handleUpdateInfo.post(runUpdateInfo);
                }
            }
        });

        findViewById(R.id.btnStartPefFev1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice != null) {

                    infoList.add("Start Test PefFev1 " + currDevice.getDeviceInfo().getAdvertisementDataName());
                    handleUpdateInfo.post(runUpdateInfo);

                    volume=0;
                    //EndOfTestTimeOUT from 15 to 120 sec - Default = 15 sec
                    Log.d(TAG, "1 fvc: ");
                    currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
                    Log.d(TAG, "1 pefFev1: ");
                    findViewById(R.id.btnStartFvc).setEnabled(false);
                    findViewById(R.id.btnStartPefFev1).setEnabled(false);

                }
            }
        });

        findViewById(R.id.btnStartFvc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice != null) {
                    infoList.add("Start Test Fvc " + currDevice.getDeviceInfo().getAdvertisementDataName());
                    handleUpdateInfo.post(runUpdateInfo);

                    volume=0;
                    //EndOfTestTimeOUT from 15 to 120 sec - Default = 15 sec
                 //  currDevice.startTest(getApplicationContext(), Device.TestType.Fvc);

                   currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)90);
                    Log.d(TAG, "1 fvc: ");
                  //  currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
                    Log.d(TAG, "1 pefFev1: ");

                    //    currDevice.startTest;
                    findViewById(R.id.btnStartFvc).setEnabled(false);
                    findViewById(R.id.btnStartPefFev1).setEnabled(false);
                }
            }
        });

        findViewById(R.id.btnStopTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice != null) {

                    infoList.add("Stop Test " + currDevice.getDeviceInfo().getAdvertisementDataName());
                    handleUpdateInfo.post(runUpdateInfo);

                    currDevice.stopTest(getApplicationContext());
                }
            }
        });

        findViewById(R.id.btnDisconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice != null) {
                    deviceManager.disconnect();
                    currDevice = null;
                }
            }
        });

        final String version1 = "2.5";
        ((TextView)findViewById(R.id.btnUpgrade1)).setText("Update device To " + version1);
        findViewById(R.id.btnUpgrade1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice==null) {
                    infoList.add("Connect device to update software " + version1);
                    handleUpdateInfo.post(runUpdateInfo);
                    return;
                }
                InputStream firmware =  getResources().openRawResource(R.raw.spirobank_smart_25_encrypt);
                byte[] updateData;
                try {
                    updateData=new byte[firmware.available()];
                    firmware.read(updateData);
                }catch (Exception ex){
                    infoList.add("Error " + ex.getMessage());
                    handleUpdateInfo.post(runUpdateInfo);
                    return;
                }
                infoList.add("Start SoftwareUpdate " + version1 + " " + currDevice.getDeviceInfo().getAdvertisementDataName());
                handleUpdateInfo.post(runUpdateInfo);
                strProgress = " ";
                handleSoftwareUpdateProgress.post(runSoftwareUpdateProgress);
                startUpdate=System.currentTimeMillis();
                currDevice.startSoftwareUpdateProcedure(getApplicationContext(),updateData);
            }
        });

        final String version2 = "2.6";
        ((TextView)findViewById(R.id.btnUpgrade2)).setText("Update device To " + version2);
        findViewById(R.id.btnUpgrade2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice==null) {
                    infoList.add("Connect device to update software " + version2);
                    handleUpdateInfo.post(runUpdateInfo);
                    return;
                }
                InputStream firmware =  getResources().openRawResource(R.raw.spirobank_smart_26_encrypt);
                byte[] updateData;
                try {
                    updateData=new byte[firmware.available()];
                    firmware.read(updateData);
                }catch (Exception ex){
                    infoList.add("Error " + ex.getMessage());
                    handleUpdateInfo.post(runUpdateInfo);
                    return;
                }
                infoList.add("Start SoftwareUpdate " + version2 + " " + currDevice.getDeviceInfo().getAdvertisementDataName());
                handleUpdateInfo.post(runUpdateInfo);
                strProgress = " ";
                handleSoftwareUpdateProgress.post(runSoftwareUpdateProgress);
                startUpdate=System.currentTimeMillis();
                currDevice.startSoftwareUpdateProcedure(getApplicationContext(),updateData);
            }
        });
        String version="";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception ex){}

        this.setTitle(getString(R.string.app_name) + " " + version);
    }


    @Override
    public void onPause() {
        deviceManager.disconnect();
        currDevice=null;
        deviceManager.stopDiscovery();
        super.onPause();
    }


    DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void deviceDiscovered(DeviceInfo deviceInfo) {
          Log.d(TAG, "deviceDiscovered: " + deviceInfo.getAddress());
    //peter: this is a hardCode. I was first told to focus on connecting only one device using whatever I want to implement incluing Hardcoding.
    //the if statement looks for the device's address number so 00:26:33:CD:28:F6 is a Z008182 address.
          if(deviceInfo.getAddress().matches("00:26:33:CD:28:EB")) {
                //if you find the device, then send the bluetooth information over.
                Log.d(TAG, "bro: " + deviceInfo);
                discoveredDeviceInfo = deviceInfo;
                handleUpdateListScan.post(runUpdateListScan);
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
            currDevice.setDeviceCallback(deviceCallback);
            infoList.add("devConnected");
            handleUpdateInfo.post(runUpdateInfo);
            if (dialogConnection != null) dialogConnection.dismiss();

        }

        @Override
        public void deviceDisconnected(Device device) {
            infoDisconnect = "Disconnected \n" + device.getDeviceInfo().getAdvertisementDataName();
            currDevice=null;
            handleUpdateInfo.post(runUpdateInfo);

        }

        @Override
        public void deviceConnectionFailed(DeviceInfo deviceInfo) {
            currDevice=null;
            infoDisconnect = deviceInfo.getAdvertisementDataName() + " Connection Fail";
            handleUpdateInfo.post(runUpdateInfo);
            if (dialogConnection != null) dialogConnection.dismiss();
        }

        @Override
        public void bluetoothLowEnergieIsNotSupported() {
            infoDisconnect = "Bluetooth Low Energie Is Not Supported";
            handleUpdateInfo.post(runUpdateInfo);
            if (dialogConnection != null) dialogConnection.dismiss();
        }

        @Override
        public void bluetoothIsPoweredOFF() {
            infoDisconnect = "Bluetooth Is Powered OFF";
            handleUpdateInfo.post(runUpdateInfo);
            if (dialogConnection != null) dialogConnection.dismiss();

            deviceManager.turnOnBluetooth(myContext);
        }
        @Override
        public void accessCoarseLocationPermissionRequired(){
            //Android M runtime authorizzation
            infoDisconnect = "Access Coarse Location Permission Required";
            handleUpdateInfo.post(runUpdateInfo);

            //For this request you need to implement callback
            deviceManager.requestCoarseLocationPermission(MainActivity.this,PERMISSION_REQUEST_COARSE_LOCATION);
        }
    };


    //This Call back need for new Android M runtime authorizzation
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

    DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void flowUpdated(Device device, float flow, int stepVolume, boolean isFirstPackage) {
            volume+=stepVolume;
            //Log.d("FLOW ", String.valueOf(flow) + " " + volume + " " + isFirstPackage);
            Log.d("flowUpdated ", String.valueOf(flow) + " " + String.valueOf(isFirstPackage));
            predictedPercentageOfTarget = patient.predictedPercentageOfTargetWithFlow(flow, stepVolume, isFirstPackage);
            actualPercentageOfTarget = patient.actualPercentageOfTargetWithFlow(flow, stepVolume, isFirstPackage);
            handleUpdateTest.post(runUpdateTest);
        }

        @Override
        public void resultsUpdated(ResultsPefFev1 resultsPefFev1) {
            Log.d("Results PEF ", String.valueOf(resultsPefFev1.getPef_cLs() / (float) 100) + " L/s");
            result = "PEF: " + String.valueOf(resultsPefFev1.getPef_cLs() * 60 / (float) 100) + " L/m" +
                    " | FEV1: " + String.valueOf(resultsPefFev1.getFev1_cL() / (float) 100) + " L"+
                    " \nPefTime: " + String.valueOf(resultsPefFev1.getPefTime_msec()) + " ms"+
                    " | Evol: " + String.valueOf(resultsPefFev1.geteVol_mL() ) + " ml";
            qualityRawCode= resultsPefFev1.getQualityCode();
            qualityMsgCode = patient.getQualityMessage(resultsPefFev1);
            handleUpdateResult.post(runUpdateResult);

            String pef = String.valueOf(resultsPefFev1.getPef_cLs() * 60 / (float) 100);
            String fev1 = String.valueOf(resultsPefFev1.getFev1_cL() / (float) 100);
            String peftime = String.valueOf(resultsPefFev1.getPefTime_msec());
            String  evol = String.valueOf(resultsPefFev1.geteVol_mL() );

            Log.d(TAG, "1111resultsUpdated for peffev1: " + qualityMsgCode);


            upload_PefFev1(pef, fev1, peftime, evol);

        }
        @Override
        public void resultsUpdated(ResultsFvc resultsFvc){
            result = "PEF: " + String.valueOf(resultsFvc.getPef_cLs() * 60 / (float) 100) + " L/m" +
                    " | FEV1: " + String.valueOf(resultsFvc.getFev1_cL() / (float) 100) + " L"+
                    " \nFVC: " + String.valueOf(resultsFvc.getFvc_cL() / (float) 100) + " L"+
                    " | FEV1/FVC: " + String.valueOf(Math.round(resultsFvc.getFev1_Fvc_pcnt() * 100)/(float)100) + "%" +
                    " \nFEV6: " + String.valueOf(resultsFvc.getFev6_cl() / (float) 100) + " L"+
                    " | FEF2575: " + String.valueOf(resultsFvc.getFef2575_cLs()  / (float) 100) + " L/s";
            qualityRawCode = resultsFvc.getQualityCode();
            qualityMsgCode = patient.getQualityMessage(resultsFvc);
            handleUpdateResult.post(runUpdateResult);

            String pef = String.valueOf(resultsFvc.getPef_cLs() * 60 / (float) 100);
            String fev1 = String.valueOf(resultsFvc.getFev1_cL() / (float) 100);
            String fvc = String.valueOf(resultsFvc.getFvc_cL() / (float) 100);
            String fev1_fvc = String.valueOf(Math.round(resultsFvc.getFev1_Fvc_pcnt() * 100)/(float)100);
            String fev6 = String.valueOf(resultsFvc.getFev6_cl() / (float) 100);
            String fef2575 = String.valueOf(resultsFvc.getFef2575_cLs()  / (float) 100);

            Log.d(TAG, "resultsUpdated for fvc: " + pef + fvc);
            Log.d(TAG, "1111resultsUpdated for peffev1: " + qualityMsgCode);
            Log.d(TAG, "1111resultsUpdated for peffev1: " + qualityMsgCode);


           // currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)40);

               upload_FVC(pef, fev1, fvc, fev1_fvc, fev6, fef2575);

        }

        @Override
        public void testRestarted(Device device) {
            Log.d(TAG, "a restart");
            volume=0;
            currDevice.stopTest(getApplicationContext());
            predictedPercentageOfTarget = (float) 1;
            actualPercentageOfTarget = (float) 1;
            handleUpdateTest.post(runUpdateTest);
            Log.d("TEST RESTARTED","TRUE");
        }

        @Override
        public void testStopped(Device device) {
            volume=0;
            Log.d(TAG, "a stopped");
            //currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
            predictedPercentageOfTarget = (float) 1;
            actualPercentageOfTarget = (float) 1;
            handleUpdateTest.post(runUpdateTest);
            infoList.add("Test Stopped " + device.getDeviceInfo().getAdvertisementDataName());
            handleUpdateInfo.post(runUpdateInfo);
           // currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)40);
        }

        @Override
        public void softwareUpdateProgress(float progress, Device.UpdateStatus status, String error) {
            strProgress = status.toString() + " " + Float.toString(progress) + "% " + error;
            if (status!= Device.UpdateStatus.UpdateInProgress) {
                strProgress += " sec.: " + (System.currentTimeMillis()-startUpdate)/1000;
            }
            handleSoftwareUpdateProgress.post(runSoftwareUpdateProgress);
        }
    };

    Handler handleUpdateInfo = new Handler();
    Runnable runUpdateInfo = new Runnable() {
        @Override
        public void run() {
            String localInfo = "";
            if (infoList.size()>0 ) {
                localInfo = infoList.get(0);
                infoList.remove(0);
            }
            if (localInfo.equals("devConnected")) {
                localInfo = currDevice.getDeviceInfo().getAdvertisementDataName() + " Connected" + "\n" +
                        "BT version " + currDevice.getBluetoothVersion() + "\n" +
                        "SW version " + currDevice.getSoftwareVersion() + "\n" +
                        "Battery " + currDevice.getBatteryLevel(getApplicationContext()) + "%" + "\n" +
                        "Volume Step " + currDevice.getVolumeStep();
                findViewById(R.id.btnStartFvc).setEnabled(true);
                findViewById(R.id.btnStartPefFev1).setEnabled(true);
            }
            if (infoDisconnect.equals(""))
                fullInfo = localInfo + "\n\n" + fullInfo;
            else {
                fullInfo = infoDisconnect + "\n\n" + fullInfo;
                infoDisconnect = "";
                findViewById(R.id.btnStartFvc).setEnabled(true);
                findViewById(R.id.btnStartPefFev1).setEnabled(true);
            }
            if (localInfo.matches("Test Stopped.*")) {
                findViewById(R.id.btnStartFvc).setEnabled(true);
                findViewById(R.id.btnStartPefFev1).setEnabled(true);
            }
            ((TextView) findViewById(R.id.tvInfo)).setText(fullInfo);
            findViewById(R.id.tvInfo).scrollTo(0, 0);
        }
    };

    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {

            deviceInfoArray.add(discoveredDeviceInfo);// I probably don't need this
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
            handleUpdateInfo.post(runUpdateInfo);
        } //+++
    }; //여기에다가 listview를 에드하는듯 하다.

    Handler handleUpdateTest = new Handler();
    Runnable runUpdateTest = new Runnable() {
        @Override
        public void run() {
            ((TextView) findViewById(R.id.tvTarget)).setText(String.valueOf(Math.round(predictedPercentageOfTarget))+"%");
            findViewById(R.id.imgTarget).getLayoutParams().height = Math.round(200*(predictedPercentageOfTarget/(float)100)*getResources().getDisplayMetrics().density);
            findViewById(R.id.imgTarget).requestLayout();
            ((TextView) findViewById(R.id.tvActual)).setText(String.valueOf(Math.round(actualPercentageOfTarget))+"%");
            findViewById(R.id.imgActual).getLayoutParams().height = Math.round(200*(actualPercentageOfTarget/(float)100)*getResources().getDisplayMetrics().density);
            findViewById(R.id.imgActual).requestLayout();
        }
    };

    Handler handleUpdateResult = new Handler();
    Runnable runUpdateResult = new Runnable() {
                @Override
                public void run() {
                    String qualityMsgString = "";
            switch (qualityMsgCode) {
                case Patient.QualityMessageAvoidCoughing:
                    qualityMsgString = "Avoid Coughing";
                    Log.d(TAG, "aaaa---a");
                    break;
                case Patient.QualityMessageBlowOutFaster:
                    qualityMsgString = "Blow Out Faster";
                    Log.d(TAG, "bbbb---b");

                    break;
                case Patient.QualityMessageDontEsitate:
                    qualityMsgString = "Don't Esitate";
                    Log.d(TAG, "cccc---c");

                    break;
                case Patient.QualityMessageDontStartTooEarly:
                    qualityMsgString = "Don't Start Too Early";
                    Log.d(TAG, "dddd---d");

                    break;
                case Patient.QualityMessageGoodBlow:
                    qualityMsgString = "Good Blow";
                    Log.d(TAG, "eeee---e");

                    break;
                case Patient.QualityMessageBlowOutLonger:
                    qualityMsgString = "Blow Out Longer";
                    Log.d(TAG, "ffff---f");

                    break;
                case Patient.QualityMessageAbruptEnd:
                    qualityMsgString = "Abrupt End";
                    Log.d(TAG, "gggg---g");

                    break;
            }
            ((TextView) findViewById(R.id.tvResult)).setText(result);
            ((TextView) findViewById(R.id.tvQualityMsg)).setText(Integer.toHexString(qualityRawCode) + " " + qualityMsgString);

        }
    };

    Handler handleSoftwareUpdateProgress = new Handler();
    Runnable runSoftwareUpdateProgress = new Runnable() {
        @Override
        public void run() {
            ((TextView) findViewById(R.id.tvResult)).setText(strProgress);
            ((TextView) findViewById(R.id.tvQualityMsg)).setText("");
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

    void upload_FVC(final String pef, final String fev1, final String fvc, final String fev1_fvc, final String fev6, final String fef2575) {
        // Tag used to cancel the request
        String tag_string_req = "req_response";
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_FVC_UPLOAD, new Response.Listener<String>() {
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
                params.put("fvc", fvc);
                params.put("fev1_fvc", fev1_fvc);
                params.put("fev6", fev6);
                params.put("fef2575", fef2575);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
