package id.duglegir.jagosholat.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.util.Calendar;

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
        Intent intent = new Intent(context, BootReceiver.class); // Panggil BootReceiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        Log.d("AlarmScheduler", "Menjadwalkan ulang semua alarm untuk besok...");
    }


    @SuppressLint("NewApi")
    private static void schedulePrayerAlarm(Context context, String prayerName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long prayerTimeMillis = getPrayerTimeInMillis(prayerName);

        if (prayerTimeMillis <= System.currentTimeMillis()) {

            prayerTimeMillis = getPrayerTimeInMillis(prayerName, true);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, "Waktu " + prayerName + " Tiba");
        intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, "Saatnya menunaikan sholat " + prayerName);

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

    private static long getPrayerTimeInMillis(String prayerName) {
        return getPrayerTimeInMillis(prayerName, false); // Default untuk hari ini
    }

    private static long getPrayerTimeInMillis(String prayerName, boolean isForTomorrow) {
        JadwalHelper helper = new JadwalHelper();
        int totalSeconds;

        switch (prayerName) {
            case "Subuh": totalSeconds = helper.getJmlWaktuShubuh(); break;
            case "Dzuhur": totalSeconds = helper.getJmlWaktuDzuhur(); break;
            case "Ashar": totalSeconds = helper.getJmlWaktuAshar(); break;
            case "Maghrib": totalSeconds = helper.getJmlWaktuMaghrib(); break;
            case "Isya": totalSeconds = helper.getJmlWaktuIsya(); break;
            default: totalSeconds = 0;
        }


        if (totalSeconds == 0) {
            Log.e("AlarmScheduler", "Waktu sholat " + prayerName + " tidak ditemukan oleh JadwalHelper.");
            return 0; // Waktu tidak valid
        }

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, totalSeconds / 3600); // 3600 detik/jam
            calendar.set(Calendar.MINUTE, (totalSeconds % 3600) / 60); // sisa detik / 60
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
            return 0; // Waktu tidak valid
        }
    }


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