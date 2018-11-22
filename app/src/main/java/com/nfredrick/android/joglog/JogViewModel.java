package com.nfredrick.android.joglog;


import android.location.Location;

import com.nfredrick.android.joglog.db.JogData;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;


public class JogViewModel extends ViewModel {

    private Repository sRepository;
    private LiveData<Double> mDistance;
    private LiveData<Long> mElapsedTime;
    private LiveData<List<JogData>> mJogData;
    private static final double METERS_TO_MILES = 0.000621371;

    public JogViewModel() {
        sRepository = Repository.getInstance();
        mJogData = sRepository.getJogData();
        mDistance = Transformations.map(mJogData, mJogData -> {
            double distance = 0;
            for (int i = 1; i < mJogData.size(); i++) {
                float[] res = new float[10];
                Location.distanceBetween(
                        mJogData.get(i-1).latitude,
                        mJogData.get(i-1).longitude,
                        mJogData.get(i).latitude,
                        mJogData.get(i).longitude,
                        res);
                distance += res[0]*METERS_TO_MILES;
            }
            return distance;
        });
        mElapsedTime = Transformations.map(mJogData, mJogData -> {
            long time = 0;
            if (mJogData.size() == 0) {
                return time;
            }
            time = mJogData.get(mJogData.size()-1).time - mJogData.get(0).time;
            return time/1000;
        });
    }

    public LiveData<List<JogData>> getJogData() {
        return mJogData;
    }

    public void startJog() {
        sRepository.startJog(1);
    }

    public void stopJog() {
        sRepository.stopJog(1);
    }

    public LiveData<Double> getDistance() {
        return mDistance;
    }

    public LiveData<Long> getElapsedTime() {
        return mElapsedTime;
    }

}
