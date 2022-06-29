package io.cloudwalk.pos.pinpadlibrary.managers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.Application;
import io.cloudwalk.utilitieslibrary.utilities.BundleUtility;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

public class PinpadManager {
    private static final String
            TAG = IPinpadService.class.getSimpleName().substring(1);

    private static final String
            PINPAD_SERVICE_ACTION  = "io.cloudwalk.pos.pinpadservice.PinpadService";

    private static final String
            PINPAD_SERVICE_PACKAGE = "io.cloudwalk.pos.pinpadservice";

    private static final Semaphore
            sAbortSemaphore   = new Semaphore(1, true);

    private static final Semaphore
            sReceiveSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sRequestSemaphore = new Semaphore(1, true);

    public static interface Callback {
        public int onServiceCallback(String string);
    }

    private static String _request(String string, Callback callback) {
        Log.d(TAG, "_request");

        IServiceCallback channel = new IServiceCallback.Stub() {
            @Override
            public int onServiceCallback(Bundle bundle) {
                JSONObject buffer = BundleUtility.getJSONObject(bundle);

                return callback.onServiceCallback(buffer.toString());
            }
        };

        return _request(string, (callback != null) ? channel : null);
    }

    private static String _request(String string, IServiceCallback callback) {
        Log.d(TAG, "_request");

        long timestamp  = SystemClock.elapsedRealtime();

        byte[][] stream = { null, null };

        try {
            sRequestSemaphore.acquireUninterruptibly();

            stream[0] = PinpadUtility.buildRequestDataPacket(string);
            stream[1] = new byte[2048];

            int status;

            sAbortSemaphore.acquireUninterruptibly();

            try {
                status = send(stream[0], stream[0].length, callback);

                if (status  < 0) {
                    throw new RuntimeException("_request::status [" + status + "]");
                }

                status = recv(stream[1], 2000);

                if (status  < 0) {
                    throw new RuntimeException("_request::status [" + status + "]");
                }

                if (status == 0) {
                    throw new TimeoutException();
                }
            } finally {
                sAbortSemaphore.release();
            }

            switch (stream[1][0]) { // 2022-02-24: <<ACK>> or <<EOT>>
                case 0x06:
                    /* Nothing to do */
                    break;

                case 0x04:
                    throw new InterruptedException();

                default:
                    String message = String.format("_request::stream[1][0] [%02X]", stream[1][0]);

                    throw new RuntimeException(message);
            }

            do {
                ByteUtility.clear(stream[1]);

                if (!sAbortSemaphore.tryAcquire(0, SECONDS)) {
                    throw new InterruptedException();
                }

                try {
                    status = recv(stream[1], 0);

                    if (status  < 0) {
                        throw new RuntimeException("_request::status [" + status + "]");
                    }
                } finally {
                    sAbortSemaphore.release();
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
            ByteUtility.clear(stream[0]);
            ByteUtility.clear(stream[1]);

            sRequestSemaphore.release();

            Log.d(TAG, "_request::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }
    }

    private static void _abort() {
        long timestamp = SystemClock.elapsedRealtime();

        byte[][] stream = {
                new byte[] { 0x18 },
                new byte[2048]
        };

        try {
            sAbortSemaphore.acquireUninterruptibly();

            int status = send(stream[0], 1, (Callback) null);

            if (status < 0) {
                throw new RuntimeException("_abort::status [" + status + "]");
            }

            long[] timeout = { 2000, timestamp + 2000 };

            while (stream[1][0] != 0x04) {
                ByteUtility.clear(stream[1]);

                status = recv(stream[1], timeout[0]);

                if (status < 0) {
                    throw new RuntimeException("_abort::status [" + status + "]");
                }

                if (status == 0) {
                    throw new TimeoutException();
                }

                timeout[0] = timeout[1] - SystemClock.elapsedRealtime();
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            ByteUtility.clear(stream[0]);
            ByteUtility.clear(stream[1]);

            sAbortSemaphore.release();

            Log.d(TAG, "_abort::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
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
     * @deprecated As of release 1.2.0, replaced by {@link PinpadManager#request(String, Callback)}.
     */
    @Deprecated
    public static Bundle request(@NotNull Bundle bundle, IServiceCallback callback) {
        Log.d(TAG, "request");

        Bundle[]  response  = {
                new Bundle()
        };

        Semaphore semaphore = new Semaphore(0, true);

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    JSONObject[] buffer = { new JSONObject(), null };

                    for (String entry : bundle.keySet()) {
                        buffer[0].put(entry, bundle.get(entry));
                    }

                    String string = _request(buffer[0].toString(), callback);

                    buffer[1] = new JSONObject(string);

                    for (Iterator<String> it = buffer[1].keys(); it.hasNext(); ) {
                        String entry = it.next();

                        if (ABECS.RSP_STAT.equals(entry)) {
                            ABECS.STAT RSP_STAT = ABECS.STAT.valueOf(buffer[1].getString(ABECS.RSP_STAT));

                            response[0].putSerializable(ABECS.RSP_STAT, RSP_STAT);
                        } else {
                            response[0].putString(entry, buffer[1].getString(entry));
                        }
                    }

                    semaphore.release();
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                }
            }
        }.start();

        semaphore.acquireUninterruptibly();

        return response[0];
    }

    /**
     * Performs a request using JSON string as input, mirroring the ABECS PINPAD protocol.
     *
     * @param string JSON {@link String}
     * @param callback {@link IServiceCallback}
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

                response[0] = _request(string, callback);

                semaphore.release();
            }
        }.start();

        semaphore.acquireUninterruptibly();

        return response[0];
    }

    /**
     * Interrupts blocking and/or queued requests.
     */
    public static void abort() {
        Log.d(TAG, "abort");

        Semaphore semaphore = new Semaphore(0, true);

        new Thread() {
            @Override
            public void run() { super.run(); _abort(); semaphore.release(); }
        }.start();

        semaphore.acquireUninterruptibly();
    }

    /**
     * @deprecated As of release 1.2.0, usage is discouraged.
     */
    @Deprecated
    public static void execute(@NotNull Runnable runnable) {
        Log.d(TAG, "execute");

        ServiceUtility.execute(runnable);
    }

    /**
     * Queries the queue of processing results.<br>
     * See {@link PinpadManager#send(byte[], int, IServiceCallback)}.
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

                IBinder binder = ServiceUtility.retrieve(PINPAD_SERVICE_PACKAGE, PINPAD_SERVICE_ACTION);

                status = IPinpadService.Stub.asInterface(binder)
                        .getPinpadManager(null).recv(bundle);

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
     * Doesn't wait for the processing result.
     *
     * @param array {@code byte[]} as specified by ABECS PINPAD protocol
     * @param length {@code input} length
     * @param callback {@link IServiceCallback}
     * @return {@code int} bigger than zero if the request was sent successfully, less than zero
     *         otherwise
     */
    public static int send(byte[] array, int length, Callback callback) {
        // Log.d(TAG, "send");

        IServiceCallback channel = new IServiceCallback.Stub() {
            @Override
            public int onServiceCallback(Bundle bundle) {
                JSONObject buffer = BundleUtility.getJSONObject(bundle);

                return callback.onServiceCallback(buffer.toString());
            }
        };

        return send(array, length, (callback != null) ? channel : null);
    }

    /**
     * @deprecated As of release 1.2.0, replaced by {@link PinpadManager#send(byte[], int, Callback)}.
     */
    @Deprecated
    public static int send(byte[] array, int length, IServiceCallback callback) {
        // Log.d(TAG, "send");

        byte[] stream = null;
        int    status;

        try {
            Log.h(TAG, array, length);

            stream = new byte[length];

            System.arraycopy(array, 0, stream, 0, stream.length);

            Bundle bundle = new Bundle();

            bundle.putString   ("application_id", Application.getContext().getPackageName());
            bundle.putByteArray("request", stream);

            IBinder binder = ServiceUtility.retrieve(PINPAD_SERVICE_PACKAGE, PINPAD_SERVICE_ACTION);

            status = IPinpadService.Stub.asInterface(binder)
                    .getPinpadManager(null).send(bundle, callback);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            status = -1;
        } finally {
            ByteUtility.clear(stream);
        }

        return status;
    }

    /**
     * @deprecated As of release 1.2.0, replaced by {@link PinpadManager#register(String, ServiceUtility.Callback)};
     */
    @Deprecated
    public static void register(@NotNull ServiceUtility.Callback callback) {
        Log.d(TAG, "register");

        ServiceUtility.register(PINPAD_SERVICE_PACKAGE, PINPAD_SERVICE_ACTION, null, callback);
    }

    /**
     * @deprecated As of release 1.2.0, replaced by {@link PinpadManager#register(String, ServiceUtility.Callback)};
     */
    @Deprecated
    public static void register(Bundle bundle, @NotNull ServiceUtility.Callback callback) {
        Log.d(TAG, "register");

        ServiceUtility.register(PINPAD_SERVICE_PACKAGE, PINPAD_SERVICE_ACTION, bundle, callback);
    }

    /**
     * Binds the service.<br>
     * Ensures the binding will be undone in the event of a service disconnection.<br>
     *
     * @param string channel for identification, operation mode selection and key mapping dynamic
     *               definition.
     * @param callback {@link ServiceUtility.Callback}
     */
    public static void register(String string, @NotNull ServiceUtility.Callback callback) {
        Log.d(TAG, "register");

        Bundle bundle = new Bundle();

        try {
            JSONObject buffer = new JSONObject(string);

            for (Iterator<String> it = buffer.keys(); it.hasNext(); ) {
                String entry = it.next();

                Object value = buffer.get(entry);

                if        (value instanceof String) {
                    bundle.putString   (entry, (String) value);
                } else if (value instanceof byte[]) {
                    bundle.putByteArray(entry, (byte[]) value);
                } else {
                    Log.d(TAG, "register::entry type unsupported [" + entry + "]");
                }
            }
        } catch (Exception exception) {
            bundle = null;
        }

        ServiceUtility.register(PINPAD_SERVICE_PACKAGE, PINPAD_SERVICE_ACTION, bundle, callback);
    }

    /**
     * Unbinds the service.
     */
    public static void unregister() {
        Log.d(TAG, "unregister");

        ServiceUtility.unregister(PINPAD_SERVICE_PACKAGE, PINPAD_SERVICE_ACTION);
    }
}
