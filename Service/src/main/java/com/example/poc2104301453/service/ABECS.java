package com.example.poc2104301453.service;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.service.utilities.ServiceUtility;

import java.util.concurrent.Semaphore;

import static com.example.poc2104301453.library.ABECS.*;
import static com.example.poc2104301453.library.ABECS.RSP_STAT.*;

/**
 *
 */
public class ABECS extends IABECS.Stub {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final Semaphore[] sSemaphoreList = {
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

        sSemaphoreList[1].acquireUninterruptibly();

        currentCaller = sCaller;

        sSemaphoreList[1].release();

        return currentCaller;
    }

    private void setCaller(String caller) {
        sSemaphoreList[1].acquireUninterruptibly();

        sCaller = caller;

        sSemaphoreList[1].release();
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

        ServiceUtility serviceUtility = ServiceUtility.getInstance();
        Bundle output = null;
        String currentCaller = getCaller();

        if (currentCaller != null) {
            if (!currentCaller.equals(caller)) {
                output = new Bundle();

                output.putInt(KEY_STATUS, ST_INTERR.getNumericValue());
                output.putSerializable(KEY_EXCEPTION, new Exception("Already bounded by " + sCaller));

                return output;
            }
        }

        serviceUtility.abort();

        sSemaphoreList[0].acquireUninterruptibly();

        setCaller(caller);

        output = serviceUtility.run(callback, input);

        sSemaphoreList[0].release();

        return output;
    }
}
