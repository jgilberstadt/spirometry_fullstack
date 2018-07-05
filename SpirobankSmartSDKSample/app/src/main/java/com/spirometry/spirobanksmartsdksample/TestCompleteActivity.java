package com.spirometry.spirobanksmartsdksample;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import static com.spirometry.spirobanksmartsdksample.AppController.TAG;

public class TestCompleteActivity extends AppCompatActivity {

    String[][] arraya; //6 data storing 4 String Values; +-
    private MyParcelable mBundleData;
    private static final String TAG = BlowActivity.class.getSimpleName();
    TextView nextAppointment;
    TextView changeAppointment;
    TextView dateRepresent;
    TextView timeRepresent;
    Button dateButton;
    Calendar finalDate =Calendar.getInstance();
    String pm = "P.M.";
    String am = "A.M.";

    private AlarmManager m_alarmMgr;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private static final int uniqueID = 123456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just

        nextAppointment = (TextView) findViewById(R.id.nextAppointment);
        dateRepresent = (TextView) findViewById(R.id.dateRepresent);
        timeRepresent = (TextView) findViewById(R.id.timeRepresent);
        dateButton = (Button) findViewById(R.id.dateButton);
        changeAppointment = (TextView) findViewById(R.id.changeAppointment);

   /*     nextAppointment.setVisibility(View.VISIBLE);
        dateButton.setVisibility(View.VISIBLE);
        dateRepresent.setVisibility(View.VISIBLE);
        timeRepresent.setVisibility(View.VISIBLE);
        changeAppointment.setVisibility(View.VISIBLE); */

        //Get current date time with Calendar()
        Calendar weekAddedCal = Calendar.getInstance();
        weekAddedCal.add(Calendar.DATE, 7); // Adding a week to current time to set a appointment date
        //weekAddedCal.add(Calendar.HOUR, 2); // Adding a week to current time to set a appointment date

        //Making into a int value so that I can save it it in dateRepresent variable
        int yearFirst = weekAddedCal.get(Calendar.YEAR);
        int monthFirst = (weekAddedCal.get(Calendar.MONTH) + 1);
        int dayFirst = weekAddedCal.get(Calendar.DAY_OF_MONTH);
        final int hour = weekAddedCal.get(Calendar.HOUR_OF_DAY);
        final int minute = weekAddedCal.get(Calendar.MINUTE);

        //This is just a reformatting code, I really don't need this code. This is for debugging purpose
        SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");
        String formattedDate = df.format(weekAddedCal.getTime());
        SimpleDateFormat formatHour = new SimpleDateFormat("hh");
        String formmattedHour = formatHour.format(weekAddedCal.getTime());
        SimpleDateFormat formatMinute = new SimpleDateFormat("mm");
        String formmattedMinute = formatMinute.format(weekAddedCal.getTime());
        dateRepresent.setText(formattedDate);
        if(hour <13) {
            timeRepresent.setText(formmattedHour + ":" + formmattedMinute + " " + am);
        }else{
            timeRepresent.setText(formmattedHour + ":" + formmattedMinute + " " + pm);
        }
        finalDate.setTimeInMillis(System.currentTimeMillis());
        finalDate.clear();
        finalDate.set(yearFirst, monthFirst, dayFirst, hour,minute);
        Log.d(TAG, "finalDate Initial " + finalDate);

        changeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //now we need a calendar object so that we can get a current date and month
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        TestCompleteActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                mDateSetListener,
                                year,month,day); //first is context
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); //
                 dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //show dialog background transpartent
                            dialog.show();
                Log.d(TAG, "When Clicked Ok");


            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                //
                String monthString = "";
                int mathMonth = month + 1;
                if(mathMonth == 1){
                    monthString ="Jan";
                }else if(mathMonth == 2){
                    monthString ="Feb";
                }else if(mathMonth == 3){
                    monthString ="Mar";
                }else if(mathMonth == 4){
                    monthString ="Apr";
                }else if(mathMonth == 5){
                    monthString ="May";
                }else if(mathMonth == 6){
                    monthString ="Jun";
                }else if(mathMonth == 7){
                    monthString ="Jul";
                }else if(mathMonth == 8){
                    monthString ="Aug";
                }else if(mathMonth == 9){
                    monthString ="Sep";
                }else if(mathMonth == 10){
                    monthString ="Oct";
                }else if(mathMonth == 11){
                    monthString ="Nov";
                }else if(mathMonth == 12){
                    monthString ="Dec";
                }
                Log.d(TAG, "When Clicked Ok2");
                String date = monthString + "-" + dayOfMonth + "-" + year;
                finalDate.clear();
                //finalDate.set(year, month, dayOfMonth, 12,35);
                dateRepresent.setText(date);
//++

                TimePickerDialog mTimeSetListener;
                mTimeSetListener = new TimePickerDialog(TestCompleteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        int pmHours =13;

                        if(selectedHour <13) {
                           // timeRepresent.setText(selectedHour + ":" + selectedMinute + " " + am);
                            if(selectedMinute <10){
                                timeRepresent.setText(selectedHour + ":" + "0"+ selectedMinute + " " + am);
                            }else {
                                timeRepresent.setText(selectedHour + ":" + selectedMinute + " " + am);
                            }
                        }else{
                            if(selectedHour == 13){
                                pmHours =1;
                            }else if(selectedHour ==14){
                             pmHours = 2;
                            }else if(selectedHour ==15){
                                pmHours = 3;
                            }else if(selectedHour ==16){
                                pmHours = 4;
                            }else if(selectedHour ==17){
                                pmHours = 5;
                            }else if(selectedHour ==18){
                                pmHours = 6;
                            }else if(selectedHour ==19){
                                pmHours = 7;
                            }else if(selectedHour ==20){
                                pmHours = 8;
                            }else if(selectedHour ==21){
                                pmHours = 9;
                            }else if(selectedHour ==22){
                                pmHours = 10;
                            }else if(selectedHour ==23){
                                pmHours =11;
                            }else if(selectedHour ==24){
                                pmHours =12;
                            }
                            if(selectedMinute <10){
                                timeRepresent.setText(pmHours + ":" + "0"+ selectedMinute + " " + pm);
                            }else {
                                timeRepresent.setText(pmHours + ":" + selectedMinute + " " + pm);
                            }
                        }

                        finalDate.set(year, month, dayOfMonth, selectedHour,selectedMinute);
                        Log.d(TAG, "onTimeSet: " + selectedHour + selectedMinute);
                    }
                }, hour, minute, false);//Yes 24 hour time
                //mTimeSetListener.setTitle("Select Time");
                mTimeSetListener.show();
            }
        };

    /*    new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nextAppointment.setVisibility(View.VISIBLE);
                changeAppointment.setVisibility(View.VISIBLE);
                dateButton.setVisibility(View.VISIBLE);
                dateRepresent.setVisibility(View.VISIBLE);
                timeRepresent.setVisibility(View.VISIBLE);
                testingComplete.setVisibility(View.GONE);
            }
        }, 4000); */

/*        arraya = mBundleData.getBlowDataArray();

        Log.d(TAG, "result blow1" + arraya[4][3]);
        Log.d(TAG, "result blow2" + arraya[0][0]);
        Log.d(TAG, "result blow3" + arraya[1][2]);
        Log.d(TAG, "result blow4" + arraya[2][1]);
        Log.d(TAG, "result blow5" + arraya[3][3]);  */

      // This is notificaiton design ++need this
     /*   NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(TestCompleteActivity.this )
                .setSmallIcon(R.drawable.spiro)
                .setContentTitle("My notification")
                .setContentText("Your Spiro Device Appointment is Today!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(TestCompleteActivity.this);  */


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
        Log.d(TAG, "Start Arlarm!: ");
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
}