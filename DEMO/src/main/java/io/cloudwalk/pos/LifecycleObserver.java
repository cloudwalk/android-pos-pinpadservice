package io.cloudwalk.pos;

import static android.content.Context.DISPLAY_SERVICE;
import static android.system.OsConstants.EXIT_SUCCESS;
import static android.view.Display.STATE_OFF;

import static io.cloudwalk.pos.Application.sPinpadServer;

import android.hardware.display.DisplayManager;
import android.view.Display;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.concurrent.atomic.AtomicBoolean;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.pinpadserver.PinpadServer;
import io.cloudwalk.utilitieslibrary.Application;

public class LifecycleObserver implements androidx.lifecycle.LifecycleObserver {
    private static final String
            TAG = LifecycleObserver.class.getSimpleName();

    public static AtomicBoolean
            sBackground = new AtomicBoolean(false);

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP) // TODO: prefer `DefaultLifecycleObserver` or
                                               // `LifecycleEventObserver` instead
    public void onEnterBackground() {
        Log.d(TAG, "onEnterBackground");

        DisplayManager manager = (DisplayManager) Application
                .getContext()
                .getSystemService(DISPLAY_SERVICE);

        // 2022-03-18: some OS versions - e.g. 7.1.1+ - throw the application to background when
        // the display goes off...
        for (Display entry : manager.getDisplays()) {
            int state = entry.getState();

            if (state != STATE_OFF) {
                Log.d(TAG, "entry.getState() [" + state + "]");

                sBackground.set(true);

                PinpadManager.abort();

                PinpadServer server = sPinpadServer.get();

                if (server != null) {
                    server.close();
                }

                try {
                    int pid = android.os.Process.myPid();

                    android.os.Process.killProcess(pid);
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));

                    System.exit(EXIT_SUCCESS);
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(TAG, "onEnterForeground");

        sBackground.set(false);
    }
}
