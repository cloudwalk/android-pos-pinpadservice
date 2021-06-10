package com.example.poc2104301453.pinpadlibrary;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class PinpadAbstractionLayer extends Application {
    private static final String TAG_LOGCAT = PinpadAbstractionLayer.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    /**
     * @return application context
     */
    public static Context getContext() {
        return PinpadAbstractionLayer.sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PinpadAbstractionLayer.sContext = getApplicationContext();
    }
}
