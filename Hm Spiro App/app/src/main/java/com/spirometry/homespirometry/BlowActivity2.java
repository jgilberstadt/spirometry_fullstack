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
import com.spirometry.homespirometry.PulseInstructionActivity;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import shared.STTrace;
import terminalIO.TIOManager;
import terminalIO.TIOPeripheral;
import terminalIO.TIOPeripheralCallback;

public class BlowActivity2 extends AppCompatActivity implements TIOPeripheralCallback {

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
    TIOPeripheral _peripheral;

    //This is a MyParcelable object that contains data / objects to be passed between activities
    private MyParcelable mBundleData;

    Patient patient;

    Context myContext;
    final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    DeviceInfo discoveredDeviceInfo;

    int numBlows = 0;
    int value = numBlows + 1;
    private int messageNumber = 7;
    private int messageNumberFvc = 6;

    private String patient_id = "000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mBundleData = getIntent().getParcelableExtra("bundle-data");
        String peripheralAddress = getIntent().getStringExtra("peripheralAddress");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blow);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _peripheral = TIOManager.sharedInstance().findPeripheralByAddress(peripheralAddress);
        _peripheral.setListener(this);
        Log.d("hyunrae", peripheralAddress);

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
                    //currDevice.startTest(getApplicationContext(), Device.TestType.Fvc,(byte)50);
                    //currDevice.startTest(getApplicationContext(), Device.TestType.Fvc);

                    buttonReBlow.setVisibility(View.INVISIBLE);
                    blowDirection.setVisibility(View.VISIBLE);
                }else{
                    //currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
                    buttonReBlow.setVisibility(View.INVISIBLE);
                    blowDirection.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    //******************************************************************************
    // TIOPeripheral implementation
    //******************************************************************************


    @Override
    public void tioPeripheralDidConnect(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralDidConnect");

        if (!this._peripheral.shallBeSaved()) {
            // save if connected for the first time
            this._peripheral.setShallBeSaved(true);
            TIOManager.sharedInstance().savePeripherals();

            Log.d("bro hyunrae", "CONNECTED");
            Intent intent = new Intent(BlowActivity2.this, SpirometerInstructionActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            startActivity(intent);
        }
    }

    @Override
    public void tioPeripheralDidFailToConnect(TIOPeripheral peripheral, String errorMessage) {
        Log.d("hyunrae", "FAIL TO CONNECT");
        STTrace.method("tioPeripheralDidFailToConnect", errorMessage);
    }

    @Override
    public void tioPeripheralDidDisconnect(TIOPeripheral peripheral, String errorMessage) {
        STTrace.method("tioPeripheralDidDisconnect", errorMessage);
        Log.d("hyunrae", "disconnected");
        //numBlows--;
    }

    @Override
    public void tioPeripheralDidReceiveUARTData(TIOPeripheral peripheral, byte[] data) {
        try {
            handlerVisibilityChange.post(runVisibilityChange);
            numBlows++;
            Log.d("hyunrae", "peter " );
            messageNumber--;
            handlerTextViewNumberChange.post(runTextViewNumberChange);
            handlerVisibilityChangeTwoWaitOneSecond.post(runVisibilityChangeTwoWaitOneSecond);
            String text = new String(data, "CP-1252");
            Log.d("hyunrae", "text "  + text);
        } catch (Exception e) {
            Log.d("hyunrae","nah" + e.toString());
        }

    }

    @Override
    public void tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral peripheral, int bytesWritten) {
        STTrace.method("tioPeripheralDidWriteNumberOfUARTBytes", Integer.toString(bytesWritten));
        Log.d(TAG, "aaaaa");

    }

    @Override
    public void tioPeripheralUARTWriteBufferEmpty(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralUARTWriteBufferEmpty");
        Log.d(TAG, "bbbbb");

    }

    @Override
    public void tioPeripheralDidUpdateAdvertisement(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralDidUpdateAdvertisement");
        Log.d(TAG, "ccccc");

    }

    @Override
    public void tioPeripheralDidUpdateRSSI(TIOPeripheral peripheral, int rssi) {
        STTrace.method("tioPeripheralDidUpdateRSSI", Integer.toString(rssi));
        Log.d(TAG, "ddddd");

    }

    @Override
    public void tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
        STTrace.method("tioPeripheralDidUpdateLocalUARTCreditsCount", Integer.toString(creditsCount));

        Log.d(TAG, "eeeee");

    }

    @Override
    public void tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
        STTrace.method("tioPeripheralDidUpdateRemoteUARTCreditsCount", Integer.toString(creditsCount));
        Log.d(TAG, "fffff");


    }

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

    Handler handleUpdateListScan = new Handler();
    Runnable runUpdateListScan = new Runnable() {
        @Override
        public void run() {
            deviceManager.connect(getApplicationContext(), discoveredDeviceInfo);
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

    Handler handleIntentToTestComplete = new Handler();
    Runnable runIntentToTestComplete= new Runnable() {
        @Override
        public void run() {
            deviceManager.disconnect();
            Intent intent = new Intent(BlowActivity2.this, PulseInstructionActivity.class); //PulseConnectingActivity
            //  Intent intent = new Intent(BlowActivity.this, PulseConnectingActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            BlowActivity2.this.startActivity(intent);
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
                    // currDevice.startTest(getApplicationContext(), Device.TestType.Fvc, (byte)50);
                    //currDevice.startTest(getApplicationContext(), Device.TestType.Fvc);
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
                    //currDevice.startTest(getApplicationContext(), Device.TestType.PefFev1);
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
            }, 2000);        }
    };

    public void onClickHelp(View view) {
        Intent intent = new Intent(BlowActivity2.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

}