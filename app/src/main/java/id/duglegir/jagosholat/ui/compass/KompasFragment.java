package id.duglegir.jagosholat.ui.compass;

import android.Manifest; // <-- IMPORT BARU
import android.content.Context;
import android.content.pm.PackageManager; // <-- IMPORT BARU
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // <-- IMPORT BARU

import androidx.activity.result.ActivityResultLauncher; // <-- IMPORT BARU
import androidx.activity.result.contract.ActivityResultContracts; // <-- IMPORT BARU
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // <-- IMPORT BARU
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map; // <-- IMPORT BARU
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.duglegir.jagosholat.databinding.FragmentKompasBinding;
import id.duglegir.jagosholat.util.KompasGPSTracker;
import id.duglegir.jagosholat.util.KompasRose;

public class KompasFragment extends Fragment implements SensorEventListener {

    private FragmentKompasBinding binding;
    private KompasRose kompasRose;
    private KompasGPSTracker gps;
    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float azimuth = 0f;

    private double latitude, longitude;
    private double Qlati = 21.42243;
    private double Qlongi = 39.82624;
    public static double degree;



    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGrantedMap) -> {

                boolean fineLocationGranted = isGrantedMap.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                boolean coarseLocationGranted = isGrantedMap.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted && coarseLocationGranted) {

                    initializeKompas();
                } else {

                    Toast.makeText(requireContext(), "Izin lokasi ditolak. Kompas tidak dapat berfungsi.", Toast.LENGTH_LONG).show();
                    if (binding != null) {
                        binding.idTvCountryName.setText("Izin lokasi ditolak");
                    }
                }
            });


    public KompasFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentKompasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            initializeKompas();
        } else {

            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

    }




    private void initializeKompas() {

        if (getContext() == null) return;

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }

        kompasRose = new KompasRose(requireContext());

        getlocation();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            String addressString = getAddress(latitude, longitude);
            handler.post(() -> {
                if (binding != null) { // Pastikan fragment masih ada
                    binding.idTvCountryName.setText(addressString);
                }
            });
        });

        degree = bearing(latitude, longitude, Qlati, Qlongi);

        if (binding != null) {
            binding.containerLayout.addView(kompasRose);
        }
        kompasRose.invalidate();
    }


    protected double bearing(double startLat, double startLng, double endLat, double endLng) {
        double longitude1 = startLng;
        double longitude2 = endLng;
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public String getAddress(double lat, double lng) {

        if (getContext() == null || !Geocoder.isPresent()) {
            return "Layanan Geocoder tidak tersedia";
        }
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        String add = "Mencari lokasi...";
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address obj = addresses.get(0);
                add = obj.getLocality();
                if (add == null) {
                    add = obj.getSubAdminArea();
                }
                if (obj.getCountryName() != null) {
                    add = add + ", " + obj.getCountryName();
                }
            }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {

            e.printStackTrace();
            add = "Gagal memuat lokasi";
        }
        return add;
    }

    private void getlocation() {
        if (getContext() == null) return;

        gps = new KompasGPSTracker(requireContext());

        if (gps.canGetLocation()) {

            Location loc = gps.getLocation();

            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            } else {

                latitude = 0.0; // Set default agar tidak crash
                longitude = 0.0;
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi terakhir, sedang mencari...", Toast.LENGTH_SHORT).show();
            }
        } else {

            gps.showSettingsAlert();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
        }
        float R[] = new float[9];
        float I[] = new float[9];
        boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
        if (success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);
            azimuth = (float) Math.toDegrees(orientation[0]);
            azimuth = (azimuth + 360) % 360;

            if (kompasRose != null) {
                kompasRose.setDirections(azimuth, azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}