package io.cloudwalk.pos.utilitieslibrary;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import io.cloudwalk.pos.loglibrary.Log;

public class LifecycleObserver implements androidx.lifecycle.LifecycleObserver {
    private static final String
            TAG = LifecycleObserver.class.getSimpleName();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(TAG, "onEnterForeground");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(TAG, "onEnterBackground");
    }
}
