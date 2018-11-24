package com.nfredrick.android.joglog.jog;

import android.location.Location;
import android.util.Log;
import com.nfredrick.android.joglog.JogApplication;
import com.nfredrick.android.joglog.db.Jog;
import com.nfredrick.android.joglog.db.JogDao;
import com.nfredrick.android.joglog.db.JogData;
import com.nfredrick.android.joglog.db.JogDataDao;
import com.nfredrick.android.joglog.db.JogDatabase;
import java.util.Calendar;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class Repository {

    private static Repository sRepository;
    private final JogDatabase sJogDatabase;
    private final JogDataDao mJogDataDao;
    private final JogDao mJogDao;
    private Jog mJog;
    private int mJogId;

    private LiveData<List<JogData>> mJogData;
    private LiveData<Double> mDistance;
    private LiveData<Long> mElapsedTime;

    private static final String TAG = "com.nfredrick.android.joglog.jog.Repository";
    private static final double METERS_TO_MILES = 0.000621371;

    private Repository(final JogDatabase database) {
        Log.d(TAG, "Repository");
        sJogDatabase = database;
        mJogDataDao = sJogDatabase.jogLocationsDao();
        mJogDao = sJogDatabase.jogDao();
        mJogId = 0;
        updateLiveData();
    }

    public static Repository getInstance() {
        Log.d(TAG, "getInstance()");
        JogDatabase database = JogDatabase.getDatabase(JogApplication.getContext());
        if (sRepository == null) {
            synchronized (Repository.class) {
                if (sRepository == null) {
                    sRepository = new Repository(database);
                }
            }
        }
        return sRepository;
    }

    private void updateLiveData() {
        mJogData = mJogDataDao.getJogData(mJogId);
        distanceMapper();
        timeMapper();
    }

    private void distanceMapper() {
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
    }

    private void timeMapper() {
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

    public LiveData<Double> getDistance() {
        return mDistance;
    }

    public LiveData<Long> getElapsedTime() {
        return mElapsedTime;
    }

    public void startJog() {
        Log.d(TAG, "startJog()");

        mJogId = getNumberOfJogs() + 1;
        mJog = new Jog(mJogId, 0.0, 0, Calendar.getInstance().getTime().toString());
        updateLiveData();
        Log.d(TAG, "Jog id = " + Integer.toString(mJog.jogId));

        JogApplication.getContext().startForegroundService(LocationService.newIntent(JogApplication.getContext()));
    }

    public void stopJog() {
        Log.d(TAG, "stopJog()");
        JogApplication.getContext().stopService(LocationService.newIntent(JogApplication.getContext()));
        mJog.distance = mDistance.getValue();
        mJog.time = mElapsedTime.getValue();
        mJogDao.insert(mJog);
        Log.d(TAG, "Number of jogs in database = " + Integer.toString(getNumberOfJogs()));
        Log.d(TAG, "Number of data points in most recent jog = " + Integer.toString(mJogData.getValue().size()));
        Log.d(TAG, "Number of data points with non live data query = " + getSingleJogData(mJogId).size());
    }

    public List<Jog> getJogs() {
        return mJogDao.getJogs();
    }

    public List<JogData> getSingleJogData(int jogId) {
        Log.d(TAG, "getSingleJogData");
        List<JogData> data = mJogDataDao.getSingleJogData(jogId);
        Log.d(TAG, "data size = " + data.size());
        return data;
    }

    public int getJogId() {
        return mJogId;
    }

    private int getNumberOfJogs() {
        return mJogDao.getNumberOfJogs();
    }
}