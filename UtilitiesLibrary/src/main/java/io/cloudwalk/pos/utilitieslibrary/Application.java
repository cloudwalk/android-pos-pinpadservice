package io.cloudwalk.pos.utilitieslibrary;

import android.content.Context;

import androidx.lifecycle.ProcessLifecycleOwner;

import io.cloudwalk.pos.loglibrary.Log;

public class Application extends android.app.Application {
    private static final String
            TAG = Application.class.getSimpleName();

    private static Context
            sPackageContext = null;

    public static Context getPackageContext() {
        return Application.sPackageContext;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

        LifecycleListener lifecycleListener = new LifecycleListener();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleListener);

        Application.sPackageContext = getApplicationContext();
    }
}
