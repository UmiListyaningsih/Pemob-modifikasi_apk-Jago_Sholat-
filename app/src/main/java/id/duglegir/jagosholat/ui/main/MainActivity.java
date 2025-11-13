package id.duglegir.jagosholat.ui.main;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2; // <-- GANTI KE VIEW PAGER 2

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator; // <-- IMPORT BARU

import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.ActivityMainBinding; // <-- IMPORT VIEW BINDING
import id.duglegir.jagosholat.util.AlarmScheduler;
import id.duglegir.jagosholat.util.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ViewPager2 v_pager; // <-- GANTI KE VIEW PAGER 2
    private int resID;

    // Deklarasi Judul & Ikon (Tetap sama)
    private String[] pageTitle = {"Catatan", "Jadwal", "Statistik", "Kompas", "Tasbih", "Tata Cara"};
    private String[] pageIcon = {"catat", "jadwal", "statistik", "kompas", "tasbih", "more"};

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Izin diberikan, sekarang cek Izin Alarm Tepat Waktu
                    checkExactAlarmPermission();
                } else {
                    Toast.makeText(this, "Izin Notifikasi ditolak. Alarm tidak akan berbunyi.", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ... (kode setSupportActionBar & SlideView Anda) ...
        setSupportActionBar(binding.toolbar);
        SlideView();

        // === 2. Panggil Rangkaian Pengecekan Izin ===
        // (Panggil ini hanya sekali, mungkin gunakan SharedPreferences)
        checkAndRequestPermissions();
    }

    // === 3. Fungsi untuk Memulai Rangkaian Izin ===
    private void checkAndRequestPermissions() {
        // Cek Izin Notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Minta Izin
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Izin Notifikasi sudah ada, cek Izin Alarm
                checkExactAlarmPermission();
            }
        } else {
            // Android 12 ke bawah (Izin Notifikasi otomatis ada)
            // Langsung cek Izin Alarm
            checkExactAlarmPermission();
        }
    }

    // === 4. Fungsi untuk Cek Izin Alarm Tepat Waktu ===
    private void checkExactAlarmPermission() {
        // Cek Izin Alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Izin tidak ada. Arahkan pengguna ke Settings.
                // Kita tidak bisa MINTA izin ini, pengguna harus MENGAKTIFKANNYA.
                new AlertDialog.Builder(this)
                        .setTitle("Izin Diperlukan")
                        .setMessage("Aplikasi ini memerlukan izin 'Alarm & Pengingat' agar notifikasi sholat dapat berbunyi TEPAT WAKTU. Aktifkan sekarang?")
                        .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            // (Opsional) Arahkan ke app Anda: intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Batal", (dialog, which) -> {
                            Toast.makeText(this, "Alarm mungkin tidak akan presisi.", Toast.LENGTH_LONG).show();
                            // Tetap jadwalkan, meskipun mungkin tidak tepat waktu
                            AlarmScheduler.scheduleAlarms(this);
                        })
                        .show();
            } else {
                // Izin Alarm sudah ada.
                // Aman untuk menjadwalkan alarm.
                AlarmScheduler.scheduleAlarms(this);
            }
        } else {
            // Android 11 ke bawah (Izin Alarm otomatis ada)
            // Aman untuk menjadwalkan alarm.
            AlarmScheduler.scheduleAlarms(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Ganti 'switch' dengan 'if' untuk memperbaiki error 'constant expression'
        int id = item.getItemId();

        if (id == R.id.info_apps) {
            Intent i = new Intent(MainActivity.this, TentangKamiActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SlideView() {
        // Ambil ViewPager2 dari binding
        v_pager = binding.viewpagerMain;

        // =====================================================================
        // INI ADALAH PERBAIKAN UNTUK ERROR ANDA (BARIS 92)
        // Gunakan constructor BARU ('this' adalah FragmentActivity)
        MainPagerAdapter Adatapters = new MainPagerAdapter(this);
        // =====================================================================

        v_pager.setAdapter(Adatapters);

        // CARA BARU: Gunakan TabLayoutMediator untuk menyambungkan ViewPager2 dan TabLayout
        new TabLayoutMediator(binding.tablayoutMain, v_pager,
                (tab, position) -> {
                    // Set IKON di sini
                    resID = getResources().getIdentifier("ic_" + pageIcon[position] + "_24px", "drawable", getPackageName());
                    tab.setIcon(resID);
                }
        ).attach(); // PENTING: panggil .attach()

        // Listener BARU untuk mengatur warna ikon dan judul Toolbar
        binding.tablayoutMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Set judul toolbar
                setTitle(pageTitle[tab.getPosition()]);

                // Set warna ikon yang aktif
                int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.IconSelect);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Set warna ikon yang tidak aktif
                int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.IconUnselect);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tidak perlu melakukan apa-apa
            }
        });

        // Set tab default secara manual (untuk pertama kali)
        // Ini untuk memastikan tab pertama (0) memiliki warna dan judul yang benar saat aplikasi dibuka
        TabLayout.Tab firstTab = binding.tablayoutMain.getTabAt(0);
        if (firstTab != null) {
            setTitle(pageTitle[0]);
            int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.IconSelect);
            if (firstTab.getIcon() != null) {
                firstTab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }
}