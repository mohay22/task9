package com.example.task9;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView locationText;
    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private BroadcastReceiver locationReceiver;
    private boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate called");

        locationText = findViewById(R.id.location_text);
        Button startButton = findViewById(R.id.start_button);
        Button stopButton = findViewById(R.id.stop_button);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d("MainActivity", "Map fragment initialized");
        } else {
            Log.e("MainActivity", "Map fragment is null");
        }

        // Request permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE},
                    PERMISSION_REQUEST_CODE);
            Log.d("MainActivity", "Requesting permissions");
        } else {
            Log.d("MainActivity", "Permissions already granted");
        }

        startButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, LocationTrackingService.class);
            startForegroundService(serviceIntent);
            Log.d("MainActivity", "Start Tracking clicked");
        });

        stopButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, LocationTrackingService.class);
            stopService(serviceIntent);
            locationText.setText("Location: Not tracking");
            Log.d("MainActivity", "Stop Tracking clicked");
        });

        setupLocationReceiver();
    }

    private void setupLocationReceiver() {
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MainActivity", "Broadcast received");
                double latitude = intent.getDoubleExtra(LocationTrackingService.EXTRA_LATITUDE, 0.0);
                double longitude = intent.getDoubleExtra(LocationTrackingService.EXTRA_LONGITUDE, 0.0);
                float bearing = intent.getFloatExtra(LocationTrackingService.EXTRA_BEARING, 0f);
                updateLocationUI(latitude, longitude, bearing);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationReceiver,
                new IntentFilter(LocationTrackingService.ACTION_LOCATION_UPDATE));
        Log.d("MainActivity", "Location receiver registered");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Log.d("MainActivity", "My Location enabled - blue dot should appear");
        } else {
            Log.e("MainActivity", "Location permission not granted - blue dot disabled");
        }
    }

    public void updateLocationUI(double latitude, double longitude, float bearing) {
        runOnUiThread(() -> {
            if (!isMapReady || mMap == null) {
                Log.w("MainActivity", "Map not ready yet, skipping UI update");
                return;
            }
            locationText.setText(String.format("Lat: %.6f, Long: %.6f, Bearing: %.1f", latitude, longitude, bearing));
            LatLng position = new LatLng(latitude, longitude);
            mMap.addPolyline(new PolylineOptions().add(position));
            mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(position, 15));
            Log.d("MainActivity", "UI updated: Lat=" + latitude + ", Long=" + longitude + ", Bearing=" + bearing);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        Log.d("MainActivity", "onDestroy called");
    }
}


























































