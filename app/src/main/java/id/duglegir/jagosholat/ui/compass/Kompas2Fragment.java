package id.duglegir.jagosholat.ui.compass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.duglegir.jagosholat.databinding.FragmentKompas2Binding;
import id.duglegir.jagosholat.util.Kompas2View;

public class Kompas2Fragment extends Fragment implements SensorEventListener {

    private FragmentKompas2Binding binding;
    private Kompas2View kompasView;

    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float azimuth = 0f;
    private float derajatKiblat = 0f;

    private double kaabaLat = 21.4225;
    private double kaabaLng = 39.8262;


    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGrantedMap) -> {

                Boolean fineGranted = isGrantedMap.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseGranted = isGrantedMap.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                boolean fineLocationGranted = (fineGranted != null && fineGranted);
                boolean coarseLocationGranted = (coarseGranted != null && coarseGranted);



                if (fineLocationGranted || coarseLocationGranted) {

                    startGpsSearch();
                } else {

                    Toast.makeText(requireContext(), "Izin lokasi ditolak. Kompas tidak dapat berfungsi.", Toast.LENGTH_LONG).show();
                    if (binding != null) binding.tvLokasi.setText("Izin lokasi ditolak");
                }
            });

    public Kompas2Fragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentKompas2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        kompasView = new Kompas2View(requireContext());
        binding.kompasContainer.addView(kompasView);

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startGpsSearch();
        } else {

            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void startGpsSearch() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvLokasi.setText("Mencari lokasi GPS...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (binding == null) return; // Fragment hancur
                    binding.progressBar.setVisibility(View.GONE);
                    if (location != null) {
                        handleNewLocation(location);
                    } else {
                        binding.tvLokasi.setText("Gagal mendapat lokasi, coba lagi.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvLokasi.setText("Gagal mendapatkan lokasi.");
                });
    }

    private void handleNewLocation(Location location) {
        double userLat = location.getLatitude();
        double userLng = location.getLongitude();

        derajatKiblat = (float) bearing(userLat, userLng, kaabaLat, kaabaLng);

        kompasView.updateDerajatKiblat(derajatKiblat);
        binding.tvDerajat.setText(String.format(Locale.getDefault(), "Kiblat: %.0f°", derajatKiblat));

        loadAddress(userLat, userLng);
    }

    protected double bearing(double startLat, double startLng, double endLat, double endLng) {
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff = Math.toRadians(endLng - startLng);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private void loadAddress(double lat, double lng) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
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
            String finalAddressString = addressString;
            handler.post(() -> {
                if (binding != null) {
                    binding.tvLokasi.setText(finalAddressString);
                }
            });
        });
    }




    @Override
    public void onResume() {
        super.onResume();

        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
            if (magnetometer != null) {
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }
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

            if (kompasView != null) {
                kompasView.updateAzimuth(azimuth);
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