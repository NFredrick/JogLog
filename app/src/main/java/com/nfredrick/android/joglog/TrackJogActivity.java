package com.nfredrick.android.joglog;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class TrackJogActivity extends AppCompatActivity{


    private static final int MY_PERMISSIONS_LOCATION_REQUEST_CODE = 4321;
    private static final String TAG = "TrackJogFragment";
    private static final String WAKE_LOCK_TAG = "JogLog::WakeLockTag";

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private TextView mElapsedTimeView;
    private TextView mDistanceView;
    private Button mCollectButton;
    private Button mMapButton;
    private Button mStopButton;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private JogViewModel mJogViewModel;

    private PowerManager.WakeLock mWakeLock;

    private boolean mIsRunning;
    private static final String IS_RUNNING = "mIsRunning";

    private final Observer<Double> mDistanceObserver = new Observer<Double>() {
        @Override
        public void onChanged(Double newDistance) {
            mDistanceView.setText(String.format("%.2f", newDistance));
        }
    };

    private final Observer<Integer> mElapsedTimeObserver = new Observer<Integer>() {
        @Override
        public void onChanged(Integer newElapsedTime) {
            mElapsedTimeView.setText(formatTime(newElapsedTime));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsRunning = savedInstanceState.getBoolean(IS_RUNNING, false);
        } else {
            mIsRunning = false;
        }

        mJogViewModel = ViewModelProviders.of(this).get(JogViewModel.class);
        if (!mIsRunning) {
            mJogViewModel.setup();
            acquireWakeLock();
        }

        subscribeDistanceObserver();
        subscribeElapsedTimeObserver();

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
                for (Location location : locationResult.getLocations()) {
                    mJogViewModel.addLocation(location);
                }
            }
        };

        setContentView(R.layout.fragment_track_jog);

        mElapsedTimeView = findViewById(R.id.elapsed_time);

        mDistanceView = findViewById(R.id.total_distance);
        mDistanceView.setText("0.00");

        mCollectButton = findViewById(R.id.collect_run_data_button);
        mCollectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectJogData();
                mJogViewModel.startJog();
                mIsRunning = true;
                mCollectButton.setVisibility(View.GONE);
                mStopButton.setVisibility(View.VISIBLE);
            }
        });

        mStopButton = findViewById(R.id.stop_run_data_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                mJogViewModel.stopJog();
                mWakeLock.release();
                mMapButton.setVisibility(View.VISIBLE);
            }
        });

        if (mIsRunning) {
            mCollectButton.setVisibility(View.GONE);
            mStopButton.setVisibility(View.VISIBLE);
        }

        mMapButton = findViewById(R.id.view_map);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = JogMapActivty.newIntent(TrackJogActivity.this, mJogViewModel.getJogLocationData());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(IS_RUNNING, mIsRunning);
    }

    private void subscribeDistanceObserver() {
        mJogViewModel.getDistance().observe(this, mDistanceObserver);
    }

    private void subscribeElapsedTimeObserver() {
        mJogViewModel.getElapsedTime().observe(this, mElapsedTimeObserver);
    }

    public String formatTime(int time) {
        int hours = time / 3600;
        int minutes = time % 3600 / 60;
        int seconds = time - hours * 3600 - minutes * 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
    }

    private void collectJogData() {
        Log.d(TAG, "entered collectJogData()");
        createLocationRequest();
        startLocationUpdates();
    }

    //
    private void createLocationRequest() {
        int sampleInterval = 1000 * 1; // in milliseconds
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(sampleInterval);
        mLocationRequest = request;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "entered startLocationUpdates");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // permission has not yet been granted
            requestPermissions(LOCATION_PERMISSIONS,
                    MY_PERMISSIONS_LOCATION_REQUEST_CODE);
        }

        if (hasLocationPermission()) {
            Log.d(TAG, "has location permission");
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null);
        } else {
            Log.d(TAG, "no location permission");
        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(this, LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire();
    }
}