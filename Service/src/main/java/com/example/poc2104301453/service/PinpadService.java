package com.example.poc2104301453.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 *
 */
public class PinpadService extends Service {
    private static final String TAG_LOGCAT = PinpadService.class.getSimpleName();

    /**
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG_LOGCAT, "onBind");

        return ABECS.getInstance();
    }
}
