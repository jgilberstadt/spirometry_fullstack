package com.spirometry.homespirometry;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.classes.NewParcelable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

public class TestCompleteActivity extends AppCompatActivity {

    public static final String FILE_NAME = "timeKeeping.txt";

    String[][] arraya; //6 data storing 4 String Values; +-
    private NewParcelable mBundleData;
    private static final String TAG = BlowActivity.class.getSimpleName();
    TextView nextAppointment;
    TextView varianceAndSymptoms;
    TextView varianceAndNoSymptoms;
    EditText symptomIndicatorText;
    Button changeAppointment;
    TextView dateRepresent;
    TextView timeRepresent;
    Button dateButton;
    Calendar finalDate = Calendar.getInstance();
    TimePicker timePicker;
    DatePicker datePicker;
    Handler mHandler;

    private AlarmManager m_alarmMgr;

    File file;

    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    int[] hours = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);

        mHandler = new Handler();

        mBundleData = getIntent().getParcelableExtra("bundle-data");

//        Log.d("hyunrae", Arrays.toString(mBundleData.getSurveyAnswerArr()));
        // for testing purpose. When deployed, comment the following hardcode flags.
        /*
        mBundleData.setVarianceExists(1);
        mBundleData.setSymptomsExist(0);
        */
        // should handle the case of normal test vs. repeated test
        if (mBundleData.getVarianceExists()==1) {
            if (mBundleData.getSymptomsExist()==1) {
                varianceAndSymptoms = (TextView) findViewById(R.id.varianceAndSymptoms);
                varianceAndSymptoms.setVisibility(View.VISIBLE);

                createFile("yesVarianceYesSymptoms", true);
            } else {
                varianceAndNoSymptoms = (TextView) findViewById(R.id.varianceAndNoSymptoms);
                varianceAndNoSymptoms.setVisibility(View.VISIBLE);
                //TODO: set notifications for the next 4 days here
                // get value from shared preference

                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                int testingPeriodDay = sharedPref.getInt(getString(R.string.testingPeriodDay),0);
                SharedPreferences.Editor editor = sharedPref.edit();
                if(testingPeriodDay<4) {
                    editor.putInt(getString(R.string.testingPeriodDay), testingPeriodDay + 1);
                    editor.apply();
                    startSurveyAlarm();
                }else{
                    editor.putInt(getString(R.string.testingPeriodDay), 0);
                    editor.apply();
                }
                createFile("yesVarianceNoSymptoms", true);
            }
        } else {
            nextAppointment = (TextView) findViewById(R.id.nextAppointment);
            nextAppointment.setVisibility(View.VISIBLE);
            dateRepresent = (TextView) findViewById(R.id.dateRepresent);
            timeRepresent = (TextView) findViewById(R.id.timeRepresent);
            dateButton = (Button) findViewById(R.id.dateButton);
            changeAppointment = (Button) findViewById(R.id.changeAppointment);

            //Get Test Finished Time
            final Calendar getTestFinsihedTime = Calendar.getInstance();
            final int dayFinishedTime = getTestFinsihedTime.get(Calendar.DAY_OF_WEEK);
            Log.d(TAG, "Day of Week" + dayFinishedTime);
            Log.d(TAG, "Calendar Day of the Week" + Calendar.SATURDAY);

            if (dayFinishedTime != Calendar.SATURDAY) {
                Log.d(TAG, "Day of Week" + getTestFinsihedTime.DAY_OF_WEEK);

//            getTestFinsihedTime.add( Calendar.DATE, 1 );
            }

            java.util.Date currentTime = getTestFinsihedTime.getTime();
            Log.d(TAG, "currentTime" + currentTime);


            //Log.d("What time is it?"  + getTestFinsihedTime);

            //Get current date time with Calendar()
            Calendar weekAddedCal = Calendar.getInstance();
            weekAddedCal.add(Calendar.DATE, 7); // Adding a week to current time to set a appointment date

            int minutes = weekAddedCal.get(Calendar.MINUTE);
            int mod = minutes % 15;
            weekAddedCal.add(Calendar.MINUTE, mod < 8 ? -mod : (15 - mod)); //mod < 8 ? -mod : (15-mod)
            //If the equality is true, then do the first one (number before colon)
            //if the equality is not right, then do the second one (number after the colon)

            Log.d("minutes", Integer.toString(minutes));

            SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
            String formattedDate = df.format(weekAddedCal.getTime());
            Log.d(TAG, " dddd" + formattedDate);
            dateRepresent.setText(formattedDate);

            SimpleDateFormat formatHour = new SimpleDateFormat("hh:mm");
            String formattedTime = formatHour.format(weekAddedCal.getTime());

            if (formattedTime.charAt(0) == '0') {
                formattedTime = formattedTime.substring(1);
            }

            if (weekAddedCal.get(Calendar.HOUR_OF_DAY) < 13) {
                timeRepresent.setText(formattedTime + " " + "AM");
            } else {
                timeRepresent.setText(formattedTime + " " + "PM");
            }

            finalDate = weekAddedCal;

            Log.d(TAG, "finalDate Initial " + finalDate);

            changeAppointment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Dialog dialog = new Dialog(TestCompleteActivity.this, R.style.Theme_Dialog);
                    //dialog.getDatePicker().setMaxDate(new Date().getTime());
                    dialog.setContentView(R.layout.date_time_picker);

                    dialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
                    datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
                    Calendar minDate = Calendar.getInstance();
                    Calendar maxDate = Calendar.getInstance();
                    if (dayFinishedTime == Calendar.SUNDAY) {
                        Log.d(TAG, "Day of Week" + getTestFinsihedTime.DAY_OF_WEEK);
                        minDate.add(Calendar.DAY_OF_YEAR, +7);
                        long sevenDaysAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +13);
                        long thirteenDayAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(sevenDaysAhead);
                        datePicker.setMaxDate(thirteenDayAhead);
                        //datePicker.setMinDate(System.currentTimeMillis()-1000);
                    } else if (dayFinishedTime == Calendar.MONDAY) {
                        minDate.add(Calendar.DAY_OF_YEAR, +6);
                        long sixDaysAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +12);
                        long twelveDaysAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(sixDaysAhead);
                        datePicker.setMaxDate(twelveDaysAhead);
                    } else if (dayFinishedTime == Calendar.TUESDAY) {
                        minDate.add(Calendar.DAY_OF_YEAR, +5);
                        long fiveDaysAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +11);
                        long elevenDaysAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(fiveDaysAhead);
                        datePicker.setMaxDate(elevenDaysAhead);
                    } else if (dayFinishedTime == Calendar.WEDNESDAY) {
                        minDate.add(Calendar.DAY_OF_YEAR, +4);
                        long fourDaysAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +10);
                        long tenDaysAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(fourDaysAhead);
                        datePicker.setMaxDate(tenDaysAhead);
                    } else if (dayFinishedTime == Calendar.THURSDAY) {
                        minDate.add(Calendar.DAY_OF_YEAR, +3);
                        long threeDaysAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +9);
                        long nineDaysAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(threeDaysAhead);
                        datePicker.setMaxDate(nineDaysAhead);
                    } else if (dayFinishedTime == Calendar.FRIDAY) {
                        minDate.add(Calendar.DAY_OF_YEAR, +2);
                        long twoDaysAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +8);
                        long eightDaysAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(twoDaysAhead);
                        datePicker.setMaxDate(eightDaysAhead);
                    } else if (dayFinishedTime == Calendar.SATURDAY) {
                        minDate.add(Calendar.DAY_OF_YEAR, +1);
                        long oneDayAhead = minDate.getTimeInMillis();
                        maxDate.add(Calendar.DAY_OF_YEAR, +7);
                        long sevenDaysAhead = maxDate.getTimeInMillis();
                        datePicker.setMinDate(oneDayAhead);
                        datePicker.setMaxDate(sevenDaysAhead);
                    }
                    //  timePicker.

              /*  maxDate.add(Calendar.DAY_OF_YEAR, +5);
                long fiveDaysAhead = maxDate.getTimeInMillis();
                datePicker.setMaxDate(fiveDaysAhead); */

                    dialog.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent myIntent = new Intent(TestCompleteActivity.this, AlarmNotificationReciever.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent,
                                    0);
                            alarmManager.cancel(pendingIntent);//important
                            pendingIntent.cancel();//important

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
                            } else {
                                minuteString = Integer.toString(minute);
                            }
                            timeRepresent.setText(hours[timePicker.getCurrentHour()] + ":" + minuteString + " " + AM_PM);
                            finalDate.clear();
                            finalDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());

                            //file name called savedName , and MODE_PRIVATE meaning only this application can access
                            // SharedPreferences sharedP = getSharedPreferences("savedName", Context.MODE_PRIVATE);

                            setDefaults("finalDateStore", finalDate.getTimeInMillis(), getApplicationContext());
                            getDefaults("finalDateStore", getApplicationContext());
                            Log.d(TAG, "getDefaults" + getDefaults("finalDateStore", getApplicationContext()));
                            Long temp = getDefaults("finalDateStore", getApplicationContext());
                            Date myDate = new Date(temp);
                            Log.d(TAG, "getDefaults " + "Date" + myDate);
                            Toast.makeText(getApplicationContext(), "saved" + myDate, Toast.LENGTH_SHORT).show();


                            //gives object that can edit to this file
            /*            SharedPreferences.Editor editor = sharedP.edit();
                        editor.putLong("saved" , finalDate.getTimeInMillis());
                        editor.apply();

                        Date myDate = new Date(sharedP.getLong("saved", 0));
                        Log.d(TAG, "mydate" + myDate); */


              /*          java.util.Date savedTIIIIMMMEe = finalDate.getTime();
                        long savedMillis = savedTIIIIMMMEe.getTime();
                        SharedPreferences prefs = getSharedPreferences("savedNameTwo", Context.MODE_PRIVATE);; */


