package com.spirometry.homespirometry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.spirometry.homespirometry.R;

import static com.spirometry.homespirometry.AppController.TAG;

public class AlarmNotificationReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d(TAG, "I recieved!: ");
        Intent myIntent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntent(myIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.spiro)
                .setContentTitle("Spirometer Notification")
                .setContentText("Today is your Appointment. Please finish your Spirometer Test")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentInfo("Info")
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
        }



    }
