package com.nfredrick.android.joglog.db;

import android.content.Context;

import com.nfredrick.android.joglog.JogApplication;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {JogData.class}, version = 1, exportSchema = false)
public abstract class JogDatabase extends RoomDatabase {
    private static JogDatabase sInstance;

    public abstract JogDataDao jogLocationsDao();

    public static JogDatabase getDatabase(Context context) {
        synchronized (JogDatabase.class) {
            if (sInstance == null) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(), JogDatabase.class,
                        JogApplication.getDatabaseName())
                        .allowMainThreadQueries()
                        .build();
            }
            return sInstance;
        }
    }


}
