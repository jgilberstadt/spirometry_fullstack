package com.spirometry.spirobanksmartsdksample;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import java.io.Serializable;
import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity implements Serializable  {

    Button submitButton;
    EditText etPassword;
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

   /* public boolean isValidPassword(final String patientPassword) {

        Pattern patternCheck;
        Matcher matcherCheck;

        final String PATIENT_PASSWORD_PATTERN = "^(123456)$";

        patternCheck = Pattern.compile(PATIENT_PASSWORD_PATTERN);
        matcherCheck = patternCheck.matcher(patientPassword);

        return matcherCheck.matches();

    } */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mBundleData = new MyParcelable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        submitButton = (Button) findViewById(R.id.submitButton);
        etPassword = (EditText) findViewById(R.id.etPassword);

        //get device manager istance
        deviceManager = DeviceManager.getInstance(this);

        //set device manger callback
        deviceManager.setDeviceManagerCallback(deviceManagerCallback);


        //peter: this will look for specific user's device, at our case, it is Z008182
        deviceManager.startDiscovery(LoginActivity.this);


        //we want to create a login request, when the user actually clicks the login button, so onClickListener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int typedPassword = Integer.parseInt(etPassword.getText().toString());

               // if(isValidPassword(etPassword.getText().toString().trim())){

                if(truePassword.equals(etPassword.getText().toString())) {

                    Intent intent = new Intent(LoginActivity.this, ConnectingActivity.class);
                    intent.putExtra("bundle-data", mBundleData);
                    //intent.putExtra("BlueTooth Connect Info", (Parcelable) discoveredDeviceInfo);

                    Log.d(TAG, "Oppa?: ");
                    LoginActivity.this.startActivity(intent);

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

  /* DeviceCallback deviceCallback = new DeviceCallback() {
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
            String evol = String.valueOf(resultsPefFev1.geteVol_mL() );

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

            upload_FVC(pef, fev1, fvc, fev1_fvc, fev6, fef2575);

        }

        @Override
        public void testRestarted(Device device) {
            volume=0;
            predictedPercentageOfTarget = (float) 1;
            actualPercentageOfTarget = (float) 1;
            handleUpdateTest.post(runUpdateTest);
            Log.d("TEST RESTARTED","TRUE");
        }

        @Override
        public void testStopped(Device device) {
            volume=0;
            predictedPercentageOfTarget = (float) 1;
            actualPercentageOfTarget = (float) 1;
            handleUpdateTest.post(runUpdateTest);
            infoList.add("Test Stopped " + device.getDeviceInfo().getAdvertisementDataName());
            handleUpdateInfo.post(runUpdateInfo);
        }

        @Override
        public void softwareUpdateProgress(float progress, Device.UpdateStatus status, String error) {
            strProgress = status.toString() + " " + Float.toString(progress) + "% " + error;
            if (status!= Device.UpdateStatus.UpdateInProgress) {
                strProgress += " sec.: " + (System.currentTimeMillis()-startUpdate)/1000;
            }
            handleSoftwareUpdateProgress.post(runSoftwareUpdateProgress);
        }
    }; */
/*
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
    }; */

    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {

        //   deviceInfoArray.add(discoveredDeviceInfo); // I need this for sure
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
          //  handleUpdateInfo.post(runUpdateInfo);
        } //+++
    }; //여기에다가 listview를 에드하는듯 하다.

 /*   Handler handleUpdateTest = new Handler();
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
    };*/

 /*   Handler handleUpdateResult = new Handler();
    Runnable runUpdateResult = new Runnable() {
        @Override
        public void run() {
            String qualityMsgString = "";
            switch (qualityMsgCode) {
                case Patient.QualityMessageAvoidCoughing:
                    qualityMsgString = "Avoid Coughing";
                    break;
                case Patient.QualityMessageBlowOutFaster:
                    qualityMsgString = "Blow Out Faster";
                    break;
                case Patient.QualityMessageDontEsitate:
                    qualityMsgString = "Don't Esitate";
                    break;
                case Patient.QualityMessageDontStartTooEarly:
                    qualityMsgString = "Don't Start Too Early";
                    break;
                case Patient.QualityMessageGoodBlow:
                    qualityMsgString = "Good Blow";
                    break;
                case Patient.QualityMessageBlowOutLonger:
                    qualityMsgString = "Blow Out Longer";
                    break;
                case Patient.QualityMessageAbruptEnd:
                    qualityMsgString = "Abrupt End";
                    break;
            }
            ((TextView) findViewById(R.id.tvResult)).setText(result);
            ((TextView) findViewById(R.id.tvQualityMsg)).setText(Integer.toHexString(qualityRawCode) + " " + qualityMsgString);
        }
    };*/

  /*  Handler handleSoftwareUpdateProgress = new Handler();
    Runnable runSoftwareUpdateProgress = new Runnable() {
        @Override
        public void run() {
            ((TextView) findViewById(R.id.tvResult)).setText(strProgress);
            ((TextView) findViewById(R.id.tvQualityMsg)).setText("");
        }
    }; */
}
