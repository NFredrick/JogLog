package com.nfredrick.android.joglog.generator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nfredrick.android.joglog.R;
import com.nfredrick.android.joglog.db.JogData;
import com.nfredrick.android.joglog.log.JogMapActivity;

import java.util.ArrayList;

public class GeneratedMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private MapFragment mMapFragment;

    private ArrayList<LatLng> mLocations;

    private static final String DATA_KEY = "LOCATION_LIST_DATA";
    private static final String TAG = "com.nfredrick.android.joglog.log.GeneratedMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_map);
        mLocations = (ArrayList<LatLng>)getIntent().getExtras().get(DATA_KEY);
        Log.d(TAG, "Number of data points = " + mLocations.size());

        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.generated_map, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        drawRoute();
    }

    public void drawRoute() {
        double maxLatitude = 0.0;
        double minLatitude = 90.0;
        double maxLongitude = -180.0;
        double minLongitude = 180.0;

        PolylineOptions dataPoints = new PolylineOptions();
        for (LatLng loc : mLocations) {
            dataPoints.add(loc);
            maxLatitude = Math.max(maxLatitude, loc.latitude);
            minLatitude = Math.min(minLatitude, loc.latitude);
            maxLongitude = Math.max(maxLongitude, loc.longitude);
            minLongitude = Math.min(minLatitude, loc.longitude);
        }
        Polyline route = mGoogleMap.addPolyline(dataPoints);
        route.setWidth(5.0f);
        Log.d(TAG, "min latitude = " + minLatitude);
        Log.d(TAG, "min longitude = " + minLongitude);
        Log.d(TAG, "max latitude = " + maxLatitude);
        Log.d(TAG, "max longitude = " + maxLongitude);
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(minLatitude, minLongitude),
                new LatLng(maxLatitude, maxLongitude));
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int minMetric = Math.min(width, height);
        int padding = (int) (minMetric * 0.40);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
    }

    public static Intent newIntent(Context packageContext, ArrayList<LatLng> data) {
        Intent intent = new Intent(packageContext, GeneratedMapActivity.class);
        intent.putExtra(DATA_KEY, data);
        return intent;
    }
}
