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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.databinding.ActivityMainBinding; 
import id.duglegir.jagosholat.util.AlarmScheduler;
import id.duglegir.jagosholat.util.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ViewPager2 v_pager;
    private int resID;

    private String[] pageTitle = {"Catatan", "Jadwal", "Statistik", "Kompas", "Tasbih", "Tata Cara"};
    private String[] pageIcon = {"catat", "jadwal", "statistik", "kompas", "tasbih", "more"};

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

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

        setSupportActionBar(binding.toolbar);
        SlideView();


        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {

                checkExactAlarmPermission();
            }
        } else {


            checkExactAlarmPermission();
        }
    }

    private void checkExactAlarmPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {


                new AlertDialog.Builder(this)
                        .setTitle("Izin Diperlukan")
                        .setMessage("Aplikasi ini memerlukan izin 'Alarm & Pengingat' agar notifikasi sholat dapat berbunyi TEPAT WAKTU. Aktifkan sekarang?")
                        .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);

                            startActivity(intent);
                        })
                        .setNegativeButton("Batal", (dialog, which) -> {
                            Toast.makeText(this, "Alarm mungkin tidak akan presisi.", Toast.LENGTH_LONG).show();

                            AlarmScheduler.scheduleAlarms(this);
                        })
                        .show();
            } else {


                AlarmScheduler.scheduleAlarms(this);
            }
        } else {


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

        int id = item.getItemId();

        if (id == R.id.info_apps) {
            Intent i = new Intent(MainActivity.this, TentangKamiActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SlideView() {

        v_pager = binding.viewpagerMain;



        MainPagerAdapter Adatapters = new MainPagerAdapter(this);


        v_pager.setAdapter(Adatapters);

        new TabLayoutMediator(binding.tablayoutMain, v_pager,
                (tab, position) -> {

                    resID = getResources().getIdentifier("ic_" + pageIcon[position] + "_24px", "drawable", getPackageName());
                    tab.setIcon(resID);
                }
        ).attach(); // PENTING: panggil .attach()

        binding.tablayoutMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                setTitle(pageTitle[tab.getPosition()]);

                int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.IconSelect);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.IconUnselect);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


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