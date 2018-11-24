package com.nfredrick.android.joglog.jog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.nfredrick.android.joglog.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class JogActivity extends AppCompatActivity {

    private static final String TAG = "com.nfredrick.android.joglog.jog.JogActivity";
    private static final int MY_PERMISSIONS_LOCATION_REQUEST_CODE = 4321;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private TextView mElapsedTimeView;
    private TextView mDistanceView;
    private Button mCollectButton;
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

        setContentView(R.layout.activity_jog);

        mElapsedTimeView = findViewById(R.id.elapsed_time);

        mDistanceView = findViewById(R.id.total_distance);

        mCollectButton = findViewById(R.id.collect_run_data_button);
        mCollectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJogViewModel.startJog();
                subscribeDistanceObserver();
                subscribeElapsedTimeObserver();
                mIsRunning = true;
                mCollectButton.setVisibility(View.INVISIBLE);
                mStopButton.setVisibility(View.VISIBLE);
            }
        });

        mStopButton = findViewById(R.id.stop_run_data_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mJogViewModel.stopJog();
                mCollectButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.INVISIBLE);

                Toast.makeText(JogActivity.this, "Jog Saved", Toast.LENGTH_SHORT).show();
            }
        });

        if (mIsRunning) {
            mCollectButton.setVisibility(View.GONE);
            mStopButton.setVisibility(View.VISIBLE);
        }
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

    public static String formatTime(long time) {
        int hours = (int) time / 3600;
        int minutes = (int) time % 3600 / 60;
        int seconds = (int) time - hours * 3600 - minutes * 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}