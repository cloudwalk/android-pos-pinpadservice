package com.example.poc2104301453;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.example.poc2104301453.utilities.UTL;

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
        Log.w(TAG_LOGCAT, "ABECS");
    }

    /**
     *
     * @param sync
     * @param callback
     * @param input
     */
    private Bundle process(boolean sync, IServiceCallback callback, Bundle input) {
        final Bundle[] output = { null };
        Lock lock = new ReentrantLock(true);

        lock.lock();

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    UTL utl = UTL.getInstance();

                    if (input != null) {
                        output[0] = utl.run(input);
                    } else {
                        output[0] = utl.register(callback);
                    }
                } catch (Exception exception) {
                    Log.w(TAG_LOGCAT, exception);
                } finally {
                    lock.unlock();
                }
            }
        }.start();

        if (sync) {
            lock.lock();
        }

        return output[0];
    }

    /**
     * @return {@link ABECS}
     */
    public static ABECS getInstance() {
        Log.w(TAG_LOGCAT, "getInstance");

        return sABECS;
    }

    /**
     *
     * @param sync
     * @param callback
     * @throws RemoteException
     */
    @Override
    public Bundle register(boolean sync, IServiceCallback callback) {
        return process(sync, callback, null);
    }

    /**
     *
     * @param sync
     * @param input
     * @return
     */
    @Override
    public Bundle run(boolean sync, Bundle input) {
        return process(sync, null, input);
    }
}
