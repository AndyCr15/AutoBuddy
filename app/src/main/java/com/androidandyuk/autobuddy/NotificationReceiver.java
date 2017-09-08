package com.androidandyuk.autobuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

/**
 * Created by AndyCr15 on 06/07/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG,"onReceive");

        String message = intent.getStringExtra("MyMessage");

        checkNotifications(context, message);

    }

    public void checkNotifications(Context context, String message) {
        Log.i(TAG, "checkNotifications");

        int notificationID = 100;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The id of the channel.
            String id = "my_channel_01";
            // The user-visible name of the channel.
            CharSequence name = "Channel Name";
            // The user-visible description of the channel.
            String description = "Channel Desc";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);


            // Create a notification and set the notification channel.
            Notification.Builder notification = new Notification.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Be Aware!")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.icon)
                    .setChannelId(id)
                    .setAutoCancel(true);

            // Issue the notification.
            notificationManager.notify(notificationID, notification.build());

        } else {

            Notification.Builder notification = new Notification.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Be Aware!")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true);

            // Issue the notification.
            notificationManager.notify(notificationID, notification.build());
        }
    }

}