package io.cloudwalk.pos.pinpadservice;

import android.content.Context;
import android.content.Intent;

import io.cloudwalk.pos.loglibrary.Log;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            /* 2021-07-15: deprecated... */

            // Intent service = new Intent();
            // service.setClassName(context.getPackageName(), context.getPackageName() + ".PinpadService");
            // context.startService(service);
        }
    }
}
