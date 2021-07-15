package io.cloudwalk.pos.utilitieslibrary;

import android.content.Context;
import android.util.Log;

public class Application extends android.app.Application {
    private static final String TAG = Application.class.getSimpleName();

    private static Context sPackageContext = null;

    public static Context getPackageContext() {
        return Application.sPackageContext;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

        Application.sPackageContext = getApplicationContext();
    }
}