//                        Toast.makeText(getApplicationContext(), "saved" + myDate, Toast.LENGTH_SHORT).show();
                            //   Log.d(TAG, "savedDate" + )


                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });


            dateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String text = dateRepresent.getText().toString() + " " + timeRepresent.getText().toString();
                    FileOutputStream fos = null;
                    Log.d(TAG, "try" + text);
                    Log.d(TAG, "try" + finalDate);


                    try { //try always gets executed, and then if some kind of error occurs, then go to catch method. (Ah i see this now heh)
                        fos = openFileOutput(FILE_NAME, MODE_PRIVATE); //Mode_private means that only this spirometer app can access the file, not the other apps
                        fos.write(text.getBytes()); // now we need to actaully save this outputstream file, officially saves the data
                        Toast.makeText(getApplicationContext(), "Saved: " + FILE_NAME, // Saved to " + getFilesDir() + "/" +
                                Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) { // this is an exception from for.write(text.getBytes())
                        e.printStackTrace();
                    } finally { // this will be executed even if the exception is thrown
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    startAlarm(true);
                    Intent intent = new Intent(TestCompleteActivity.this, FinalPageActivity.class);
                    intent.putExtra("bundle-data", mBundleData);
                    TestCompleteActivity.this.startActivity(intent);
                    finish();
                }
            });
            createFile("noVariance", false);
        }

    }

    public static void setDefaults(String key, Long value, Context context) {
        //SharedPreferences sharedP = getSharedPreferences("savedName", Context.MODE_PRIVATE);
        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedP.edit();
        editor.putLong(key, value);
        editor.apply();
        //editor.commit();
    }

    public static Long getDefaults(String key, Context context) {
        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedP.getLong(key, 1);
    }

    public void createFileRaw(String param, boolean addSurvey) {
        final File file_path = getFilesDir();
        final String file_name = param + "_" + getManufacturerSerialNumber();

        file = new File(file_path, file_name);

        //String[][] blow_arr = mBundleData.getBlowDataArray();
        String blow_arr = mBundleData.getBlowDataArray();
        String pulsedata = mBundleData.getPulseData();

        //LinkedList<String[]> pulse_list = mBundleData.getPulseData();
        //ListIterator<String[]> it = pulse_list.listIterator();
        String[] it = pulsedata.split("\n");

        //int[] survey_arr = mBundleData.getSurveyAnswerArr();

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            ///DeflaterOutputStream dOut = new DeflaterOutputStream(fOut);
            String line = "";

            /*
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    line += blow_arr[i][j] + " ";
                }
                line += "!";
            }
            */
            line += "\n";
            fOut.write(line.getBytes());

            /*
            line = "";
            while (it.hasNext()) {
                String[] dataPoint = it.next();
                for (int i = 0; i < dataPoint.length; i++) {
                    line += (dataPoint[i]);
                }
                line += "!";
            }
            line += "\n";
            fOut.write(line.getBytes());
            */

            line = mBundleData.getLowestSat() + "!" + mBundleData.getMinHeartrate() + "!" + mBundleData.getMaxHeartrate() + "!" + mBundleData.getTimeAbnormal() + "!" + mBundleData.getTimeMinRate();
            line += "\n";
            fOut.write(line.getBytes());

            /*
            line = "";
            if (addSurvey) {
                for (int i = 0; i < survey_arr.length; i++) {
                    line += survey_arr[i];
                }
                line += "\n";
                fOut.write(line.getBytes());
            }
            */

            fOut.close();

            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager != null ? connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) : null;
            //reduce data usage - only send file if wifi is connected
            if (mWifi != null && mWifi.isConnected()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkSurvey(file_path.getPath(), file_name, "https://hstest.wustl.edu/spirometry/store_data_plain.php");
                    }
                }).start();
            }
            else{
                mHandler.post(new Runnable() {
                    public void run(){
                        AlertDialog.Builder warning = new AlertDialog.Builder(TestCompleteActivity.this);
                        warning.setMessage("Please connect to wi=fi later on to upload your answers.").setTitle("Wi-Fi not connected");
                        AlertDialog warning_dialog = warning.create();
                        warning_dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        warning_dialog.show();
                }
            });
            }

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }

    public void createFile(String param, boolean addSurvey) {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final File file_path = getFilesDir();
        final String file_name = param + "_" + getManufacturerSerialNumber() + "_" + sdf.format(currentTime);

        Log.d("upload_filename pos 1", file_name);

        file = new File(file_path, file_name);

        // fake data
        //mBundleData.setBlowDataArray("1.85 1.47 1.97 1.52 1.53 2.18\n");
        /*
        mBundleData.setPatient_id("123456");
        mBundleData.setVarianceExists(1);
        mBundleData.setLowestSat(95);
        mBundleData.setMinHeartrate(30);
        mBundleData.setMaxHeartrate(100);
        mBundleData.setTimeAbnormal(600);
        mBundleData.setTimeMinRate(140);
        */


        //Log.d("result", mBundleData.getBlowDataArray());

        String[] blow_arr = mBundleData.getBlowDataArray().split("\n");
        String[] pulsedata = mBundleData.getPulseData().split("\n");

        int[] survey_arr = mBundleData.getSurveyAnswerArr();


        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            //DeflaterOutputStream dOut = new DeflaterOutputStream(fOut);

            // get patient_id, test_date, normal range?, test counter.
            String line = "";

            line += mBundleData.getPatient_id();
            line += "!";
            line += sdf.format(currentTime);
            line += "!";
            if(mBundleData.getVarianceExists()==1) {
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                final int testingPeriodDay = sharedPref.getInt(getString(R.string.testingPeriodDay),0);
                line = line + "1!" + Integer.toString(testingPeriodDay);
            }
            else {
                line += "0!0";
            }
            line += "\n";
            fOut.write(line.getBytes());
            line = "";

            for (int i = 0; i < 6; i++) {

                String each_blow = blow_arr[i];
                String[] params = each_blow.split(" ");
                Log.d("fev1:", params[1]);
                line += params[1];

                line += "!";
            }
            line += "\n";
            fOut.write(line.getBytes());

            line = mBundleData.getLowestSat() + "!" + mBundleData.getMinHeartrate() + "!" + mBundleData.getMaxHeartrate() + "!" + mBundleData.getTimeAbnormal() + "!" + mBundleData.getTimeMinRate();
            line += "\n";
            fOut.write(line.getBytes());

            line = mBundleData.getBlowDataArrayPefFev1() + "\n";
            fOut.write(line.getBytes());

            line = mBundleData.getPulseData() + "\n";
            fOut.write(line.getBytes());


            if (addSurvey) {
                line = "";
                line = line + mBundleData.getQuestionStates(0) + "!" + mBundleData.getQuestionStates(1) + "!" + mBundleData.getQuestionStates(2) + "!" + mBundleData.getQuestionStates(3);
                line += "\n";
                fOut.write(line.getBytes());
                line = "";
                for (int i = 0; i < survey_arr.length; i++) {
                    line = line + survey_arr[i] + "!";
                }
                //line += "\n";
                fOut.write(line.getBytes());
            }


            fOut.close();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkSurvey(file_path.getPath(), file_name, "https://hstest.wustl.edu/spirometry/store_data_plain.php");
                }
            }).start();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

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

        Log.d("final path:", selectedFile.getPath());

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
                connection.setRequestProperty("uploaded_file", file_name);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + file_name + "\"" + lineEnd);

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
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(TestCompleteActivity.this, AlarmNotificationReciever.class);
        myIntent.putExtra("notificationTitle", "Spirometer Notification");
        myIntent.putExtra("notificationBody","Today is your Appointment. Please finish your Spirometer Test");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TestCompleteActivity.this, 0, myIntent, 0);

        if (isNotification) {
            Log.d(TAG, "Start!!! ");
            Log.d(TAG, "start1" + String.valueOf(finalDate));
            Log.d(TAG, "start2" + String.valueOf(finalDate.getTimeInMillis()));
            manager.set(AlarmManager.RTC_WAKEUP, finalDate.getTimeInMillis(), pendingIntent);
        }
    }

    private void startSurveyAlarm() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.HOUR_OF_DAY, 9);

        Log.d(TAG, "Start Alarm!: ");

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(TestCompleteActivity.this, AlarmNotificationReciever.class);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        final int testingPeriodDay = sharedPref.getInt(getString(R.string.testingPeriodDay),0);

        Log.d(TAG, "test day: "+testingPeriodDay);

        myIntent.putExtra("notificationTitle","Repeated Tests - 4 Day Testing Period");
        myIntent.putExtra("notificationBody","Please log in to repeat your test & questionnaire");

        Log.d(TAG,"test day: "+ myIntent.getStringExtra("notificationTitle"));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(TestCompleteActivity.this, 0, myIntent, 0);
        myIntent = null;
        manager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

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
        } catch (Exception ignored) {
        }
        return serial;
    }
    /*
    private void sendSymptomIndicator(final EditText symptomIndicatorText) {
        final String symptomIndicator  = symptomIndicatorText.getText().toString();
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_CHECK_PATIENT_EXIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Network Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Symptoms: ", symptomIndicator);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "symptomIndicatorRequest");
        spiroProgressBar.setVisibility(View.INVISIBLE);
    }
    */

    public void checkSurvey(final String selectedFilePath, final String file_name, final String php_address) {


            String tag_string_req = "req_confirm";

            StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_CHECK_FILE_EXIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "confirmSurvey Response: " + response);
                    if (response.equals("true")) {
                        // database contains survey
                        Log.d("exists", "YES");
                        File file = new File(selectedFilePath, file_name);
                        file.delete();
                    } else {
                        Log.d("exists", "NO");

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
}