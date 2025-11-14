package id.duglegir.jagosholat.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.ui.main.MainActivity;

public class AdhanPlaybackService extends Service {

    public static final String EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME";
    private static final String CHANNEL_ID = "ADHAN_PLAYBACK_CHANNEL";
    private static final int NOTIFICATION_ID = 2;

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME);
        if (prayerName == null) prayerName = "Waktu Sholat";

        Log.d("AdhanService", "Memulai service untuk: " + prayerName);

        Notification notification = buildForegroundNotification(prayerName);
        startForeground(NOTIFICATION_ID, notification);

        int adhanResourceId;
        if (prayerName.equals("Subuh")) {
            adhanResourceId = R.raw.adhan_fajr;
        } else {
            adhanResourceId = R.raw.adhan_main;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, adhanResourceId);

        if (mediaPlayer == null) {
            Log.e("AdhanService", "Gagal membuat MediaPlayer. File audio ada?");
            stopSelf();
            return START_NOT_STICKY;
        }

        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnCompletionListener(mp -> {
            Log.d("AdhanService", "Adzan selesai diputar. Menghentikan service.");
            stopSelf();
        });

        mediaPlayer.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private Notification buildForegroundNotification(String prayerName) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Adzan " + prayerName)
                .setContentText("Sedang memutar Adzan...")
                .setSmallIcon(R.drawable.ic_logo_jago_sholat)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notifikasi Pemutaran Adzan",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel untuk service pemutar Adzan");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}