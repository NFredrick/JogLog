package com.nfredrick.android.joglog.jog;

import com.nfredrick.android.joglog.db.JogData;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;


public class JogViewModel extends ViewModel {

    private Repository sRepository;
    private LiveData<Double> mDistance;
    private LiveData<Long> mElapsedTime;
    private LiveData<List<JogData>> mJogData;
    private static final String TAG = "com.nfredrick.android.joglog.jog.JogViewModel";


    public JogViewModel() {
        sRepository = Repository.getInstance();
        updateLiveData();
    }

    public void updateLiveData() {
        mJogData = sRepository.getJogData();
        mDistance = sRepository.getDistance();
        mElapsedTime = sRepository.getElapsedTime();
    }

    public LiveData<List<JogData>> getJogData() {
        return mJogData;
    }

    public void startJog() {
        sRepository.startJog();
        updateLiveData();
    }

    public void stopJog() {
        sRepository.stopJog();
    }

    public LiveData<Double> getDistance() {
        return mDistance;
    }

    public LiveData<Long> getElapsedTime() {
        return mElapsedTime;
    }

}
