package io.cloudwalk.pos.pinpadlibrary;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class PinpadAbstractionLayer extends Application {
    private static final String TAG = PinpadAbstractionLayer.class.getSimpleName();

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
