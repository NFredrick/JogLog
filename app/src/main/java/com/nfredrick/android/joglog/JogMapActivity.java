package com.nfredrick.android.joglog;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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
import com.nfredrick.android.joglog.db.JogData;

import java.util.ArrayList;
import androidx.fragment.app.FragmentActivity;


public class JogMapActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mGoogleMap;
    private MapFragment mMapFragment;

    private ArrayList<JogData> mLocations;

    private static final String DATA_KEY = "LOCATION_LIST_DATA";
    private static final String TAG = "JogMapActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jog_map);
        mLocations = (ArrayList<JogData>)getIntent().getExtras().get(DATA_KEY);

        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mMapFragment);
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
        for (JogData loc : mLocations) {

            double lat = loc.latitude;
            double lon = loc.longitude;
            Log.d(TAG, "Adding data point "+ Double.toString(lat) + ", " + Double.toString(lon));
            dataPoints.add(new LatLng(lat, lon));

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

    public static Intent newIntent(Context packageContext, ArrayList<JogData> data) {
        Intent intent = new Intent(packageContext, JogMapActivity.class);
        intent.putExtra(DATA_KEY, data);
        return intent;
    }
}
