package com.example.poc2104301453;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

public class SystemService extends Service {
    private static final String TAG_LOGCAT = SystemService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.w(TAG_LOGCAT, "onBind");

        return COTS.getInstance();
    }
}
