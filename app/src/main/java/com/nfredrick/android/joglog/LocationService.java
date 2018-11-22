package com.nfredrick.android.joglog;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.nfredrick.android.joglog.db.JogData;
import com.nfredrick.android.joglog.db.JogDatabase;

import androidx.core.content.ContextCompat;

public class LocationService extends Service {

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private JogDatabase mJogDatabase;
    private int mJogId;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final String TAG = "LocationService";
    private static final String JOG_ID_KEY = "jog id key";

    @Override
    public void onCreate() {
        Log.d(TAG, "LocationService onCreate()");
        mJogDatabase = JogDatabase.getDatabase(JogApplication.getContext());
        mJogDatabase.jogLocationsDao().clearTable();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService onStartCommand()");
        mJogId = intent.getIntExtra("JOG_ID_KEY", 0);

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
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        mGoogleApiClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Intent newIntent(Context context, int jogId) {
        Intent intent = new Intent(context, LocationService.class);
        Bundle extras = new Bundle();
        if (intent == null)
            Log.d(TAG, "intent is null");
        if (extras == null)
            Log.d(TAG, "extras is null");
        extras.putInt(JOG_ID_KEY, jogId);
        intent.putExtras(extras);
        return intent;
    }
}