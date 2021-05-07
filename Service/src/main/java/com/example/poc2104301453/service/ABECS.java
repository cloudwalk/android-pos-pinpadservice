package com.example.poc2104301453.service;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.service.utilities.ServiceUtility;

import java.util.concurrent.Semaphore;

/**
 *
 */
public class ABECS extends IABECS.Stub {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final Semaphore sSemaphore = new Semaphore(1, true);

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
     * @param input
     * @return
     */
    @Override
    public Bundle run(IStatusCallback callback, Bundle input) {
        Log.d(TAG_LOGCAT, "run");

        sSemaphore.acquireUninterruptibly();

        Bundle output = ServiceUtility.getInstance().run(callback, input);

        sSemaphore.release();

        return output;
    }
}
