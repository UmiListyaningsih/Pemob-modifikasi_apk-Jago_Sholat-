package id.duglegir.jagosholat.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address; // <-- Import baru
import android.location.Geocoder; // <-- Import baru
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer; // <-- Import baru
import android.os.Handler; // <-- Import baru
import android.os.Looper; // <-- Import baru
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.IOException; // <-- Import baru
import java.util.ArrayList; // <-- Import baru
import java.util.Calendar; // <-- Import baru
import java.util.Collections; // <-- Import baru
import java.util.List; // <-- Import baru
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService; // <-- Import baru
import java.util.concurrent.Executors; // <-- Import baru
import java.util.concurrent.TimeUnit; // <-- Import baru

import id.duglegir.jagosholat.databinding.FragmentJadwalBinding;
import id.duglegir.jagosholat.util.AlarmScheduler; // <-- Import helper alarm
import id.duglegir.jagosholat.util.PrayerTimeStorage;

public class JadwalFragment extends Fragment {

    private FragmentJadwalBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue volleyQueue;

    private ExecutorService executor;
    private Handler handler;

    private CountDownTimer countDownTimer;
    private static final String[] PRAYER_NAMES = {"Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"};
    private static final String FORMAT_COUNTDOWN = "%02d:%02d:%02d";

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGrantedMap) -> {
                Boolean fineGranted = isGrantedMap.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseGranted = isGrantedMap.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if ((fineGranted != null && fineGranted) || (coarseGranted != null && coarseGranted)) {
                    startGpsFetch();
                } else {
                    Toast.makeText(requireContext(), "Izin lokasi ditolak.", Toast.LENGTH_LONG).show();
                }
            });

    public JadwalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJadwalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        volleyQueue = Volley.newRequestQueue(requireContext());

        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startGpsFetch();
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void startGpsFetch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        binding.txtWaktuShubuh.setText("Mencari...");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        fetchPrayerTimesFromApi(location);

                        loadAddress(location.getLatitude(), location.getLongitude());

                    } else {
                        binding.txtWaktuShubuh.setText("Gagal mendapat lokasi");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.txtWaktuShubuh.setText("Error: " + e.getMessage());
                });
    }

    private void fetchPrayerTimesFromApi(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String url = String.format(Locale.US,
                "https://api.aladhan.com/v1/timings?latitude=%.4f&longitude=%.4f&method=9",
                latitude, longitude);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        JSONObject timings = response.getJSONObject("data").getJSONObject("timings");

                        String fajr = timings.getString("Fajr");
                        String dhuhr = timings.getString("Dhuhr");
                        String asr = timings.getString("Asr");
                        String maghrib = timings.getString("Maghrib");
                        String isha = timings.getString("Isha");

                        binding.txtWaktuShubuh.setText(fajr);
                        binding.txtWaktuDzuhur.setText(dhuhr);
                        binding.txtWaktuAshar.setText(asr);
                        binding.txtWaktuMaghrib.setText(maghrib);
                        binding.txtWaktuIsya.setText(isha);

                        Context context = requireContext();
                        PrayerTimeStorage.savePrayerTime(context, "Subuh", fajr);
                        PrayerTimeStorage.savePrayerTime(context, "Dzuhur", dhuhr);
                        PrayerTimeStorage.savePrayerTime(context, "Ashar", asr);
                        PrayerTimeStorage.savePrayerTime(context, "Maghrib", maghrib);
                        PrayerTimeStorage.savePrayerTime(context, "Isya", isha);

                        AlarmScheduler.scheduleAlarms(context);

                        calculateAndStartCountdown();

                    } catch (Exception e) {
                        e.printStackTrace();
                        binding.txtWaktuShubuh.setText("Error parsing JSON");
                    }
                }, error -> {
                    error.printStackTrace();
                    binding.txtWaktuShubuh.setText("Gagal mengambil data");
                });

        volleyQueue.add(jsonObjectRequest);
    }

    private void loadAddress(double lat, double lng) {
        executor.execute(() -> {
            String addressString = "Lokasi tidak diketahui";
            if (Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address obj = addresses.get(0);
                        addressString = obj.getLocality();
                        if (addressString == null) {
                            addressString = obj.getSubAdminArea();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Post ke UI thread
            String finalAddressString = addressString;
            handler.post(() -> {
                if (binding != null) {
                    binding.tvLokasi.setText(finalAddressString);
                }
            });
        });
    }

    private long getPrayerTimeInMillis(Context context, String prayerName) {
        String timeString = PrayerTimeStorage.getPrayerTime(context, prayerName);
        if (timeString.equals("00:00")) {
            return 0; // Waktu tidak valid
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
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void calculateAndStartCountdown() {
        if (getContext() == null) return;

        long now = System.currentTimeMillis();
        ArrayList<Long> prayerTimesMillis = new ArrayList<>();
        ArrayList<String> prayerNamesList = new ArrayList<>();

        for (String name : PRAYER_NAMES) {
            long time = getPrayerTimeInMillis(requireContext(), name);
            if (time > 0) {
                prayerTimesMillis.add(time);
                prayerNamesList.add(name);
            }
        }

        for (int i = 0; i < prayerTimesMillis.size(); i++) {
            long prayerTime = prayerTimesMillis.get(i);
            if (prayerTime > now) {
                long millisUntilFinished = prayerTime - now;
                String nextPrayerName = prayerNamesList.get(i);

                binding.txtViewSholat.setText(nextPrayerName);
                startCountdownTimer(millisUntilFinished);
                return;
            }
        }

        long tomorrowFajrMillis = getPrayerTimeInMillis(requireContext(), "Subuh");
        if (tomorrowFajrMillis > 0) {
            tomorrowFajrMillis += (24 * 60 * 60 * 1000);
            long millisUntilFinished = tomorrowFajrMillis - now;

            binding.txtViewSholat.setText("Subuh");
            startCountdownTimer(millisUntilFinished);
        }
    }

    private void startCountdownTimer(long millisUntilFinished) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
            @Override
            public void onTick(long millisLeft) {
                if (binding != null) {
                    binding.countDown.setText("- " + String.format(FORMAT_COUNTDOWN,
                            TimeUnit.MILLISECONDS.toHours(millisLeft),
                            TimeUnit.MILLISECONDS.toMinutes(millisLeft) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisLeft)),
                            TimeUnit.MILLISECONDS.toSeconds(millisLeft) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisLeft))
                    ));
                }
            }

            @Override
            public void onFinish() {
                if (binding != null) {
                    binding.countDown.setText("00:00:00");
                    binding.txtViewSholat.setText("Waktu Sholat Telah Tiba!");
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> calculateAndStartCountdown(), 2000);
            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}