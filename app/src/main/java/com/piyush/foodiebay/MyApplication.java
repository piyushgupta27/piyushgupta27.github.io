package com.piyush.foodiebay;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by piyush on 13/05/16.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crashlytics in order to get Crash reports
        Fabric.with(this, new Crashlytics());
    }
}
