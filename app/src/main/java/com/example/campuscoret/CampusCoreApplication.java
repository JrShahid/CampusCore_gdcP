package com.example.campuscoret;

import android.app.Application;

public class CampusCoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseCampusSync.initialize(this);
    }
}
