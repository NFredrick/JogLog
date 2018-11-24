package com.nfredrick.android.joglog;

import android.app.Application;
import android.content.Context;

import com.nfredrick.android.joglog.db.JogDatabase;
import com.nfredrick.android.joglog.jog.Repository;

public class JogApplication extends Application {

    private static Context sContext;
    private static final String DB_NAME = "jogs.db";

    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getContext() {
        return sContext;
    }

    public static String getDatabaseName() {
        return DB_NAME;
    }

    public JogDatabase getDatabase() {
        return JogDatabase.getDatabase(this);
    }

    public Repository getRepository() {
        return Repository.getInstance();
    }
}
