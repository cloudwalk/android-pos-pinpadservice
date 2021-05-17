package com.example.poc2104301453.library;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.poc2104301453.service.IABECS;
import com.example.poc2104301453.service.IServiceCallback;

import java.util.concurrent.Semaphore;

import static com.example.poc2104301453.library.ABECS.RSP_STAT.*;

public class ABECS {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final String CLASS_POC_2104301453_PINPAD_SERVICE = "com.example.poc2104301453.service.PinpadService";

    private static final String PACKAGE_POC_2104301453 = "com.example.poc2104301453.service";

    private static Callback sCallback = null;

    private static IBinder sService = null;

    private static final Semaphore[] sSemaphoreList = {
            new Semaphore(1, true),
            new Semaphore(1, true)
    };

    private static ServiceConnection sServiceConnection = null;

    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    /**
     *
     * @return
     */
    private static IBinder getService() {
        IBinder service;

        sSemaphoreList[1].acquireUninterruptibly();

        service = sService;

        sSemaphoreList[1].release();

        return service;
    }

    /**
     * Translates an instance of {@link Callback} to an instance of {@link IServiceCallback.Stub}.
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
     */
    private static void bindService() {
        Intent intent = new Intent();

        intent.setClassName(PACKAGE_POC_2104301453, CLASS_POC_2104301453_PINPAD_SERVICE);

        sServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                setService(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG_LOGCAT, "onServiceDisconnected::name [" + name + "]");
            }
        };

        boolean serviceBind = sContext.bindService(intent, sServiceConnection, Context.BIND_AUTO_CREATE);

        if (!serviceBind) {
            Log.e(TAG_LOGCAT, "Unable to bind " + CLASS_POC_2104301453_PINPAD_SERVICE);
        }
    }

    /**
     *
     * @param service
     */
    private static void setService(IBinder service) {
        sSemaphoreList[1].acquireUninterruptibly();

        sService = service;

        sSemaphoreList[1].release();
    }

    /**
     *
     */
    private static void unbindService() {
        if (sServiceConnection != null) {
            sContext.unbindService(sServiceConnection);
        }

        setService(null);

        sServiceConnection = null;
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
     *
     */
    public static enum RSP_STAT {
        ST_OK(0),               ST_NOSEC(3),
        ST_F1(4),               ST_F2(5),
        ST_F3(6),               ST_F4(7),
        ST_BACKSP(8),           ST_ERRPKTSEC(9),
        ST_INVCALL(10),         ST_INVPARM(11),
        ST_TIMEOUT(12),         ST_CANCEL(13),
        ST_MANDAT(19),          ST_TABVERDIF(20),
        ST_TABERR(21),          ST_INTERR(40),
        ST_MCDATAERR(41),       ST_ERRKEY(42),
        ST_NOCARD(43),          ST_PINBUSY(44),
        ST_RSPOVRFL(45),        ST_ERRCRYPT(46),
        ST_NOSAM(51),           ST_DUMBCARD(60),
        ST_ERRCARD(61),         ST_CARDINVALIDAT(67),
        ST_CARDPROBLEMS(68),    ST_CARDINVDATA(69),
        ST_CARDAPPNAV(70),      ST_CARDAPPNAUT(71),
        ST_ERRFALLBACK(76),     ST_INVAMOUNT(77),
        ST_ERRMAXAID(78),       ST_CARDBLOCKED(79),
        ST_CTLSMULTIPLE(80),    ST_CTLSCOMMERR(81),
        ST_CTLSINVALIDAT(82),   ST_CTLSPROBLEMS(83),
        ST_CTLSAPPNAV(84),      ST_CTLSAPPNAUT(85),
        ST_CTLSEXTCVM(86),      ST_CTLSIFCHG(87),
        ST_MFNFOUND(100),       ST_MFERRFMT(101),
        ST_MFERR(102);

        private int mValue;

        /**
         * Constructor.
         */
        RSP_STAT(int value) {
            setValue(value);
        }

        /**
         * @return {@link int}
         */
        public int getValue() {
            return mValue;
        }

        /**
         * @param value {@link int}
         */
        public void setValue(int value) {
            mValue = value;
        }
    }

    /**
     *
     * @param input
     * @return
     */
    public static Bundle run(Bundle input) {
        final Bundle[] output = { null };
        final Semaphore[] semaphore = { new Semaphore(0, true) };
        final boolean[] sync = { false };

        sync[0] = input.getBoolean(KEY_SYNCHRONOUS_OPERATION);

        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphoreList[0].acquireUninterruptibly();

                Callback callback = sCallback;
                IBinder service;

                long timestamp = SystemClock.elapsedRealtime();

                do {
                    service = getService();

                    if (SystemClock.elapsedRealtime() > (timestamp + 2000)) {
                        break;
                    }
                } while (service == null);

                String caller = (sContext != null) ? sContext.getPackageName() : null;

                sSemaphoreList[0].release();

                try {
                    if (service == null) {
                        throw new Exception("Unable to get IBinder.");
                    }

                    if (caller == null) {
                        throw new Exception("Unable to get caller.");
                    }

                    output[0] = IABECS.Stub.asInterface(service).run(caller, getServiceCallback(callback), input);

                    if (output[0] != null) {
                        output[0].get(null);
                    }
                } catch (Exception exception) {
                    output[0] = new Bundle();

                    output[0].putInt(KEY_STATUS, ST_INTERR.getValue());

                    output[0].putSerializable(KEY_EXCEPTION, exception);

                    if (!sync[0]) {
                        if (callback != null) {
                            if (callback.status != null) {
                                callback.status.onFailure(output[0]);
                            }
                        }
                    }
                }

                if (sync[0]) {
                    semaphore[0].release();
                }
            }
        }.start();

        if (sync[0]) {
            semaphore[0].acquireUninterruptibly();
        }

        return output[0];
    }

    /**
     *
     * @param context
     */
    public static void register(Context context) {
        Log.d(TAG_LOGCAT, "register::context [" + context + "]");

        register(context, null);
    }

    /**
     *
     * @param context
     * @param callback
     */
    public static void register(Context context, Callback callback) {
        Log.d(TAG_LOGCAT, "register::context [" + context + "], callback [" + callback + "]");

        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphoreList[0].acquireUninterruptibly();

                sCallback = callback;

                unbindService();

                sContext = context;

                if (sContext != null) {
                    bindService();
                }

                sSemaphoreList[0].release();
            }
        }.start();
    }

    /**
     *
     */
    public static void unregister() {
        Log.d(TAG_LOGCAT, "unregister");

        register(null);
    }
}
