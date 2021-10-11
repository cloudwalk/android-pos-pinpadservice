package io.cloudwalk.pos.pinpadservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
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

        Bundle extras = intent.getExtras();

        try {
            boolean overwrite = extras.getBoolean  ("overwrite", false);
            byte[]  keymap    = extras.getByteArray("keymap.dat");

            if (keymap != null) {
                PinpadAbstractionLayer.setConfig(keymap, overwrite);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        return PinpadAbstractionLayer.getInstance();
    }
}
