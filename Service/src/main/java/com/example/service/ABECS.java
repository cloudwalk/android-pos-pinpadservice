package com.example.service;

import android.os.Bundle;
import android.util.Log;

import com.example.service.utilities.ServiceUtility;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class ABECS extends IABECS.Stub {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final ABECS sABECS = new ABECS();

    /**
     * Constructor.
     */
    private ABECS() {
        Log.d(TAG_LOGCAT, "ABECS");
    }

    /**
     * @return {@link ABECS}
     */
    public static ABECS getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sABECS;
    }

    /**
     *
     * @param sync
     * @param input
     * @return
     */
    @Override
    public Bundle run(boolean sync, Bundle input) {
        Log.d(TAG_LOGCAT, "run::sync [" + sync + "], input [" + ((input != null) ? input.toString() : null) + "]");

        final Bundle[] output = { null };
        Lock lock = new ReentrantLock(true);

        if (sync) {
            lock.lock();
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    output[0] = ServiceUtility.getInstance().run(sync, input);
                } catch (Exception exception) {
                    Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
                } finally {
                    if (sync) {
                        lock.unlock();
                    }
                }
            }
        }.start();

        if (sync) {
            lock.lock();
        }

        return output[0];
    }
}
