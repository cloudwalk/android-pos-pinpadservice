package io.cloudwalk.pos.pinpadlibrary.managers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static io.cloudwalk.pos.pinpadlibrary.PinpadService.SERVICE_ACTION;
import static io.cloudwalk.pos.pinpadlibrary.PinpadService.SERVICE_PACKAGE;

import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.PinpadService;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.Application;
import io.cloudwalk.utilitieslibrary.utilities.BundleUtility;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

public class PinpadManager {
    private static final String
            TAG = PinpadService.class.getSimpleName();

    private static final Semaphore
            sInterruptSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sReceiveSemaphore   = new Semaphore(1, true);

    private static final Semaphore
            sRequestSemaphore   = new Semaphore(1, true);

    public static interface Callback {
            String NTF_MSG                  = "NTF_MSG";
            String NTF_OPTLST               = "NTF_OPTLST";
            String NTF_PIN                  = "NTF_PIN";
            String NTF_TIMEOUT              = "NTF_TIMEOUT";
            String NTF_TITLE                = "NTF_TITLE";
            String NTF_TYPE                 = "NTF_TYPE";

        public static enum Type {
            NOTIFICATION,
            NTF_2x16,                       NTF_PROCESSING,
            NTF_INSERT_SWIPE_CARD,          NTF_TAP_INSERT_SWIPE_CARD,
            NTF_SELECT,                     NTF_SELECTED,
            NTF_AID_INVALID,                NTF_PIN_START,
            NTF_PIN_ENTRY,                  NTF_PIN_FINISH,
            NTF_PIN_INVALID,                NTF_PIN_LAST_TRY,
            NTF_PIN_BLOCKED,                NTF_PIN_VERIFIED,
            NTF_CARD_BLOCKED,               NTF_REMOVE_CARD,
            NTF_UPDATING,                   NTF_SECOND_TAP;

            private Type() {
                /* Nothing to do */
            }
        }

        public int onServiceCallback(String string);
    }

