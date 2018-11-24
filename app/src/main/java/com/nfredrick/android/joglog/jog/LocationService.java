package com.nfredrick.android.joglog.jog;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.nfredrick.android.joglog.JogApplication;
import com.nfredrick.android.joglog.R;
import com.nfredrick.android.joglog.db.JogData;
import com.nfredrick.android.joglog.db.JogDatabase;

import androidx.core.content.ContextCompat;

public class LocationService extends Service {

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private PowerManager.WakeLock mWakeLock;

    private JogDatabase mJogDatabase;
    private int mJogId;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final String TAG = "com.nfredrick.android.joglog.jog.LocationService";
    private static final String WAKE_LOCK_TAG = "JogLog::WakeLockTag";
    public static final String JOG_ID_KEY = "jog id key";
    private static final String NOTIFICATION_TITLE = "Notification title";
    private static final String NOTIFICATION_ID = "Notification id";

    @Override
    public void onCreate() {
        Log.d(TAG, "LocationService onCreate()");
        // Load database
        mJogDatabase = JogDatabase.getDatabase(JogApplication.getContext());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // start location callbacks
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location loc : locationResult.getLocations()) {
                    Log.v(TAG, "Added location result: " + Double.toString(loc.getLatitude()) + ", " + Double.toString(loc.getLongitude()));
                    mJogDatabase.jogLocationsDao().insert(
                            new JogData(mJogId, loc.getLatitude(), loc.getLongitude(), loc.getTime()));
                }
            }
        };

        // start foreground service
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        }

        mNotificationChannel = mNotificationManager.getNotificationChannel(NOTIFICATION_ID);

        if (mNotificationChannel == null) {
            mNotificationChannel = new NotificationChannel(
                    NOTIFICATION_ID, NOTIFICATION_TITLE, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        Notification notification = new Notification.Builder(this, NOTIFICATION_ID)
                .setContentTitle("Jog Log Jogging")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Jog is in progress")
                .build();
        startForeground(Notification.FLAG_FOREGROUND_SERVICE, notification);

        // acquire a wakelock so callbacks can occur while phone is locked
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService onStartCommand()");
        mJogId = Repository.getInstance().getJogId();
        Log.d(TAG, "mJogId = " + Integer.toString(mJogId));
        // create location request
        int sampleInterval = 1000 * 1; // in milliseconds
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(sampleInterval);
        mLocationRequest = request;

        // start location updates
        int permission = ContextCompat.checkSelfPermission(this, LOCATION_PERMISSIONS[0]);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "App has location permission and can start location updates");
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null);
        } else {
            Log.d(TAG, "App does not have location permission and cannot start location updates");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        mGoogleApiClient.disconnect();
        mWakeLock.release();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        return intent;
    }
}