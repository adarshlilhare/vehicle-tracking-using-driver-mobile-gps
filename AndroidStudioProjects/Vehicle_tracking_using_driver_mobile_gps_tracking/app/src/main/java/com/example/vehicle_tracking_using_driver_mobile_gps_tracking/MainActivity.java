package com.example.vehicle_tracking_using_driver_mobile_gps_tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private TextView tvStatus, tvLatitude, tvLongitude, tvSpeed, tvAccuracy, tvLastUpdate;
    private Button btnStart, btnStop;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        tvSpeed = findViewById(R.id.tv_speed);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        btnStart = findViewById(R.id.btn_start_tracking);
        btnStop = findViewById(R.id.btn_stop_tracking);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateUIWithLocation(location);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(MainActivity.this, "Please enable GPS", Toast.LENGTH_LONG).show();
            }
        };

        btnStart.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                startVehicleTracking();
            }
        });

        btnStop.setOnClickListener(v -> stopVehicleTracking());
    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVehicleTracking();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
        }
    }

    private void startVehicleTracking() {
        if (isTracking) return;

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please turn ON GPS from settings", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,   // 2 seconds
                    5,      // 5 meters
                    locationListener);

            isTracking = true;
            tvStatus.setText("Status: 🚗 TRACKING VEHICLE (LIVE)");
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);

            Toast.makeText(this, "Vehicle Tracking Started", Toast.LENGTH_SHORT).show();

            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) updateUIWithLocation(last);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopVehicleTracking() {
        if (!isTracking) return;
        locationManager.removeUpdates(locationListener);
        isTracking = false;
        tvStatus.setText("Status: Stopped");
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateUIWithLocation(Location location) {
        tvLatitude.setText(String.format("Latitude: %.6f", location.getLatitude()));
        tvLongitude.setText(String.format("Longitude: %.6f", location.getLongitude()));
        tvAccuracy.setText(String.format("Accuracy: %.1f m", location.getAccuracy()));

        if (location.hasSpeed()) {
            float speedKmh = location.getSpeed() * 3.6f;
            tvSpeed.setText(String.format("Speed: %.1f km/h", speedKmh));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        tvLastUpdate.setText("Last update: " + sdf.format(new Date()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracking) stopVehicleTracking();
    }
}