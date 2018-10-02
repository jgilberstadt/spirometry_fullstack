package com.spirometry.homespirometry;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;

import java.io.Serializable;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements Serializable {

    Button submitButton;
    EditText etPassword;
    ImageView spirometerImage;
    TextView spiroCheck;
    TextView contactHospital;
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

   // NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    DeviceManager deviceManager;
    DeviceInfo discoveredDeviceInfo;

    private static final String TAG = LoginActivity.class.getSimpleName();
    //This is a MyParcelable object that contains data / objects to be passed between activities
    private MyParcelable mBundleData;

    private long mLastClickTime = 0;
    private Dialog warningDialog;
    private Dialog datePicker;
    int correctPasswordCheck = 0;

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
        contactHospital = (TextView) findViewById(R.id.contactHospital);

        contactHospital.setVisibility(View.INVISIBLE);

//        new CountDownTimer(7000,1000){
//            @Override
//            public void onTick(long millisUntilFinished){
//                submitButton.setVisibility(View.INVISIBLE);
//                etPassword.setVisibility(View.INVISIBLE);
//                spirometerImage.setVisibility(View.VISIBLE);
//                spiroCheck.setVisibility(View.VISIBLE);
//                //spiroProgressBar.setVisibility(View.VISIBLE);
//                }
//            @Override
//            public void onFinish(){
//                //set the new Content of your activity
//                submitButton.setVisibility(View.VISIBLE);
//                etPassword.setVisibility(View.VISIBLE);
//                spirometerImage.setVisibility(View.GONE);
//                //spiroCheck.setVisibility(View.GONE);
//                //spiroProgressBar.setVisibility(View.GONE);
//            }
//        }.start();


        //we want to create a login request, when the user actually clicks the login button, so onClickListener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnectedViaWifi()) {
                    buildDialog(LoginActivity.this).show();
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();

                if(truePassword.equals(etPassword.getText().toString())) {
                    // do stuff
                    //Intent intent = new Intent(LoginActivity.this, ApplicationChooseActivity.class);
                    Intent intent = new Intent(LoginActivity.this, SpirometerConnectingActivity2.class);
                    Log.d(TAG, "bundle-data" +mBundleData);
                    intent.putExtra("bundle-data", mBundleData);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                    correctPasswordCheck++;
                    if(correctPasswordCheck >=4){
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                        contactHospital.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
    }

//    //This Call back need for new Android M runtime authorization
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_COARSE_LOCATION: {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    deviceManager.startDiscovery(this);
//                } else {
//                    Snackbar.make(findViewById(R.id.main_layout), "Can't scan without Location authorization on Android M", Snackbar.LENGTH_LONG).show();
//                }
//            }
//        }
//    }

    // check whether WiFi is connected
    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return mWifi.isConnected() || mobile.isConnected();
    }

    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {

        //   deviceInfoArray.add(discoveredDeviceInfo); // I need this for sure
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
          //  handleUpdateInfo.post(runUpdateInfo);
        } //+++
    };

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Not Connected");
        builder.setMessage("You need to connect to Wi-Fi or turn on cellular data to proceed");
        builder.setCancelable(false);

        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(settingsIntent);
            }
        });
        return builder;

    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(LoginActivity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

}
