package com.nfredrick.android.joglog;

import android.databinding.BaseObservable;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class Jog extends BaseObservable{

    private static final double METERS_TO_MILES = 0.000621371;

    private List<Location> mLocations;
    private double mDistanceMeters;

    public Jog() {
        mLocations = new ArrayList<>();
        mDistanceMeters = 0;
    }

    public void addLocation(Location newLocation) {
        if (mLocations.size() != 0) {
            mDistanceMeters += mLocations.get(mLocations.size()-1).distanceTo(newLocation);
        }
        mLocations.add(newLocation);
    }

    public double distanceInMiles() {
        return mDistanceMeters * METERS_TO_MILES;
    }

    public double timeInSeconds() {
        long start = mLocations.get(0).getTime();
        long end = mLocations.get(mLocations.size()-1).getTime();
        return (double) (end - start) / 1000;
    }

    public ArrayList<Location> getLocationData() {
        return new ArrayList<>(mLocations);
    }
}
