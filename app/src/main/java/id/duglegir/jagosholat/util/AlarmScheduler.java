package id.duglegir.jagosholat.util; // Pastikan package Anda benar

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class AlarmScheduler {

    // KITA ASUMSIKAN NAMA JADWAL SHOLAT ANDA
    private static final String[] PRAYER_NAMES = {"Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"};

    public static void scheduleAlarms(Context context) {
        for (String prayerName : PRAYER_NAMES) {
            schedulePrayerAlarm(context, prayerName);
        }
        Log.d("AlarmScheduler", "Semua alarm telah dijadwalkan.");
    }

    // Panggil ini dari AlarmReceiver untuk menjadwalkan ulang besok
    public static void scheduleNextDayAlarms(Context context) {
        // Tambahkan 1 menit delay agar tidak tumpang tindih
        long delay = 60 * 1000;

        // Kita gunakan set() agar tidak membebani sistem
        // Ini akan dijadwalkan ulang saat HP idle
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BootReceiver.class); // Panggil BootReceiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        Log.d("AlarmScheduler", "Menjadwalkan ulang semua alarm untuk besok...");
    }


    @SuppressLint("NewApi")
    private static void schedulePrayerAlarm(Context context, String prayerName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 1. Dapatkan waktu sholat dalam milidetik
        long prayerTimeMillis = getPrayerTimeInMillis(prayerName);

        // Jika waktu tidak valid (misal: "00:00") atau sudah lewat
        if (prayerTimeMillis <= System.currentTimeMillis()) {
            // Jika sudah lewat, kita jadwalkan untuk besok
            prayerTimeMillis = getPrayerTimeInMillis(prayerName, true);
        }

        // 2. Buat Intent untuk AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, "Waktu " + prayerName + " Tiba");
        intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, "Saatnya menunaikan sholat " + prayerName);

        // Kita butuh ID unik untuk tiap alarm (Subuh=0, Dzuhur=1, dst.)
        int requestCode = getRequestCode(prayerName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Set Alarm Tepat Waktu
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (Wajib cek izin)
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent);
                } else {
                    // Fallback jika izin tidak ada (alarm mungkin tidak presisi)
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent);
                }
            } else {
                // Android 11 ke bawah
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

        // 2. Ambil waktu (dalam detik) dari getter publik Anda
        switch (prayerName) {
            case "Subuh": totalSeconds = helper.getJmlWaktuShubuh(); break;
            case "Dzuhur": totalSeconds = helper.getJmlWaktuDzuhur(); break;
            case "Ashar": totalSeconds = helper.getJmlWaktuAshar(); break;
            case "Maghrib": totalSeconds = helper.getJmlWaktuMaghrib(); break;
            case "Isya": totalSeconds = helper.getJmlWaktuIsya(); break;
            default: totalSeconds = 0;
        }
        // =======================================================

        // Jika helper tidak mengembalikan waktu (misal: 0)
        if (totalSeconds == 0) {
            Log.e("AlarmScheduler", "Waktu sholat " + prayerName + " tidak ditemukan oleh JadwalHelper.");
            return 0; // Waktu tidak valid
        }

        try {
            // 3. Konversi total detik ke Objek Kalender
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, totalSeconds / 3600); // 3600 detik/jam
            calendar.set(Calendar.MINUTE, (totalSeconds % 3600) / 60); // sisa detik / 60
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Jika untuk besok
            if (isForTomorrow) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            // Jika waktu hari ini sudah lewat, otomatis set untuk besok
            else if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            return calendar.getTimeInMillis();

        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Waktu tidak valid
        }
    }
    // -----------------------------------------------------------------

    // Helper untuk ID unik PendingIntent
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