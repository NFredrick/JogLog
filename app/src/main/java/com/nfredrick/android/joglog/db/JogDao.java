package com.nfredrick.android.joglog.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface JogDao {
    @Query("SELECT * FROM jog_table")
    List<Jog> getJogs();

    @Query("SELECT COUNT() FROM jog_table")
    int getNumberOfJogs();

    @Insert
    void insert(Jog jog);
}
