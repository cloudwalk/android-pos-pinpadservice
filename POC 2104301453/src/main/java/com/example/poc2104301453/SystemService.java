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

    private Looper looper;

    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);

            Log.w(TAG_LOGCAT, "ServiceHandler");
        }

        @Override
        public void handleMessage(Message msg) {
            Log.w(TAG_LOGCAT, "handleMessage");

            try {
                Thread.sleep(2750 * 2);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            stopSelf(msg.arg1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.w(TAG_LOGCAT, "onBind");

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.w(TAG_LOGCAT, "onStartCommand");

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.w(TAG_LOGCAT, "onCreate");

        HandlerThread handlerThread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        looper = handlerThread.getLooper();
        serviceHandler = new ServiceHandler(looper);
    }

    @Override
    public void onDestroy() {
        Log.w(TAG_LOGCAT, "onDestroy");
    }
}
