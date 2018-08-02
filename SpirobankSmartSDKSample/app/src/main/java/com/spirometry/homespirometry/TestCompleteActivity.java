package com.spirometry.homespirometry;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.DeflaterOutputStream;

public class TestCompleteActivity extends AppCompatActivity {

    String[][] arraya; //6 data storing 4 String Values; +-
    private MyParcelable mBundleData;
    private static final String TAG = BlowActivity.class.getSimpleName();
    TextView nextAppointment;
    Button changeAppointment;
    TextView dateRepresent;
    TextView timeRepresent;
    Button dateButton;
    Calendar finalDate =Calendar.getInstance();
    TimePicker timePicker;
    DatePicker datePicker;

    private AlarmManager m_alarmMgr;

    File file;

    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    int[] hours = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);
        mBundleData = getIntent().getParcelableExtra("bundle-data");
        Log.d("hyunrae", Arrays.toString(mBundleData.getSurveyAnswerArr()));

        nextAppointment = (TextView) findViewById(R.id.nextAppointment);
        dateRepresent = (TextView) findViewById(R.id.dateRepresent);
        timeRepresent = (TextView) findViewById(R.id.timeRepresent);
        dateButton = (Button) findViewById(R.id.dateButton);
        changeAppointment = (Button) findViewById(R.id.changeAppointment);

        //Get current date time with Calendar()
        Calendar weekAddedCal = Calendar.getInstance();
        weekAddedCal.add(Calendar.DATE, 7); // Adding a week to current time to set a appointment date

        int minutes = weekAddedCal.get(Calendar.MINUTE);
        int mod = minutes % 15;
        weekAddedCal.add(Calendar.MINUTE, mod < 8 ? -mod : (15-mod));

        Log.d("minutes", Integer.toString(minutes));

        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = df.format(weekAddedCal.getTime());
        dateRepresent.setText(formattedDate);

        SimpleDateFormat formatHour = new SimpleDateFormat("hh:mm");
        String formattedTime = formatHour.format(weekAddedCal.getTime());

        if (formattedTime.charAt(0) == '0') {
            formattedTime = formattedTime.substring(1);
        }

        if(weekAddedCal.get(Calendar.HOUR_OF_DAY) < 13) {
            timeRepresent.setText(formattedTime + " " + "AM");
        }else{
            timeRepresent.setText(formattedTime + " " + "PM");
        }

        finalDate = weekAddedCal;

        Log.d(TAG, "finalDate Initial " + finalDate);

        changeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(TestCompleteActivity.this, R.style.Theme_Dialog);
                dialog.setContentView(R.layout.date_time_picker);

                dialog.findViewById(R.id.cancelBtn).setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
                datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
                datePicker.setMinDate(System.currentTimeMillis()-1000);

                dialog.findViewById(R.id.confirmBtn).setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dateRepresent.setText(months[datePicker.getMonth()] + " " + datePicker.getDayOfMonth() + ", " + datePicker.getYear());
                        String AM_PM;
                        if (timePicker.getCurrentHour() < 12) {
                            AM_PM = "AM";
                        } else {
                            AM_PM = "PM";
                        }
                        int minute = timePicker.getCurrentMinute();
                        String minuteString;
                        if (minute < 10) {
                            minuteString = "0" + minute;
                        } else { minuteString = Integer.toString(minute); }
                        timeRepresent.setText(hours[timePicker.getCurrentHour()] + ":" + minuteString + " " + AM_PM);
                        finalDate.clear();
                        finalDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlarm(true);
                Intent intent = new Intent(TestCompleteActivity.this, FinalPageActivity.class);
                //intent.putExtra("bundle-data", mBundleData);
                TestCompleteActivity.this.startActivity(intent);
                finish();
            }
        });
        createFile();
    }

    public void createFile() {
        File file_path = getFilesDir();
        String file_name = getManufacturerSerialNumber();

        file = new File(file_path, file_name);

        String[][] blow_arr = mBundleData.getBlowDataArray();

        LinkedList<String[]> pulse_list = mBundleData.getPulseData();
        ListIterator<String[]> it = pulse_list.listIterator();

        int[] survey_arr = mBundleData.getSurveyAnswerArr();

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            DeflaterOutputStream dOut = new DeflaterOutputStream(fOut);

            String line = "";
            for (int i = 0; i < survey_arr.length; i++) {
                line += survey_arr[i];
            }
            line += "\n";
            dOut.write(line.getBytes());

            line = "";
            for (int i = 0; i < blow_arr.length; i++) {
                for (int j = 0; j < blow_arr[0].length; j++) {
                    line += blow_arr[i][j] + " ";
                }
                line += "!";
            }
            line += "\n";
            dOut.write(line.getBytes());

            line = "";
            while (it.hasNext()) {
                String[] dataPoint = it.next();
                for (int i = 0; i < dataPoint.length; i++) {
                    line += (dataPoint[i]);
                }
                line += "!";
            }
            dOut.write(line.getBytes());

            dOut.close();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendFile(file.getPath());
                }
            }).start();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }


    public int sendFile(String selectedFilePath) {
        Log.d("hyunrae", selectedFilePath);

        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String php_address = "http://172.16.10.165/spirometry/store_data.php";

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);

        if (!selectedFile.isFile()){
            return 0;
        }
        else {
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
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();

                //See how large buffer do we allocate for the file
                Log.d("Buffer size", Integer.toString(bytesAvailable));

                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
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
                if(serverResponseCode == 200 && isConnectedViaWifi()){
                    Log.d("Upload Debug","File Upload completed.\n\n You can see the uploaded file here: \n\n");
                    //TODO: check and delete only if this isn't an unenrolled baseline survey
//                    selectedFile.delete();
                }
                else {
                    AlertDialog.Builder warning = new AlertDialog.Builder(TestCompleteActivity.this);
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
            Log.d("hyunrae", Integer.toString(serverResponseCode));
            return serverResponseCode;
        }
    }

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mWifi = connectivityManager.getActiveNetworkInfo();
        return mWifi != null && mWifi.isConnectedOrConnecting();
    }

    private void startAlarm(boolean isNotification) {
       // finalDate.clear();
        Log.d(TAG, "Start Alarm!: ");
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(TestCompleteActivity.this, AlarmNotificationReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TestCompleteActivity.this, 0, myIntent, 0);

        if(isNotification) {
        Log.d(TAG, "Start!!! ");
        Log.d(TAG,"start1"+  String.valueOf(finalDate));
        Log.d(TAG, "start2" +  String.valueOf(finalDate.getTimeInMillis()));
        manager.set(AlarmManager.RTC_WAKEUP, finalDate.getTimeInMillis(), pendingIntent);
        }
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(TestCompleteActivity.this, HelpActivity.class);
        startActivityForResult(intent, 1);
    }

    public static String getManufacturerSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serial = (String) get.invoke(c, "ril.serialnumber", "unknown");
        } catch (Exception ignored) {}
        return serial;
    }
}