package com.spirometry.homespirometry;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.spirometry.spirobanksmartsdk.Device;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;
import com.spirometry.spirobanksmartsdk.DeviceManagerCallback;
import com.spirometry.homespirometry.R;

import java.util.ArrayList;

import shared.STTrace;
import terminalIO.TIOAdvertisement;
import terminalIO.TIOManager;
import terminalIO.TIOManagerCallback;
import terminalIO.TIOPeripheral;
import terminalIO.TIOPeripheralCallback;

public class SpirometerConnectingActivity2 extends Activity implements TIOManagerCallback, TIOPeripheralCallback {

    private MyParcelable mBundleData;
    String success = "Success!";
    private static final String TAG = SpirometerConnectingActivity.class.getSimpleName();
    ArrayList<String> arr;
    DeviceInfo selectedDeviceInfo;
    TextView tvConnecting;
    ProgressBar progressBar;
    TextView directionTextView;
    Button tryAgainButton;

    Boolean localInfo = false;
    int numberOfDisconnect = 0;
    BluetoothAdapter bluetoothadapter;
    Intent bluetoothIntent;

    BluetoothDevice _device;
    TIOPeripheral _peripheral;


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

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just

        //arr = mBundleData.getDeviceInfo();

        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothadapter.isEnabled()) {
            bluetoothEnableRequest();
            return;
        }

        // ------------------------ //
        TIOManager.initialize(this.getApplicationContext());
        TIOManager.sharedInstance().setListener(this);
        TIOManager.sharedInstance().startScan();

        // ----------------------- //

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
                Log.d(TAG, "Start Discovery!33333");
                progressBar.setVisibility(View.VISIBLE);
                tvConnecting.setVisibility(View.VISIBLE);
                directionTextView.setVisibility(View.INVISIBLE);
                tryAgainButton.setVisibility(View.INVISIBLE);
                TIOManager.sharedInstance().startScan();
                Log.d("hyunrae", "start scan");
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
                TIOManager.sharedInstance().stopScan();
                Log.d(TAG, "Start Discovery!12222");
                progressBar.setVisibility(View.VISIBLE);
                tvConnecting.setVisibility(View.VISIBLE);
                directionTextView.setVisibility(View.INVISIBLE);
                tryAgainButton.setVisibility(View.INVISIBLE);
                TIOManager.sharedInstance().startScan();
                handlerWait.post(runWait);
                tryAgainButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            bluetoothEnableRequest();
                            return;
                        }
                        TIOManager.sharedInstance().stopScan();
                        Log.d(TAG, "Start Discovery!11111");
                        progressBar.setVisibility(View.VISIBLE);
                        tvConnecting.setVisibility(View.VISIBLE);
                        directionTextView.setVisibility(View.INVISIBLE);
                        tryAgainButton.setVisibility(View.INVISIBLE);
                        TIOManager.sharedInstance().startScan();
                        handlerWait.post(runWait);
                    }
                });
            }
        });
    }


    Handler handleUpdateInfo = new Handler();
    Runnable runUpdateInfo = new Runnable() {
        @Override
        public void run() {
            TIOManager.sharedInstance().stopScan();
            Intent intent = new Intent(SpirometerConnectingActivity2.this, SpirometerInstructionActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            SpirometerConnectingActivity2.this.startActivity(intent);
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
                    TIOManager.sharedInstance().stopScan();
                    if(localInfo == true){
                        Log.d(TAG, "the device is connect, nothing to show");
                        handleUpdateInfo.post(runUpdateInfo);
                    }else {
                        localInfo = false;
                        TIOManager.sharedInstance().stopScan();
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
            }, 9000);
        }
    };

    public void onClickHelp(View view) {
        Intent intent = new Intent(SpirometerConnectingActivity2.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }


    //******************************************************************************
    // TIOManagerCallback implementation
    //******************************************************************************

    @Override
    public void tioManagerDidDiscoverPeripheral(TIOPeripheral peripheral) {
        this._peripheral = peripheral;
        this._peripheral.setShallBeSaved(false);
        TIOManager.sharedInstance().savePeripherals();
        Log.d("hyunrae", peripheral.toString());
        if (this._peripheral.isConnected()) {

        } else {
            this._peripheral.setListener(this);
            Log.d("hyunrae", this._peripheral.getAddress());
            this._peripheral.connect();
        }

    }

    @Override
    public void tioManagerDidUpdatePeripheral(TIOPeripheral peripheral) {
        Log.d("hyunrae", "when is this run");
        this._peripheral = peripheral;
        this._peripheral.setShallBeSaved(false);
        TIOManager.sharedInstance().savePeripherals();
        Log.d("hyunrae", peripheral.toString());
        if (this._peripheral.isConnected()) {

        } else {
            this._peripheral.setListener(this);
            Log.d("hyunrae", this._peripheral.getAddress());
            this._peripheral.connect();
        }

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

            Log.d("hyunrae", "CONNECTED");
            Intent intent = new Intent(SpirometerConnectingActivity2.this, SpirometerInstructionActivity.class);
            intent.putExtra("peripheralAddress", this._peripheral.getAddress());
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
    }

    @Override
    public void tioPeripheralDidReceiveUARTData(TIOPeripheral peripheral, byte[] data) {

    }

    @Override
    public void tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral peripheral, int bytesWritten) {
        STTrace.method("tioPeripheralDidWriteNumberOfUARTBytes", Integer.toString(bytesWritten));

    }

    @Override
    public void tioPeripheralUARTWriteBufferEmpty(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralUARTWriteBufferEmpty");

    }

    @Override
    public void tioPeripheralDidUpdateAdvertisement(TIOPeripheral peripheral) {
        STTrace.method("tioPeripheralDidUpdateAdvertisement");

    }

    @Override
    public void tioPeripheralDidUpdateRSSI(TIOPeripheral peripheral, int rssi) {
        STTrace.method("tioPeripheralDidUpdateRSSI", Integer.toString(rssi));
        }

    @Override
    public void tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
        STTrace.method("tioPeripheralDidUpdateLocalUARTCreditsCount", Integer.toString(creditsCount));
        }

    @Override
    public void tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
        STTrace.method("tioPeripheralDidUpdateRemoteUARTCreditsCount", Integer.toString(creditsCount));

    }

}
