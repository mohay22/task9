package com.example.task9;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.location.*;

public class LocationTrackingService extends Service {
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final String TAG = "LocationService";
    public static final String ACTION_LOCATION_UPDATE = "com.example.task9.LOCATION_UPDATE";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_BEARING = "bearing";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        setupLocationCallback();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        Notification initialNotification = createNotification("Tracking started");
        startForeground(1, initialNotification);
        startLocationUpdates();
        return START_STICKY;
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w(TAG, "LocationResult is null");
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    float bearing = location.hasBearing() ? location.getBearing() : 0f;
                    Log.d(TAG, "Location update: Lat=" + latitude + ", Long=" + longitude +
                            ", Bearing=" + bearing + ", Accuracy=" + location.getAccuracy());
                    if (location.hasAccuracy() && location.getAccuracy() < 20) {
                        updateNotification(latitude, longitude);
                        Intent intent = new Intent(ACTION_LOCATION_UPDATE);
                        intent.putExtra(EXTRA_LATITUDE, latitude);
                        intent.putExtra(EXTRA_LONGITUDE, longitude);
                        intent.putExtra(EXTRA_BEARING, bearing);
                        LocalBroadcastManager.getInstance(LocationTrackingService.this).sendBroadcast(intent);
                        Log.d(TAG, "Broadcast sent with accurate location");
                    } else {
                        Log.d(TAG, "Location too inaccurate: Accuracy=" + location.getAccuracy());
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(500) // 0.5 seconds
                .setFastestInterval(250) // 0.25 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(1); // 1-meter movement

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
            Log.d(TAG, "Location updates requested with 0.5s interval");
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Notification createNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cargo Tracking")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        Log.d(TAG, "Notification created with message: " + message);
        return notification;
    }

    private void updateNotification(double latitude, double longitude) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        String locationText = String.format("Lat: %.6f, Long: %.6f", latitude, longitude);
        Notification updatedNotification = createNotification(locationText);
        manager.notify(1, updatedNotification);
        Log.d(TAG, "Notification updated: " + locationText);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
        Log.d(TAG, "Service destroyed");
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Shows location updates for cargo tracking");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        Log.d(TAG, "Notification channel created with IMPORTANCE_HIGH");
    }
}