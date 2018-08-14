package com.spirometry.homespirometry;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.spirometry.homespirometry.classes.MyParcelable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

public class ApplicationChooseActivity extends AppCompatActivity {
    public static final String FILE_NAME = "timeKeeping.txt";

    private static final String TAG = LoginActivity.class.getSimpleName();
    //This is a MyParcelable object that contains data / objects to be passed between activities
    private MyParcelable mBundleData;

    TextView timeKeepingText;
    TextView dateTimeRepresent;

    TimePicker timePicker;
    DatePicker datePicker;

    String dateRepresent;
    String timeRepresent;

    Calendar finalDate =Calendar.getInstance();

    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    int[] hours = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_choose);

        mBundleData = new MyParcelable();

        timeKeepingText = (TextView) findViewById(R.id.timeKeepingText);
        dateTimeRepresent = (TextView) findViewById(R.id.dateTimeRepresent);

     ///   AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(ApplicationChooseActivity.this, AlarmNotificationReciever.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0,myIntent, PendingIntent.FLAG_NO_CREATE);

        //PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent,
        //        0);
//        alarmManager.cancel(pendingIntent);

        //If there is not alarm set, then this will be happening
        if(pendingIntent == null) {
            Log.d(TAG, "No Pending Intent");
            dateTimeRepresent.setTextSize(50);
            dateTimeRepresent.setText(R.string.no_alarm_set);

        }else{
            Log.d(TAG, "Pending Intent");
            FileInputStream fis = null;
            dateTimeRepresent.setTextSize(70);

            try {
                fis = openFileInput(FILE_NAME);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String text;

                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }

                dateTimeRepresent.setText(sb.toString());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally { // this will be executed even if the exception is thrown
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void startTest(View v) { // this is a same method as setOnclickListener button_save

        Intent intent = new Intent(ApplicationChooseActivity.this, SpirometerConnectingActivity.class);
        Log.d(TAG, "bundle-data" +mBundleData);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);

    }

    public void changeAppointment(View v) {

        final Dialog dialog = new Dialog(ApplicationChooseActivity.this, R.style.Theme_Dialog);
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
                //Once Clicked Confirm, then previous Alarm PendingIntent will be cancelled
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent myIntent = new Intent(ApplicationChooseActivity.this, AlarmNotificationReciever.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent,
                       0);
                alarmManager.cancel(pendingIntent);//important
                pendingIntent.cancel();//important

                //This is for Display of the Finalized Alarm Time
                dateRepresent = (months[datePicker.getMonth()] + " " + datePicker.getDayOfMonth() + ", " + datePicker.getYear());
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
                timeRepresent = (hours[timePicker.getCurrentHour()] + ":" + minuteString + " " + AM_PM);
                dateTimeRepresent.setTextSize(70);
                dateTimeRepresent.setText(dateRepresent + " " + timeRepresent);

                finalDate.clear();
                finalDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());

                //This is to Save the FinalDateTime in to the timeKeeping.txt
                String text = dateRepresent+ " "+ timeRepresent;
                FileOutputStream fos = null;

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

                //This will set a new pendingIntent Time
                startAlarm(true);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void startAlarm(boolean isNotification) {
        // finalDate.clear();
        Log.d(TAG, "Start Alarm!: ");
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(ApplicationChooseActivity.this, AlarmNotificationReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ApplicationChooseActivity.this, 0, myIntent, 0);

        if(isNotification) {
            Log.d(TAG, "Start!!! ");
            Log.d(TAG,"start1"+  String.valueOf(finalDate));
            Log.d(TAG, "start2" +  String.valueOf(finalDate.getTimeInMillis()));
            manager.set(AlarmManager.RTC_WAKEUP, finalDate.getTimeInMillis(), pendingIntent);
        }
    }

}

