package io.cloudwalk.pos.pinpadservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import io.cloudwalk.loglibrary.Log;

public class PinpadService extends Service {
    private static final String
            TAG = PinpadService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");

        return PinpadAbstractionLayer.getInstance();
    }
}