    private static String _request(String string, Callback callback) {
        // Log.d(TAG, "_request");

        byte[][] stream = { null, null };

        try {
            sRequestSemaphore.acquireUninterruptibly();

            stream[0] = PinpadUtility.buildRequestDataPacket(string);
            stream[1] = new byte[2048];

            int status;

            sInterruptSemaphore.acquireUninterruptibly();

            try {
                status = send(stream[0], stream[0].length, callback);

                if (status  < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                status = recv(stream[1], 2000);

                if (status  < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                if (status == 0) {
                    throw new TimeoutException();
                }
            } finally {
                sInterruptSemaphore.release();
            }

            switch (stream[1][0]) { // 2022-02-24: only <<ACK>> or <<EOT>>
                case 0x06:
                    /* Nothing to do */
                    break;

                case 0x04:
                    throw new InterruptedException();

                default:
                    String message = String.format("request::stream[1][0] [%02X]", stream[1][0]);

                    throw new RuntimeException(message);
            }

            do {
                ByteUtility.clear(stream[1]);

                if (!sInterruptSemaphore.tryAcquire(0, SECONDS)) {
                    throw new InterruptedException();
                }

                try {
                    status = recv(stream[1], 0);

                    if (status  < 0) {
                        throw new RuntimeException("request::status [" + status + "]");
                    }
                } finally {
                    sInterruptSemaphore.release();
                }
            } while (status <= 1);

            String response = PinpadUtility.parseResponseDataPacket(stream[1], status);

            return response;
        } catch (Exception exception) {
            ABECS.STAT RSP_STAT;

            if (exception instanceof InterruptedException) {
                RSP_STAT = ABECS.STAT.ST_CANCEL;
            } else {
                RSP_STAT = ABECS.STAT.ST_INTERR;
            }

            String RSP_ID = "UNKNOWN";

            try {
                RSP_ID = (new JSONObject(string)).getString(ABECS.CMD_ID);
            } catch (JSONException ignored) { }

            return "{\"" + ABECS.RSP_ID + "\":\"" + RSP_ID + "\",\"" + ABECS.RSP_STAT + "\":\"" + RSP_STAT.name() + "\"}";
        } finally {
            ByteUtility.clear(stream);

            sRequestSemaphore.release();
        }
    }

    private static int _interrupt() {
        // Log.d(TAG, "_interrupt");

        byte[][] stream = {
                new byte[] { 0x18 },
                new byte[2048]
        };

        try {
            long timestamp = SystemClock.elapsedRealtime();

            sInterruptSemaphore.acquireUninterruptibly();

            int status = send(stream[0], 1, (Callback) null);

            if (status < 0) {
                throw new RuntimeException("interrupt::status [" + status + "]");
            }

            long[] timeout = { 2000, timestamp + 2000 };

            while (stream[1][0] != 0x04) {
                ByteUtility.clear(stream[1]);

                status = recv(stream[1], timeout[0]);

                if (status < 0) {
                    throw new RuntimeException("interrupt::status [" + status + "]");
                }

                if (status == 0) {
                    throw new TimeoutException();
                }

                timeout[0] = timeout[1] - SystemClock.elapsedRealtime();
            }

            return 1;
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            return (exception instanceof TimeoutException) ? 0 : -1;
        } finally {
            ByteUtility.clear(stream);

            sInterruptSemaphore.release();
        }
    }

    /**
     * Constructor.
     */
    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        /* Nothing to do */
    }

    /**
     * Performs a request using JSON as argument, mirroring the default public data format.
     *
     * @param string JSON {@link String}
     * @param callback {@link Callback}
     * @return JSON {@link String}
     */
    public static String request(@NotNull String string, Callback callback) {
        Log.d(TAG, "request");

        String[]  response  = { null };
        Semaphore semaphore = new Semaphore(0, true);

        new Thread() {
            @Override
            public void run() {
                super.run();

                long timestamp  = SystemClock.elapsedRealtime();

                response[0] = _request(string, callback);

                Log.d(TAG, "request::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");

                semaphore.release();
            }
        }.start();

        semaphore.acquireUninterruptibly();

        return response[0];
    }

    /**
     * Interrupts blocking and/or queued requests.<br>
     * For those which are not newcomers: {@code interrupt} and {@code abort} are equivalent terms.
     */
    public static int interrupt() {
        Log.d(TAG, "interrupt");

        Semaphore semaphore = new Semaphore(0, true);

        int[] status = { -1 };

        new Thread() {
            @Override
            public void run() {
                super.run();

                long timestamp = SystemClock.elapsedRealtime();

                status[0] = _interrupt();

                Log.d(TAG, "interrupt::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "] status[0] [" + status[0] + "]");

                semaphore.release();
            }
        }.start();

        semaphore.acquireUninterruptibly();

        return status[0];
    }

    /**
     * Queries the queue of processing results.<br>
     * See {@link PinpadManager#send(byte[], int, Callback)}.
     *
     * @param array {@code byte[]} as specified by the ABECS PINPAD protocol
     * @param timeout self-describing (milliseconds)
     * @return {@code int} bigger than zero if the request was processed successfully, less than
     *         zero in the event of a failure and zero if timeout was reached
     */
    public static int recv(byte[] array, long timeout) {
        // Log.d(TAG, "recv");

        byte[] stream = null;
        int    status = 0;

        try {
            long timestamp = SystemClock.elapsedRealtime();

            if (!sReceiveSemaphore.tryAcquire(timeout, MILLISECONDS)) {
                return status;
            }

            try {
                Bundle bundle  = new Bundle();

                timeout -= (SystemClock.elapsedRealtime() - timestamp);

                bundle.putLong("timeout", (timeout < 0) ? 0 : timeout);

                IBinder binder = ServiceUtility.retrieve(SERVICE_PACKAGE, SERVICE_ACTION);

                status = IPinpadService.Stub
                        .asInterface(binder).getPinpadManager(null).recv(bundle);

                if (status > 0) {
                    stream = bundle.getByteArray("response");

                    System.arraycopy(stream, 0, array, 0, status);
                }
            } finally {
                ByteUtility.clear(stream);

                sReceiveSemaphore.release();
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            status = -1;
        } finally {
            Log.h(TAG, array, status);
        }

        return status;
    }

    /**
     * Performs a request using the default public data format.<br>
     * Does not wait for processing results.
     *
     * @param array {@code byte[]} as specified by ABECS PINPAD protocol
     * @param length {@code input} length
     * @param callback {@link Callback}
     * @return {@code int} bigger than zero if the request was sent successfully, less than zero
     *         otherwise
     */
    public static int send(byte[] array, int length, Callback callback) {
        // Log.d(TAG, "send");

        byte[] stream = null;
        int    status;

        try {
            Log.h(TAG, array, length);

            IServiceCallback tunnel = new IServiceCallback.Stub() {
                @Override
                public int onServiceCallback(Bundle bundle) {
                    JSONObject json = BundleUtility.getJSONObject(bundle);

                    return callback.onServiceCallback(json.toString());
                }
            };

            Bundle bundle = new Bundle();

            bundle.putString("application_id", Application.getContext().getPackageName());

            stream = new byte[length];

            System.arraycopy(array, 0, stream, 0, stream.length);

            bundle.putByteArray("request", stream);

            IBinder binder = ServiceUtility.retrieve(SERVICE_PACKAGE, SERVICE_ACTION);

            status = IPinpadService.Stub
                    .asInterface(binder).getPinpadManager(null).send(bundle, tunnel);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            status = -1;
        } finally {
            ByteUtility.clear(stream);
        }

        return status;
    }
}
