package com.nfredrick.android.joglog.db;

import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface JogDataDao {
    @Query("SELECT * FROM jog_data_table WHERE jog_id LIKE :jogId")
    LiveData<List<JogData>> getJogData(int jogId);

    @Query("SELECT * FROM jog_data_table WHERE jog_id LIKE :jogId")
    List<JogData> getSingleJogData(int jogId);

    @Insert
    void insert(JogData jogData);

    @Query("DELETE FROM jog_data_table")
    void clearTable();

}
