package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.Application;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PinpadManager {
    private static final String[]
            TAG = {
                    PinpadManager .class.getSimpleName(),
                    IPinpadService.class.getSimpleName().substring(1)
            };

    private static final Semaphore
            sExchangeSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sReceiveSemaphore  = new Semaphore(1, true);

    private static final String
            ACTION_PINPAD_SERVICE  = "io.cloudwalk.pos.pinpadservice.PinpadService";

    private static final String
            PACKAGE_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice";

    /**
     * Constructor.
     */
    private PinpadManager() {
        Log.d(TAG[0], "PinpadManager");

        /* Nothing to do */
    }

    /**
     * Retrieves a valid instance of {@link IBinder}.
     *
     * @return {@link IBinder}
     */
    public static IBinder retrieve() {
        Log.d(TAG[0], "retrieve");

        return ServiceUtility.retrieve(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }

    /**
     * Performs an ABECS PINPAD request using the key/value format, heavily based on the default
     * public data format, as specified by the ABECS PINPAD protocol.
     *
     * @param input {@link Bundle}
     * @return {@link Bundle}
     */
    public static Bundle request(@NotNull Bundle input, IServiceCallback callback) {
        Log.d(TAG[0], "request");

        long overhead  = SystemClock.elapsedRealtime();
        long timestamp = overhead;

        Bundle output;
        String CMD_ID  = input.getString(ABECS.CMD_ID, "UNKNOWN");

        try {
            byte[] request  = PinpadUtility.buildRequestDataPacket(input);
            byte[] response = new byte[2048 + 4];

            int status = -1;

            try {
                sExchangeSemaphore.acquireUninterruptibly();

                timestamp = SystemClock.elapsedRealtime();

                status = send(request, request.length, callback);

                if (status < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                status = receive(response, 2000);

                if (status < 0 && status != -2) {
                    throw new RuntimeException("request::status [" + status + "]");
                }
            } finally {
                sExchangeSemaphore.release();
            }

            switch (response[0]) {
                case 0x06:
                    /* Nothing to do */
                    break;

                case 0x04:
                    if (status != -2) {
                        status =  -2;
                    }

                default:
                    if (status != -2) {
                        String message = String.format("request::response[0] [%02X]", response[0]);

                        throw (status != 0) ? new RuntimeException(message) : new TimeoutException();
                    } else {
                        throw new InterruptedException();
                    }
            }

            do {
                response = new byte[2048 + 4];
                status   = receive(response, 10000);

                if (status < 0 && status != -2) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                switch (status) {
                    case -2: /* 2021-11-23: assuming 0x04 puts at risk error translation when bundle and non-bundle
                              * interfaces are mixed */
                    case  1:
                        throw new InterruptedException();

                    default:
                        /* Nothing to do */
                        break;
                }

                // TODO: break loop for non-blocking instructions
            } while (status <= 0);

            if (timestamp >= overhead) {
                timestamp = SystemClock.elapsedRealtime() - timestamp;
            }

            output = PinpadUtility.parseResponseDataPacket(response, status);
        } catch (Exception exception) {
            Log.e(TAG[0], Log.getStackTraceString(exception));

            ABECS.STAT RSP_STAT;

            if (exception instanceof InterruptedException) {
                RSP_STAT = ABECS.STAT.ST_CANCEL;
            } else {
                RSP_STAT = ABECS.STAT.ST_INTERR;
            }

            output = new Bundle();

            output.putSerializable(ABECS.RSP_EXCEPTION, exception);
            output.putSerializable(ABECS.RSP_ID, CMD_ID);
            output.putSerializable(ABECS.RSP_STAT, RSP_STAT);
        } finally {
            if (timestamp >= overhead) {
                timestamp = SystemClock.elapsedRealtime() - timestamp;
            }

            overhead = SystemClock.elapsedRealtime() - overhead;

            Log.d(TAG[0], "request::timestamp " + ((!CMD_ID.equals("UNKNOWN")) ? "(" + CMD_ID + ") " : "") + "[" + overhead + "] [" + (overhead - timestamp) + "]");
        }

        return output;
    }

    /**
     * Performs an ABECS PINPAD abort request, interrupting blocking or sequential commands.
     */
    public static void abort() {
        Log.d(TAG[0], "abort");

        long timestamp = SystemClock.elapsedRealtime();

        try {
            sExchangeSemaphore.acquireUninterruptibly();

            byte[] request = new byte[] { 0x18 };

            int status = send(request, request.length, null);

            if (status < 0) {
                throw new RuntimeException("abort::status [" + status + "]");
            }

            byte[] response = new byte[2048 + 4];

            status = receive(response, 2000);
        } catch (Exception exception) {
            Log.e(TAG[0], Log.getStackTraceString(exception));
        } finally {
            sExchangeSemaphore.release();

            Log.d(TAG[0], "abort::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }
    }

    /**
     * Starts a new thread and calls {@link Runnable#run()} from given {@link Runnable}.<br>
     * Intended as a helper for UI thread calls.<br>
     * <code>
     *     <pre>
     * PinpadManager.execute(new Runnable() {
     *    {@literal @}Override
     *     public void execute() {
     *         // code you shouldn't run on the main thread goes here
     *     }
     * });
     *     </pre>
     * </code>
     *
     * @param runnable {@link Runnable}
     */
    public static void execute(@NotNull Runnable runnable) {
        Log.d(TAG[0], "execute");

        ServiceUtility.execute(runnable);
    }

    /**
     * Receives the processing result of a previously sent ABECS PINPAD request.<br>
     * See {@link PinpadManager#send(byte[], int, IServiceCallback)}.
     *
     * @param response {@code byte[]} as specified by ABECS PINPAD protocol
     * @param timeout self-describing (milliseconds)
     * @return {@code int} bigger than zero if the request was processed successfully, less than
     *         zero in the event of a failure and zero if timeout was reached
     */
    public static int receive(byte[] response, long timeout) {
        Log.d(TAG[0], "receive");

        long timestamp = SystemClock.elapsedRealtime();

        Bundle bundle = new Bundle();
        int    result = -2;

        try {
            if (!sReceiveSemaphore.tryAcquire(0, TimeUnit.SECONDS)) {
                return result;
            }

            try {
                bundle.putLong("timeout", timeout);

                result = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager(null).recv(bundle);

                if (result > 0) {
                    System.arraycopy(bundle.getByteArray("response"), 0, response, 0, result);
                }
            } finally {
                sReceiveSemaphore.release();
            }
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                return receive(response, timeout);
            } else {
                Log.e(TAG[0], Log.getStackTraceString(exception));
            }

            result = -1;
        } finally {
            Log.h(TAG[1], response, result);

            Log.d(TAG[0], "receive::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }

        return result;
    }

    /**
     * Binds the Pinpad Service.<br>
     * Ensures the binding will be undone in the event of a service disconnection.
     */
    public static void register(@NotNull ServiceUtility.Callback callback) {
        Log.d(TAG[0], "register");

        register(null, callback);
    }

    public static void register(Bundle extras, @NotNull ServiceUtility.Callback callback) {
        Log.d(TAG[0], "register");

        ServiceUtility.register(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE, extras, callback);
    }

    /**
     * Performs an ABECS PINPAD request using the default public data format.<br>
     * Doesn't wait for the processing result.
     *
     * @param request {@code byte[]} as specified by ABECS PINPAD protocol
     * @param length {@code input} length
     * @return {@code int} bigger than zero if the request was sent successfully, less than zero
     *         otherwise
     */
    public static int send(byte[] request, int length, IServiceCallback callback) {
        Log.d(TAG[0], "send");

        long timestamp = SystemClock.elapsedRealtime();

        Bundle bundle = new Bundle();
        int    result = 0;

        try {
            Log.h(TAG[1], request, length);

            String applicationId  = Application.getPackageContext().getPackageName();

            byte[] trimmedRequest = new byte[length];

            System.arraycopy(request, 0, trimmedRequest, 0, length);

            bundle.putString   ("application_id", applicationId);
            bundle.putByteArray("request", trimmedRequest);

            result = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager(null).send(bundle, callback);
        } catch (Exception exception) {
            Log.e(TAG[0], Log.getStackTraceString(exception));

            result = -1;
        } finally {
            Log.d(TAG[0], "send::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }

        return result;
    }

    /**
     * Unbinds the Pinpad Service.
     */
    public static void unregister() {
        Log.d(TAG[0], "unregister");

        ServiceUtility.unregister(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }
}
