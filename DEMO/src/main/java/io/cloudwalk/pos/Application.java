package io.cloudwalk.pos;

import static io.cloudwalk.loglibrary.Log.DEBUG;

import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.concurrent.atomic.AtomicReference;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadserver.PinpadServer;

public class Application extends io.cloudwalk.utilitieslibrary.Application {
    private static final String
            TAG = Application.class.getSimpleName();

    public static AtomicReference<PinpadServer>
            sPinpadServer = new AtomicReference<>(null);

    @Override
    public void onCreate() {
        Log.s(DEBUG, TAG, "onCreate");

        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver());
    }
}
