package com.nfredrick.android.joglog;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nfredrick.android.joglog.db.JogData;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class TrackJogActivity extends AppCompatActivity {

    private static final String TAG = "TrackJogFragment";
    private static final int MY_PERMISSIONS_LOCATION_REQUEST_CODE = 4321;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private TextView mElapsedTimeView;
    private TextView mDistanceView;
    private Button mCollectButton;
    private Button mMapButton;
    private Button mStopButton;

    private JogViewModel mJogViewModel;

    private boolean mIsRunning;
    private static final String IS_RUNNING = "mIsRunning";

    private final Observer<Double> mDistanceObserver = new Observer<Double>() {
        @Override
        public void onChanged(Double newDistance) {
            mDistanceView.setText(String.format("%.2f", newDistance));
        }
    };

    private final Observer<Long> mElapsedTimeObserver = new Observer<Long>() {
        @Override
        public void onChanged(Long newElapsedTime) {
            mElapsedTimeView.setText(formatTime(newElapsedTime));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Did not yet have location permissions");
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, MY_PERMISSIONS_LOCATION_REQUEST_CODE);
        }

        if (savedInstanceState != null) {
            mIsRunning = savedInstanceState.getBoolean(IS_RUNNING, false);
        } else {
            mIsRunning = false;
        }

        mJogViewModel = ViewModelProviders.of(this).get(JogViewModel.class);

        subscribeDistanceObserver();
        subscribeElapsedTimeObserver();



        setContentView(R.layout.fragment_track_jog);

        mElapsedTimeView = findViewById(R.id.elapsed_time);

        mDistanceView = findViewById(R.id.total_distance);
        mDistanceView.setText("0.00");

        mCollectButton = findViewById(R.id.collect_run_data_button);
        mCollectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                mJogViewModel.stopJog();
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
                ArrayList<JogData> data = (ArrayList<JogData>) mJogViewModel.getJogData().getValue();
                Intent intent = JogMapActivity.newIntent(TrackJogActivity.this, data);
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

    public String formatTime(long time) {
        int hours = (int) time / 3600;
        int minutes = (int) time % 3600 / 60;
        int seconds = (int) time - hours * 3600 - minutes * 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}