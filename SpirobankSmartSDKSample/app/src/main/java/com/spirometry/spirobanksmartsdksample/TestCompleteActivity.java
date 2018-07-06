package com.spirometry.spirobanksmartsdksample;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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

    private AlarmManager m_alarmMgr;

    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    int[] hours = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just

        nextAppointment = (TextView) findViewById(R.id.nextAppointment);
        dateRepresent = (TextView) findViewById(R.id.dateRepresent);
        timeRepresent = (TextView) findViewById(R.id.timeRepresent);
        dateButton = (Button) findViewById(R.id.dateButton);
        changeAppointment = (Button) findViewById(R.id.changeAppointment);

        //Get current date time with Calendar()
        Calendar weekAddedCal = Calendar.getInstance();
        weekAddedCal.add(Calendar.DATE, 7); // Adding a week to current time to set a appointment date

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

                dialog.findViewById(R.id.confirmBtn).setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
                        DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);

                        dateRepresent.setText(months[datePicker.getMonth()] + " " + datePicker.getDayOfMonth() + ", " + datePicker.getYear());
                        String AM_PM;
                        if (timePicker.getCurrentHour() < 12) {
                            AM_PM = "AM";
                        } else {
                            AM_PM = "PM";
                        }
                        timeRepresent.setText(hours[timePicker.getCurrentHour()] + ":" + timePicker.getCurrentMinute() + " " + AM_PM);
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
}