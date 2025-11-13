package id.duglegir.jagosholat.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrayerTimeStorage {

    private static final String PREFS_NAME = "PrayerTimePrefs";

    public static void savePrayerTime(Context context, String prayerName, String time) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prayerName, time);
        editor.apply();
    }

    public static String getPrayerTime(Context context, String prayerName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(prayerName, "00:00");
    }
}