package com.nfredrick.android.joglog;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class JogMapActivty extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mGoogleMap;
    private SupportMapFragment mSupportMapFragment;

    private ArrayList<Location> mLocations;

    private static final String DATA_KEY = "LOCATION_LIST_DATA";
    private static final String TAG = "JogMapActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jog_map);
        mLocations = (ArrayList<Location>)getIntent().getExtras().get(DATA_KEY);

        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mSupportMapFragment.getMapAsync(this);
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
        for (Location loc : mLocations) {

            dataPoints.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            if (lat > maxLatitude) {
                maxLatitude = lat;
            }
            if (lat < minLatitude) {
                minLatitude = lat;
            }
            if (lon > maxLongitude) {
                maxLongitude = lon;
            }
            if (lon < minLongitude) {
                minLongitude = lon;
            }
        }
        Polyline route = mGoogleMap.addPolyline(dataPoints);
        route.setWidth(5.0f);
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(minLatitude, minLongitude),
                new LatLng(maxLatitude, maxLongitude));
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
    }

    public static Intent newIntent(Context packageContext, ArrayList<Location> data) {
        Intent intent = new Intent(packageContext, JogMapActivty.class);
        intent.putExtra(DATA_KEY, data);
        return intent;
    }
}
