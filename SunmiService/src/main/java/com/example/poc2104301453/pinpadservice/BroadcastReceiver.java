package com.example.poc2104301453.pinpadservice;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG_LOGCAT = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG_LOGCAT, "onReceive");

        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            Intent service = new Intent();

            service.setClassName(context.getPackageName(), context.getPackageName() + ".PinpadService");

            context.startService(service);
        }
    }
}
