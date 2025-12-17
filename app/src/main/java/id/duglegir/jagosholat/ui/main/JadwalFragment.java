package id.duglegir.jagosholat.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import id.duglegir.jagosholat.databinding.FragmentJadwalBinding;
import id.duglegir.jagosholat.util.AlarmScheduler;
import id.duglegir.jagosholat.util.PrayerTimeStorage;

public class JadwalFragment extends Fragment {

    // ================= FIX UTAMA =================
    private FragmentJadwalBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    // =============================================

    private RequestQueue volleyQueue;
    private ExecutorService executor;
    private Handler handler;
    private CountDownTimer countDownTimer;

    private static final String[] PRAYER_NAMES = {
            "Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"
    };
    private static final String FORMAT_COUNTDOWN = "%02d:%02d:%02d";

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    (Map<String, Boolean> result) -> {

                        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                        if ((fine != null && fine) || (coarse != null && coarse)) {
                            startGpsFetch();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Izin lokasi ditolak",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

    public JadwalFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentJadwalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        volleyQueue = Volley.newRequestQueue(requireContext());
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            startGpsFetch();

        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void startGpsFetch() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        binding.txtWaktuShubuh.setText("Mencari lokasi...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fetchPrayerTimesFromApi(location);
                        loadAddress(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    } else {
                        binding.txtWaktuShubuh.setText("Lokasi tidak ditemukan");
                    }
                })
                .addOnFailureListener(e ->
                        binding.txtWaktuShubuh.setText("Error lokasi")
                );
    }

    private void fetchPrayerTimesFromApi(Location location) {

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        String url = String.format(
                Locale.US,
                "https://api.aladhan.com/v1/timings?latitude=%.4f&longitude=%.4f&method=9",
                lat, lon
        );

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject timings =
                                response.getJSONObject("data")
                                        .getJSONObject("timings");

                        String subuh = timings.getString("Fajr");
                        String dzuhur = timings.getString("Dhuhr");
                        String ashar = timings.getString("Asr");
                        String maghrib = timings.getString("Maghrib");
                        String isya = timings.getString("Isha");

                        binding.txtWaktuShubuh.setText(subuh);
                        binding.txtWaktuDzuhur.setText(dzuhur);
                        binding.txtWaktuAshar.setText(ashar);
                        binding.txtWaktuMaghrib.setText(maghrib);
                        binding.txtWaktuIsya.setText(isya);

                        Context ctx = requireContext();
                        PrayerTimeStorage.savePrayerTime(ctx, "Subuh", subuh);
                        PrayerTimeStorage.savePrayerTime(ctx, "Dzuhur", dzuhur);
                        PrayerTimeStorage.savePrayerTime(ctx, "Ashar", ashar);
                        PrayerTimeStorage.savePrayerTime(ctx, "Maghrib", maghrib);
                        PrayerTimeStorage.savePrayerTime(ctx, "Isya", isya);

                        AlarmScheduler.scheduleAlarms(ctx);
                        calculateAndStartCountdown();

                    } catch (Exception e) {
                        binding.txtWaktuShubuh.setText("Gagal parsing data");
                    }
                },
                error -> binding.txtWaktuShubuh.setText("Gagal ambil jadwal")
        );

        volleyQueue.add(request);
    }

    private void loadAddress(double lat, double lon) {
        executor.execute(() -> {
            String alamat = "Lokasi tidak diketahui";

            if (Geocoder.isPresent()) {
                Geocoder geocoder =
                        new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> list =
                            geocoder.getFromLocation(lat, lon, 1);
                    if (list != null && !list.isEmpty()) {
                        Address a = list.get(0);
                        alamat = a.getLocality() != null
                                ? a.getLocality()
                                : a.getSubAdminArea();
                    }
                } catch (IOException ignored) {
                }
            }

            String finalAlamat = alamat;
            handler.post(() -> {
                if (binding != null) {
                    binding.tvLokasi.setText(finalAlamat);
                }
            });
        });
    }

    private long getPrayerTimeInMillis(Context context, String name) {
        String time = PrayerTimeStorage.getPrayerTime(context, name);
        if (time.equals("00:00")) return 0;

        try {
            String[] p = time.split(":");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(p[0]));
            c.set(Calendar.MINUTE, Integer.parseInt(p[1]));
            c.set(Calendar.SECOND, 0);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private void calculateAndStartCountdown() {

        long now = System.currentTimeMillis();
        ArrayList<Long> times = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        for (String n : PRAYER_NAMES) {
            long t = getPrayerTimeInMillis(requireContext(), n);
            if (t > 0) {
                times.add(t);
                names.add(n);
            }
        }

        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) > now) {
                binding.txtViewSholat.setText(names.get(i));
                startCountdownTimer(times.get(i) - now);
                return;
            }
        }
    }

    private void startCountdownTimer(long millis) {

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long m) {
                binding.countDown.setText(
                        "- " + String.format(
                                FORMAT_COUNTDOWN,
                                TimeUnit.MILLISECONDS.toHours(m),
                                TimeUnit.MILLISECONDS.toMinutes(m) % 60,
                                TimeUnit.MILLISECONDS.toSeconds(m) % 60
                        )
                );
            }

            @Override
            public void onFinish() {
                binding.countDown.setText("00:00:00");
                binding.txtViewSholat.setText("Waktu Sholat");
                new Handler(Looper.getMainLooper())
                        .postDelayed(
                                JadwalFragment.this::calculateAndStartCountdown,
                                2000
                        );
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        binding = null;
    }
}
