package com.example.poc2104301453.service;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.service.utilities.ServiceUtility;

import java.util.concurrent.Semaphore;

import static com.example.poc2104301453.library.ABECS.*;

/**
 *
 */
public class ABECS extends IABECS.Stub {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final Semaphore[] sOperationSemaphoreList = {
            new Semaphore(1, true),
            new Semaphore(1, true)
    };

    private static String sCaller = null;

    private static final ABECS sABECS = new ABECS();

    /**
     * Constructor.
     */
    private ABECS() {
        Log.d(TAG_LOGCAT, "ABECS");
    }

    private String getCaller() {
        String currentCaller = null;

        sOperationSemaphoreList[1].acquireUninterruptibly();

        currentCaller = sCaller;

        sOperationSemaphoreList[1].release();

        return currentCaller;
    }

    private void setCaller(String caller) {
        sOperationSemaphoreList[1].acquireUninterruptibly();

        sCaller = caller;

        sOperationSemaphoreList[1].release();
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
    public Bundle run(String caller, IServiceCallback callback, Bundle input) {
        Log.d(TAG_LOGCAT, "run::caller [" + caller + "]");

        Bundle output = null;

        String currentCaller = getCaller();

        if (currentCaller != null) {
            if (!currentCaller.equals(caller)) {
                output = new Bundle();

                output.putInt(KEY_STATUS, 40);
                output.putSerializable(KEY_EXCEPTION, new Exception("Bounded by " + sCaller + " (wait for a " + VALUE_REQUEST_CLO + " request)"));

                return output;
            } else {
                /* TODO: <<CAN>> */

                /* 2021-05-17: according to ABECS specification v2.12 - section 2.2.2.3 - a caller's request should
                 * always start with a <<CAN>> byte */
            }
        }

        sOperationSemaphoreList[0].acquireUninterruptibly();

        setCaller(caller);

        output = ServiceUtility.getInstance().run(callback, input);

        sOperationSemaphoreList[0].release();

        return output;
    }
}
