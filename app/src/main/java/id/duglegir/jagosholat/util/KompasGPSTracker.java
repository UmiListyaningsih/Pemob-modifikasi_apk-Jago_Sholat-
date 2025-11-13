package id.duglegir.jagosholat.util;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;



public class KompasGPSTracker {

    private final Context mContext;
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    double latitude;
    double longitude;




    public KompasGPSTracker(Context context) {
        this.mContext = context;

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null; // Gagal mendapatkan LocationManager
            }

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

                this.canGetLocation = false;
            } else {
                this.canGetLocation = true;

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Log.e("KompasGPSTracker", "Izin lokasi tidak diberikan.");
                    this.canGetLocation = false;
                    return null;
                }

                if (isNetworkEnabled) {
                    try {

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                            @Override public void onLocationChanged(@NonNull Location location) {}
                            @Override public void onProviderDisabled(@NonNull String provider) {}
                            @Override public void onProviderEnabled(@NonNull String provider) {}
                            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                        });
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            return location;
                        }
                    } catch (SecurityException e) {
                        Log.e("KompasGPSTracker", "SecurityException di Network", e);
                        this.canGetLocation = false;
                        return null;
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) { // Hanya jika lokasi masih null
                        try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                                @Override public void onLocationChanged(@NonNull Location location) {}
                                @Override public void onProviderDisabled(@NonNull String provider) {}
                                @Override public void onProviderEnabled(@NonNull String provider) {}
                                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                            });
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                return location;
                            }
                        } catch (SecurityException e) {
                            Log.e("KompasGPSTracker", "SecurityException di GPS", e);
                            this.canGetLocation = false;
                            return null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location; // Kembalikan 'location' (bisa jadi null)
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {

        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationManager != null) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            this.canGetLocation = isGPSEnabled || isNetworkEnabled;
        } else {
            this.canGetLocation = false;
        }
        return this.canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Pengaturan GPS");
        alertDialog.setMessage("GPS tidak aktif. Apakah Anda ingin ke menu pengaturan?");

        alertDialog.setPositiveButton("Pengaturan", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });

        alertDialog.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }






}