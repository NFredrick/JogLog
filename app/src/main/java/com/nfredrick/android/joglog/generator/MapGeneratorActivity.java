package com.nfredrick.android.joglog.generator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.nfredrick.android.joglog.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapGeneratorActivity extends AppCompatActivity {

    private Button mGenerateButton;
    private Button mMapButton;
    private EditText mDistanceEditText;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private String mLocationProvider;
    private Location mCurrentLocation;

    private double mDistance;
    private ListProperty mListProperty;

    private Context mContext;


    private static final String TAG = "com.nfredrick.android.joglog.generator.MapGeneratorActivity";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int MY_PERMISSIONS_LOCATION_REQUEST_CODE = 54321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MapGeneratorActivity.onCreate");
        setContentView(R.layout.activity_map_generator);

        mDistanceEditText = findViewById(R.id.gen_distance_edittext);

        mLocationProvider = LocationManager.GPS_PROVIDER;

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mGenerateButton = findViewById(R.id.start_gen_button);
        mGenerateButton.setOnClickListener((View v) -> {
            Log.d(TAG, "mGenerateButton clicked");
                    mDistance = Double.parseDouble(mDistanceEditText.getText().toString());
                    Log.d(TAG, "distance entered = " + mDistance);
                    setupSingleLocationUpdate();

                }
        );

        mMapButton = findViewById(R.id.gen_map_button);
        mMapButton.setOnClickListener((View v) -> {

            ArrayList<LatLng> lst = mListProperty.getLst();
            Intent intent = GeneratedMapActivity.newIntent(MapGeneratorActivity.this, lst);
            startActivity(intent);
        });
        mMapButton.setVisibility(View.INVISIBLE);

        mListProperty = new ListProperty();
        mListProperty.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                mMapButton.setVisibility(View.VISIBLE);
            }
        });

        mContext = this;

    }

    public void setupSingleLocationUpdate() {
        Log.d(TAG, "creating location listener");
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged");
                mCurrentLocation = location;
                mLocationManager.removeUpdates(mLocationListener);
                MapGenerator.calculateRoute(mCurrentLocation, mDistance, mContext, mListProperty);
                mMapButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Did not yet have location permissions");
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, MY_PERMISSIONS_LOCATION_REQUEST_CODE);
        }
        mLocationManager.requestLocationUpdates(mLocationProvider, 0, 0, mLocationListener);
    }



}
