package id.duglegir.jagosholat.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.util.Calendar;
import id.duglegir.jagosholat.util.PrayerTimeStorage;

public class AlarmScheduler {

    private static final String[] PRAYER_NAMES = {"Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"};

    public static void scheduleAlarms(Context context) {
        for (String prayerName : PRAYER_NAMES) {
            schedulePrayerAlarm(context, prayerName);
        }
        Log.d("AlarmScheduler", "Semua alarm telah dijadwalkan.");
    }

    public static void scheduleNextDayAlarms(Context context) {

        long delay = 60 * 1000;


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BootReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        Log.d("AlarmScheduler", "Menjadwalkan ulang semua alarm untuk besok...");
    }


    @SuppressLint("NewApi")
    private static void schedulePrayerAlarm(Context context, String prayerName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long prayerTimeMillis = getPrayerTimeInMillis(context, prayerName, false);

        if (prayerTimeMillis <= System.currentTimeMillis()) {

            prayerTimeMillis = getPrayerTimeInMillis(context, prayerName, true);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_PRAYER_NAME, prayerName);

        int requestCode = getRequestCode(prayerName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent);
                } else {

                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent);
                }
            } else {

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent);
            }
            Log.d("AlarmScheduler", "Alarm " + prayerName + " di-set untuk: " + prayerTimeMillis);

        } catch (SecurityException e) {
            Log.e("AlarmScheduler", "Gagal set alarm: " + e.getMessage());
        }
    }

    private static long getPrayerTimeInMillis(Context context, String prayerName, boolean isForTomorrow) {
        String timeString = PrayerTimeStorage.getPrayerTime(context, prayerName);


        if (timeString.equals("00:00")) {
            Log.e("AlarmScheduler", "Waktu sholat " + prayerName + " tidak ditemukan di Storage.");
            return 0;
        }
        

        try {
            String[] parts = timeString.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (isForTomorrow) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            else if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            return calendar.getTimeInMillis();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Overload method agar 'getPrayerTimeInMillis(prayerName)' tetap berfungsi
    private static long getPrayerTimeInMillis(String prayerName) {
        // Ini adalah kesalahan, method ini butuh Context.
        // Kita harus perbaiki pemanggilan di 'schedulePrayerAlarm'
        Log.e("AlarmScheduler", "Pemanggilan salah, butuh context!");
        return 0;
    }
    // -----------------------------------------------------------------


    private static int getRequestCode(String prayerName) {
        switch (prayerName) {
            case "Subuh": return 0;
            case "Dzuhur": return 1;
            case "Ashar": return 2;
            case "Maghrib": return 3;
            case "Isya": return 4;
            default: return 5;
        }
    }
}