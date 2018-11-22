package com.nfredrick.android.joglog.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface JogDataDao {
    @Query("SELECT * FROM jog_data_table")
    LiveData<List<JogData>> getJogData();

    @Insert
    void insert(JogData jogData);

    @Query("DELETE FROM jog_data_table")
    public void clearTable();

}
