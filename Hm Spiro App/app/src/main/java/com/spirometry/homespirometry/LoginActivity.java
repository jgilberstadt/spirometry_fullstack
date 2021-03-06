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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.homespirometry.classes.SuperActivity;
import com.spirometry.homespirometry.classes.UrlConfig;
import com.spirometry.spirobanksmartsdk.DeviceInfo;
import com.spirometry.spirobanksmartsdk.DeviceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends SuperActivity {

    Button submitButton;
    EditText patientIdView;
    ImageView spirometerImage;
    TextView spiroCheck;
    TextView contactHospital;
    TextView titleTextView;
    ProgressBar spiroProgressBar;
    DeviceManager deviceManager;
    DeviceInfo discoveredDeviceInfo;

    private static final String TAG = LoginActivity.class.getSimpleName();
    //This is a MyParcelable object that contains data / objects to be passed between activities
    private NewParcelable mBundleData;
    private long mLastClickTime = 0;
    private Dialog warningDialog;
    private Dialog datePicker;
    int correctPasswordCheck = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mBundleData = new NewParcelable();
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        resources = this.getResources();
        submitButton = (Button) findViewById(R.id.submitButton);
        patientIdView = (EditText) findViewById(R.id.etPassword);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        spirometerImage = (ImageView) findViewById(R.id.spirometerImage);
        int imageResource = getResources().getIdentifier("@drawable/spiro", null, this.getPackageName());
        spirometerImage.setImageResource(imageResource);
        spiroCheck = (TextView) findViewById(R.id.spiroCheck);
        spiroProgressBar = (ProgressBar) findViewById(R.id.spiroProgressBar);
        contactHospital = (TextView) findViewById(R.id.contactHospital);

        contactHospital.setVisibility(View.INVISIBLE);

        if(isConnectedViaWifi()){
            File dir = getFilesDir();
            uploadPastFiles(dir);
        }

        //we want to create a login request, when the user actually clicks the login button, so onClickListener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnectedViaWifiOrMobile()) {
                    buildDialog(LoginActivity.this).show();
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();
                spiroProgressBar.setVisibility(View.VISIBLE);
                sendPatientId(patientIdView);
            }
        });
    }

    private void sendPatientId(final EditText patientIdView) {
        final String patientId = patientIdView.getText().toString();
        Log.d(TAG, patientId);
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_CHECK_PATIENT_EXIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("customDebug", "Login Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error) {
                        showWrongPasswordToast(patientIdView);
                    } else {
                        try {
                            String normal_range = jObj.getJSONObject("user").getString("normal_range");
                            if (normal_range != "null") {
                                Log.d(TAG, "normal range: "+normal_range);
                                Log.d(TAG, "is patient invalid: " + error);
                                String[] minMaxRanges = normal_range.split(",");
                                mBundleData.setMinNRange(Float.valueOf(minMaxRanges[0]));
                                newBundleData.setMinNRange(Float.valueOf(minMaxRanges[0]));
                                mBundleData.setMaxNRange(Float.valueOf(minMaxRanges[1]));
                                newBundleData.setMaxNRange(Float.valueOf(minMaxRanges[1]));
                                Log.d(TAG, "normal range: " + minMaxRanges[0] + ", " + minMaxRanges[1]);
                            }

                            // set patient id and mode
                            mBundleData.setPatientId(patientIdView.getText().toString());
                            newBundleData.setPatientId(patientIdView.getText().toString());
                            String mode = jObj.getJSONObject("user").getString("mode");
                            mBundleData.setMode(Integer.valueOf(mode));

                            //newBundleData is a static mBundle inherited from super class. This is in progress to
                            //gradally replace mBundleData. This must be set to ensure the title bar display the correct mode
                            newBundleData.setMode(Integer.valueOf(mode));
                            Intent intent = new Intent(LoginActivity.this, ApplicationChooseActivity.class);
                            intent.putExtra("bundle-data", mBundleData);
                            startActivity(intent);
                        } catch (Exception e) {
                            showWrongPasswordToast(patientIdView);
                        }
                    }
                } catch (JSONException e) {
                    // JSON error
                    showWrongPasswordToast(patientIdView);
                    e.printStackTrace();
                    Log.d(TAG, "JSON Exception:" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                showWrongPasswordToast(patientIdView);
                Log.e(TAG, "Login Error: " + error.getMessage());
            }
        }){
            // Posting parameters to login url
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("imei_num", patientId);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, "patientIdRequest");
        spiroProgressBar.setVisibility(View.INVISIBLE);

    }

    private void showWrongPasswordToast(EditText patientIdView) {
        Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "wrong");
        correctPasswordCheck++;
        if(correctPasswordCheck >=4){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(patientIdView.getWindowToken(), 0);
            contactHospital.setVisibility(View.VISIBLE);
        }
    }


    private void uploadPastFiles(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                final File file = files[i];
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkSurvey(file.getPath(), file.getName(), "http://ecp-app-hsdev1.nrg.wustl.edu/spirometry/store_data_plain.php");
                    }
                }).start();
            }
        }
    }

    public void checkSurvey(final String selectedFilePath, final String file_name, final String php_address) {


        String tag_string_req = "req_confirm";

        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_CHECK_FILE_EXIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "confirmSurvey Response: " + response);
                if (response.equals("true")) {
                    // database contains survey
                    Log.d(TAG, "exists YES");
                    File file = new File(selectedFilePath, file_name);
                    file.delete();
                } else {
                    Log.d(TAG, "exists NO");

                    // database does not contain survey
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendFile(selectedFilePath, file_name, php_address);
                        }
                    }).start();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "checkSurveyExists Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("file_name", file_name);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    public int sendFile(String selectedFilePath, String file_name, String php_address) {
        Log.d("hyunrae", selectedFilePath);

        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath, file_name);

        if (!selectedFile.isFile()) {
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(php_address);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();

                //See how large buffer do we allocate for the file
                Log.d("Buffer size", Integer.toString(bytesAvailable));

                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer responseMsg = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    responseMsg.append(inputLine);
                }

                Log.d("hyunrae", responseMsg.toString());

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200 && isConnectedViaWifi()) {
                    Log.d("Upload Debug", "File Upload completed.\n\n You can see the uploaded file here: \n\n");
                    //TODO: check and delete only if this isn't an unenrolled baseline survey
//                    selectedFile.delete();
                } else {
                    AlertDialog.Builder warning = new AlertDialog.Builder(LoginActivity.this);
                    warning.setMessage("Unable to Upload Answers, Please Check Internet Connection").setTitle("Error");
                    AlertDialog warning_dialog = warning.create();
                    warning_dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    warning_dialog.show();

                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "server response code: "+serverResponseCode);
            return serverResponseCode;
        }
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
    private boolean isConnectedViaWifiOrMobile() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return mWifi.isConnected() || mobile.isConnected();
    }

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
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
