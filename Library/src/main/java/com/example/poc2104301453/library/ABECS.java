package com.example.poc2104301453.library;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.poc2104301453.service.IABECS;
import com.example.poc2104301453.service.IServiceCallback;

import org.jetbrains.annotations.NotNull;

import java.net.UnknownServiceException;
import java.util.concurrent.Semaphore;

public class ABECS {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final String CLASS_POC_2104301453_PINPAD_SERVICE = "com.example.poc2104301453.service.PinpadService";

    private static final String PACKAGE_POC_2104301453 = "com.example.poc2104301453.service";

    private static Callback sCallback = null;

    private static IBinder sService = null;

    private static ServiceConnection sServiceConnection = null;

    private static final Semaphore sConnectionSemaphore = new Semaphore(1, true);

    private static final Semaphore[] sOperationSemaphoreList = {
            new Semaphore(1, true),
            new Semaphore(1, true)
    };

    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    /**
     * @return {@link IBinder}
     */
    private static IBinder getService() {
        IBinder service;

        sConnectionSemaphore.acquireUninterruptibly();

        service = sService;

        sConnectionSemaphore.release();

        return service;
    }

    /**
     * Translates an instance of {@link Callback} to a new instance of
     * {@link IServiceCallback.Stub}.
     *
     * @param callback {@link Callback}
     * @return {@link IServiceCallback.Stub}
     */
    private static IServiceCallback getServiceCallback(Callback callback) {
        return new IServiceCallback.Stub() {
            @Override
            public void onFailure(Bundle output) {
                output.get(null);

                if (callback != null) {
                    if (callback.status != null) {
                        callback.status.onFailure(output);
                    }
                }
            }

            @Override
            public void onSuccess(Bundle output) {
                output.get(null);

                if (callback != null) {
                    if (callback.status != null) {
                        callback.status.onSuccess(output);
                    }
                }
            }
        };
    }

    /**
     *
     * @param input
     * @return
     */
    private static Bundle queryService(Bundle input) {
        Bundle output = null;

        sOperationSemaphoreList[1].acquireUninterruptibly();

        try {
            if (getService() == null) {
                bindService();
            }

            output = IABECS.Stub.asInterface(getService()).run(sContext.getPackageName(), getServiceCallback(sCallback), input);

            if (output != null) {
                output.get(null);
            }

            if (input.getString(KEY_REQUEST).equals(VALUE_REQUEST_CLO)) {
                unbindService();
            }
        } catch(Exception exception) {
            output = new Bundle();

            output.putInt(KEY_STATUS, 40);
            output.putSerializable(KEY_EXCEPTION, exception);
        }

        sOperationSemaphoreList[1].release();

        return output;
    }

