package com.nfredrick.android.joglog.db;

import android.location.Location;

import java.io.Serializable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "jog_data_table")
public class JogData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "jog_id")
    public int jogId;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "time")
    public long time;

    public JogData(int jogId, double latitude, double longitude, long time) {
        this.jogId = jogId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

}
