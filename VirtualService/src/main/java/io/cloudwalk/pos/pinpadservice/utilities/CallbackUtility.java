package io.cloudwalk.pos.pinpadservice.utilities;

import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;

public class CallbackUtility {
    private static final String
            TAG = CallbackUtility.class.getSimpleName();

    private static final Semaphore
            sClbkSemaphore = new Semaphore(1, true);

    private static IServiceCallback
            sServiceCallback = null;

    private CallbackUtility() {
        /* Nothing to do */
    }

    public static IServiceCallback getServiceCallback() {
        Log.d(TAG, "getServiceCallback");

        IServiceCallback response;

        sClbkSemaphore.acquireUninterruptibly();

        response = sServiceCallback;

        sClbkSemaphore.release();

        return response;
    }

    public static void setServiceCallback(IServiceCallback callback) {
        Log.d(TAG, "setServiceCallback");

        sClbkSemaphore.acquireUninterruptibly();

        sServiceCallback = callback;

        sClbkSemaphore.release();
    }
}
