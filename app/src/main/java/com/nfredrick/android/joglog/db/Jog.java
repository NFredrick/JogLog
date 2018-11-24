package com.nfredrick.android.joglog.db;

import java.io.Serializable;
import java.util.Date;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "jog_table")
public class Jog implements Serializable {

    @PrimaryKey
    public int jogId;

    @ColumnInfo(name = "distance")
    public double distance;

    @ColumnInfo(name = "time")
    public long time;

    @ColumnInfo(name = "date")
    public String date;

    public static int id = 1;

    public Jog(int jogId, double distance, long time, String date) {
        this.jogId = jogId;
        this.distance = distance;
        this.time = time;
        this.date = date;
    }

    @Ignore
    public Jog(double distance, long time, String date) {
        this(id++, distance, time, date);
    }
}
