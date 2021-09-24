package io.cloudwalk.pos.pinpadservice;

import android.content.Context;
import android.content.Intent;

import io.cloudwalk.loglibrary.Log;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
    }
}
