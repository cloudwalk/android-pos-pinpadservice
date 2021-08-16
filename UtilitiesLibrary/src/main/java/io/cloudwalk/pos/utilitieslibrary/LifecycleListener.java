package io.cloudwalk.pos.utilitieslibrary;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import io.cloudwalk.pos.loglibrary.Log;

public class LifecycleListener implements LifecycleObserver {
    private static final String
            TAG = LifecycleListener.class.getSimpleName();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(TAG, "onEnterForeground");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(TAG, "onEnterBackground");
    }
}
