package io.cloudwalk.pos.pinpadservice.utilities;

import android.os.Bundle;
import android.os.SystemClock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;

public class VendorUtility {
    private static final String
            TAG = VendorUtility.class.getSimpleName();

    private static final Semaphore
            sProcessSemaphore = new Semaphore(1, true);

    public static final BlockingQueue<Bundle>
            sRequestQueue  = new LinkedBlockingQueue<>();

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    // TODO: private static class Virtual

    private VendorUtility() {
        Log.d(TAG, "VendorUtility");
    }

    public static void abort() {
        Log.d(TAG, "abort");

        while (sRequestQueue.poll() != null);

        // TODO: platform specific code
    }

    public static void process(Bundle bundle) {
        Log.d(TAG, "process");

        sRequestQueue.offer(bundle);

        new Thread() {
            @Override
            public void run() {
                super.run();

                long[] timestamp = { 0, 0 };

                timestamp[0] = SystemClock.elapsedRealtime();

                sProcessSemaphore.acquireUninterruptibly();

                timestamp[1] = SystemClock.elapsedRealtime();

                Bundle request = sRequestQueue.poll();

                if ((timestamp[1] - timestamp[0]) < 2000 && request != null) {
                    while (sResponseQueue.poll() != null);

                    Bundle response;

                    if (!request.containsKey("response")) {
                        // TODO: sResponseQueue.offer(ACK);

                        // TODO: platform specific code

                        // TODO: sResponseQueue.offer(RSP);
                    } else {
                        sResponseQueue.offer(request);
                    }
                }

                sProcessSemaphore.release();
            }
        }.start();
    }
}
