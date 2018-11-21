package com.nfredrick.android.joglog;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class JogViewModel extends ViewModel {

    private Jog mJog;
    private MutableLiveData<Double> mDistance = new MutableLiveData<>();

    private Timer mTimer;
    private TimerTask mTask;
    private MutableLiveData<Integer> mElapsedTime = new MutableLiveData<>();
    private final int mIncrement = 1;

    public LiveData<Double> getDistance() {
        return mDistance;
    }

    public LiveData<Integer> getElapsedTime() {
        return mElapsedTime;
    }

    public void setup() {
        mElapsedTime.setValue(0);
        mDistance.setValue(0.0);
        mJog = new Jog();
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedTime.postValue(mElapsedTime.getValue() + mIncrement);
            }
        };
    }

    public void startJog() {
        mTimer.schedule(mTask, mIncrement*1000, mIncrement*1000);
    }

    public void stopJog() {
        mTimer.cancel();
    }

    public void addLocation(Location location) {
        mJog.addLocation(location);
        mDistance.postValue(mJog.distanceInMiles());
    }

    public ArrayList<Location> getJogLocationData() {
        return mJog.getLocationData();
    }
}
