package com.spirometry.homespirometry;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.classes.NewParcelable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

public class ApplicationChooseActivity extends AppCompatActivity {
    public static final String FILE_NAME = "timeKeeping.txt";

    private static final String TAG = LoginActivity.class.getSimpleName();
    //This is a MyParcelable object that contains data / objects to be passed between activities
    private NewParcelable mBundleData;

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

        mBundleData = getIntent().getParcelableExtra("bundle-data");

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

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Long getDefaults(String key, Context context) {
        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedP.getLong(key, 1);
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

        Long temp = getDefaults("finalDateStore", getApplicationContext());
        Calendar minDate;
        Calendar maxDate;
        Log.d(TAG, "tempLong" + temp);
        Date myDate = new Date(temp);
        Log.d(TAG, "tempDate" + myDate);
        Calendar getLastAppointmentDateforMin =  toCalendar(myDate);
        Calendar getLastAppointmentDateforMax =  toCalendar(myDate);
        Calendar todayDate = Calendar.getInstance();
        Long todayDateCompare = todayDate.getTimeInMillis();
        Long minDateCompare;

        //int dayFinishedTime = getLastAppointmentDate.get(Calendar.DAY_OF_WEEK);

        dialog.findViewById(R.id.cancelBtn).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
        datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);

        Intent myIntent = new Intent(ApplicationChooseActivity.this, AlarmNotificationReciever.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0,myIntent, PendingIntent.FLAG_NO_CREATE);

        //If there is not alarm set, then this will be happening
        if(pendingIntent == null) {
            Log.d(TAG, "No Pending Intent");
            minDate =Calendar.getInstance();
            maxDate = Calendar.getInstance();
            minDateCompare = minDate.getTimeInMillis();

        }else {
             minDate = getLastAppointmentDateforMin;
             maxDate = getLastAppointmentDateforMax;
            minDateCompare = minDate.getTimeInMillis();

        }
        //Long temp = getDefaults("finalDateStore", getApplicationContext());
        //Date myDate = new Date(temp);
        //final Calendar getLastAppointmentDate =  toCalendar(myDate);
        int dayFinishedTime = getLastAppointmentDateforMin.get(Calendar.DAY_OF_WEEK);
        int todayIntTime = todayDate.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "a" + dayFinishedTime);
        Log.d(TAG, "a1" + Calendar.SUNDAY);
        Log.d(TAG, "minDate1m" + minDate);
        Log.d(TAG, "maxDate1ma" + maxDate);

        if(todayDateCompare <= minDateCompare && pendingIntent != null) {
            if (dayFinishedTime == Calendar.SUNDAY) {
                //minDate.add(Calendar.DAY_OF_YEAR, +0);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); //, you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +6);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);
                //datePicker.setMinDate(System.currentTimeMillis()-1000);

            } else if (dayFinishedTime == Calendar.MONDAY) {

                minDate.add(Calendar.DAY_OF_YEAR, -1);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +5);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (dayFinishedTime == Calendar.TUESDAY) {

                minDate.add(Calendar.DAY_OF_YEAR, -2);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +4);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (dayFinishedTime == Calendar.WEDNESDAY) {

                minDate.add(Calendar.DAY_OF_YEAR, -3);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +3);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (dayFinishedTime == Calendar.THURSDAY) {

                minDate.add(Calendar.DAY_OF_YEAR, -4);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +2);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (dayFinishedTime == Calendar.FRIDAY) {

                minDate.add(Calendar.DAY_OF_YEAR, -5);
                maxDate.add(Calendar.DAY_OF_YEAR, +1);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);
                Log.d(TAG, "minDate2m" + minDate);
                Log.d(TAG, "maxDate2ma" + maxDate);
                Log.d(TAG, "daysMax" + daysMax);
                Log.d(TAG, "daysMin" + daysMin);

            } else if (dayFinishedTime == Calendar.SATURDAY) {

                minDate.add(Calendar.DAY_OF_YEAR, -6);
                long daysMin = minDate.getTimeInMillis();
                datePicker.setMinDate(daysMin);
                //maxDate.add(Calendar.DAY_OF_YEAR, +0);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);
            }


        }else{
            //Assuming that the data record period is from Sunday - Saturday, each week updates
/*This is for let's say you scheduled an appointment for Tuesday, and then you come back on Monday to change the date,
    and it is to see if Sunday is an option to choose a date from. Which should not be possible.
*/
            if (todayIntTime == Calendar.SUNDAY) {
                //minDate.add(Calendar.DAY_OF_YEAR, +0);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); //, you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +6);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);
                //datePicker.setMinDate(System.currentTimeMillis()-1000);

            } else if (todayIntTime == Calendar.MONDAY) {

                //minDate.add(Calendar.DAY_OF_YEAR, -1);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +5);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (todayIntTime == Calendar.TUESDAY) {

                //minDate.add(Calendar.DAY_OF_YEAR, -2);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +4);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (todayIntTime == Calendar.WEDNESDAY) {

                //minDate.add(Calendar.DAY_OF_YEAR, -3);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +3);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (todayIntTime == Calendar.THURSDAY) {

                //minDate.add(Calendar.DAY_OF_YEAR, -4);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                maxDate.add(Calendar.DAY_OF_YEAR, +2);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);

            } else if (todayIntTime == Calendar.FRIDAY) {

                //minDate.add(Calendar.DAY_OF_YEAR, -5);
                maxDate.add(Calendar.DAY_OF_YEAR, +1);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin - 10000); // you have to subtract a little from the time for some reason
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);
                Log.d(TAG, "minDate2m" + minDate);
                Log.d(TAG, "maxDate2ma" + maxDate);
                Log.d(TAG, "daysMax" + daysMax);
                Log.d(TAG, "daysMin" + daysMin);

            } else if (todayIntTime == Calendar.SATURDAY) {

                //minDate.add(Calendar.DAY_OF_YEAR, -6);
                long daysMin = todayDate.getTimeInMillis();
                datePicker.setMinDate(daysMin);
                //maxDate.add(Calendar.DAY_OF_YEAR, +0);
                long daysMax = maxDate.getTimeInMillis();
                datePicker.setMaxDate(daysMax);
            }
        }
        //Log.d(TAG, "adsf" + datePicker);


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

