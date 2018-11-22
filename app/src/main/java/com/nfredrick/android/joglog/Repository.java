package com.nfredrick.android.joglog;

import android.app.Service;

import com.nfredrick.android.joglog.db.JogData;
import com.nfredrick.android.joglog.db.JogDatabase;

import java.util.List;

import androidx.lifecycle.LiveData;

public class Repository {

    private static Repository sRepository;
    private final JogDatabase sJogDatabase;
    private LocationService mLocationService;

    private LiveData<List<JogData>> mJogData;

    private Repository(final JogDatabase database) {
        sJogDatabase = database;
        mJogData = sJogDatabase.jogLocationsDao().getJogData();
    }

    public static Repository getInstance() {
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

    public LiveData<List<JogData>> getJogData() {
        return mJogData;
    }

    public void startJog(int jogId) {
        JogApplication.getContext().startService(LocationService.newIntent(JogApplication.getContext(), jogId));
    }

    public void stopJog(int jogId) {
        JogApplication.getContext().stopService(LocationService.newIntent(JogApplication.getContext(), jogId));
    }
}