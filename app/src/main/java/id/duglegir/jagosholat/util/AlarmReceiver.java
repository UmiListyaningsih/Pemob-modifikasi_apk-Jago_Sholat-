package id.duglegir.jagosholat.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        if (title == null) {
            title = "Waktu Sholat";
        }

        Log.d("AlarmReceiver", "Alarm Diterima: " + title);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.createNotificationChannel();
        notificationHelper.showNotification(title, message);


        AlarmScheduler.scheduleNextDayAlarms(context);
    }
}