    /**
     * @throws Exception self-descriptive
     */
    private static void bindService()
            throws Exception {
        final Semaphore[] sSyncSemaphore = { new Semaphore(0, true) };

        Intent intent = new Intent();

        intent.setClassName(PACKAGE_POC_2104301453, CLASS_POC_2104301453_PINPAD_SERVICE);

        sServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(ABECS.TAG_LOGCAT, "onServiceConnected::name [" + name + "], service [" + service + "]");

                setService(service);

                sSyncSemaphore[0].release();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG_LOGCAT, "onServiceDisconnected::name [" + name + "]");
            }
        };

        boolean serviceBind = sContext.bindService(intent, sServiceConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG_LOGCAT, "onCreate::serviceBind [" + serviceBind + "]");

        if (serviceBind) {
            sSyncSemaphore[0].acquireUninterruptibly();
        } else {
            throw new UnknownServiceException("Unable to bind to " + CLASS_POC_2104301453_PINPAD_SERVICE);
        }
    }

    /**
     * @throws Exception self-descriptive
     */
    private static void unbindService()
            throws Exception {
        if (sServiceConnection != null) {
            sContext.unbindService(sServiceConnection);
        }

        setService(null);

        sServiceConnection = null;
    }

    /**
     * @param service {@link IBinder}
     */
    private static void setService(IBinder service) {
        sConnectionSemaphore.acquireUninterruptibly();

        sService = service;

        sConnectionSemaphore.release();
    }

    public static final String KEY_EXCEPTION = "exception";

    public static final String KEY_REQUEST = "request";

    public static final String KEY_STATUS = "status";

    public static final String KEY_SYNCHRONOUS_OPERATION = "synchronous_operation";

    public static final String VALUE_REQUEST_OPN = "OPN";

    public static final String VALUE_REQUEST_GIN = "GIN";

    // public static final String VALUE_REQUEST_GIX = "GIX";

    // public static final String VALUE_REQUEST_DWK = "DWK";

    public static final String VALUE_REQUEST_CLO = "CLO";

    // public static final String VALUE_REQUEST_CLX = "CLX";

    public static final String VALUE_REQUEST_CKE = "CKE";

    // public static final String VALUE_REQUEST_CHP = "CHP";

    // public static final String VALUE_REQUEST_DEX = "DEX";

    // public static final String VALUE_REQUEST_DSP = "DSP";

    // public static final String VALUE_REQUEST_EBX = "EBX";

    public static final String VALUE_REQUEST_ENB = "ENB";

    // public static final String VALUE_REQUEST_GCD = "GCD";

    public static final String VALUE_REQUEST_GDU = "GDU";

    // public static final String VALUE_REQUEST_GKY = "GKY";

    public static final String VALUE_REQUEST_GPN = "GPN";

    // public static final String VALUE_REQUEST_GTK = "GTK";

    public static final String VALUE_REQUEST_MNU = "MNU";

    // public static final String VALUE_REQUEST_RMC = "RMC";

    // public static final String VALUE_REQUEST_MLI = "MLI";

    // public static final String VALUE_REQUEST_MLR = "MLR";

    // public static final String VALUE_REQUEST_MLE = "MLE";

    // public static final String VALUE_REQUEST_LMF = "LMF";

    // public static final String VALUE_REQUEST_DMF = "DMF";

    // public static final String VALUE_REQUEST_DSI = "DSI";

    public static final String VALUE_REQUEST_GTS = "GTS";

    public static final String VALUE_REQUEST_TLI = "TLI";

    public static final String VALUE_REQUEST_TLR = "TLR";

    public static final String VALUE_REQUEST_TLE = "TLE";

    public static final String VALUE_REQUEST_GCR = "GCR";

    public static final String VALUE_REQUEST_CNG = "CNG";

    public static final String VALUE_REQUEST_GOC = "GOC";

    public static final String VALUE_REQUEST_FNC = "FNC";

    // public static final String VALUE_REQUEST_GCX = "GCX";

    // public static final String VALUE_REQUEST_GED = "GED";

    // public static final String VALUE_REQUEST_GOX = "GOX";

    // public static final String VALUE_REQUEST_FCX = "FCX";

    // public static final String VALUE_REQUEST_GEN = "GEN";

    /**
     *
     */
    public static class Callback {
        public Kernel kernel;

        public Status status;

        public Callback(Kernel kernel, Status status) {
            this.kernel = kernel;
            this.status = status;
        }

        /**
         *
         */
        public static interface Kernel {
            /* TODO */
        }

        /**
         *
         */
        public static interface Status {
            /**
             * Status callback.<br>
             * As the name states, its called upon a processing failure.<br>
             * {@code output} will return the {@link ABECS#KEY_STATUS} key. The
             * {@link ABECS#KEY_EXCEPTION} key may also be present, providing further details on
             * the failure.
             *
             * @param output {@link Bundle}
             */
            void onFailure(Bundle output);

            /**
             * Status callback.<br>
             * As the name states, its called when the processing of a request finishes
             * successfully.<br>
             * {@code output} will return the {@link ABECS#KEY_STATUS} key. Other keys may be
             * present, conditionally to the request that was just processed.<br>
             * See the specification v2.12 from ABECS for further details.
             *
             * @param output {@link Bundle}
             */
            void onSuccess(Bundle output);
        }
    }

    /**
     * Parses and processes a {@link Bundle} {@code input}.<br>
     * <br>
     * Mandatory key(s):<br>
     * <ul>
     *     <li>{@code request}</li>
     * </ul>
     * Optional key(s):<br>
     * <ul>
     *     <li>{@code synchronous_operation} - NOT ALLOWED IN THE MAIN THREAD</li>
     * </ul>
     *
     * Other conditional and optional keys: every request may have its own mandatory, conditional
     * and/or optional keys.<br>
     * See the specification v2.12 from ABECS for further details.
     *
     * @param input {@link Bundle}
     * @return {@link Bundle}
     */
    public static Bundle run(@NotNull Bundle input) {
        sOperationSemaphoreList[0].acquireUninterruptibly();

        Bundle output = null;

        if (sContext != null) {
            if (input.getBoolean(KEY_SYNCHRONOUS_OPERATION)) {
                output = queryService(input);
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        queryService(input);
                    }
                }.start();
            }
        } else {
            Log.e(TAG_LOGCAT, "Unable to identify the caller. Call ABECS#register(Context)");
        }

        sOperationSemaphoreList[0].release();

        return output;
    }

    public static void register(@NotNull Context context) {
        for (Semaphore semaphore : sOperationSemaphoreList) {
            semaphore.acquireUninterruptibly();
        }

        sCallback = null;

        try {
            unbindService();
        } catch (Exception exception) {
            Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
        }

        sContext = context;

        for (Semaphore semaphore : sOperationSemaphoreList) {
            semaphore.release();
        }
    }

    public static void register(@NotNull Context context, Callback callback) {
        for (Semaphore semaphore : sOperationSemaphoreList) {
            semaphore.acquireUninterruptibly();
        }

        sCallback = callback;

        try {
            unbindService();
        } catch (Exception exception) {
            Log.d(TAG_LOGCAT, exception.getMessage() + "\r\n" + Log.getStackTraceString(exception));
        }

        sContext = context;

        for (Semaphore semaphore : sOperationSemaphoreList) {
            semaphore.release();
        }
    }
}
