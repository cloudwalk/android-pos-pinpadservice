package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.Application;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

public class PinpadManager {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final Semaphore
            sSemaphore = new Semaphore(1, true);

    private static final String
            ACTION_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice.PinpadService";

    private static final String
            PACKAGE_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice";

    private static final byte
            ACK = 0x06;

    private static final byte
            CAN = 0x18;

    private static final byte
            EOT = 0x04;

    /**
     * Constructor.
     */
    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        /* Nothing to do */
    }

    /**
     * See {@link Semaphore#acquireUninterruptibly()}.
     */
    private static void acquire() {
        Log.d(TAG, "acquire");

        sSemaphore.acquireUninterruptibly();
    }

    /**
     * See {@link PinpadManager#receive(byte[], long)}.
     */
    private static int recv(byte[] output, long timeout) {
        return receive(output, timeout);
    }

    /**
     * Releases a permit if the number of available permits doesn't already surpasses zero.<br>
     * See {@link Semaphore#release()}.
     */
    private static void release() {
        Log.d(TAG, "release");

        if (sSemaphore.availablePermits() <= 0) {
            sSemaphore.release();
        }

        Log.d(TAG, "release::semaphore.availablePermits() [" + sSemaphore.availablePermits() + "]");
    }

    /**
     * Retrieves a valid instance of {@link IBinder}.
     *
     * @return {@link IBinder}
     */
    public static IBinder retrieve() {
        Log.d(TAG, "retrieve");

        return ServiceUtility.retrieve(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }

    /**
     * Performs an ABECS PINPAD request using the key/value format, heavily based on the default
     * public data format, as specified by the ABECS PINPAD protocol.
     *
     * @param input {@link Bundle}
     * @return {@link Bundle}
     */
    public static Bundle request(IServiceCallback callback, @NotNull Bundle input) {
        Log.d(TAG, "request");

        long overhead  = SystemClock.elapsedRealtime();
        long timestamp = overhead;

        Bundle output;
        String CMD_ID  = input.getString(ABECS.CMD_ID, "UNKNOWN");

        try {
            acquire();

            byte[] request  = PinpadUtility.buildRequestDataPacket(input);
            byte[] response = new byte[2048 + 4];

            int retry  = 3;
            int status = 0;

            timestamp = SystemClock.elapsedRealtime();

            do {
                status = send(callback, request, request.length);

                Log.d(TAG, "request::send [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                status = recv(response, 2000);

                Log.d(TAG, "request::recv [" + status + "]");

                if (status != 1) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                switch (response[0]) {
                    case ACK:
                        /* Nothing to do */
                        break;

                    case EOT:
                        throw new InterruptedException();

                    default:
                        if (--retry <= 0) {
                            throw (status != 0) ? new RuntimeException() : new TimeoutException();
                        }
                        break;
                }
            } while (response[0] != ACK);

            release();

            do {
                status = recv(response, 10000);

                Log.d(TAG, "request::recv [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                } else {
                    if (response[0] == EOT) {
                        throw new InterruptedException();
                    }
                }
            } while (status <= 0);

            if (timestamp >= overhead) {
                timestamp = SystemClock.elapsedRealtime() - timestamp;
            }

            output = PinpadUtility.parseResponseDataPacket(response, status);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            output = new Bundle();

            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.ST_INTERR);
            output.putSerializable(ABECS.RSP_EXCEPTION, exception);

            if (timestamp >= overhead) {
                timestamp = SystemClock.elapsedRealtime() - timestamp;
            }
        } finally {
            release();

            overhead = SystemClock.elapsedRealtime() - overhead;

            Log.d(TAG, "request::timestamp " + ((!CMD_ID.equals("UNKNOWN")) ? "(" + CMD_ID + ") " : "") + "[" + overhead + "] [" + (overhead - timestamp) + "]");
        }

        return output;
    }

    /**
     * Performs an ABECS PINPAD abort request, interrupting blocking or sequential commands.
     */
    public static void abort() {
        Log.d(TAG, "abort");

        long timestamp = SystemClock.elapsedRealtime();

        try {
            acquire();

            byte[] request  = new byte[] { CAN };

            int status = send(null, request, request.length);

            Log.d(TAG, "abort::send [" + status + "]");

            if (status < 0) {
                throw new RuntimeException("abort::status [" + status + "]");
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            release();

            Log.d(TAG, "abort::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
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
        Log.d(TAG, "execute");

        ServiceUtility.execute(runnable);
    }

    /**
     * Waits for the processing result of a previously sent ABECS PINPAD request.<br>
     * See {@link PinpadManager#send(IServiceCallback, byte[], int)}.
     *
     * @param output {@code byte[]} as specified by ABECS PINPAD protocol
     * @param timeout self-describing (milliseconds)
     * @return {@code int} bigger than zero if the request was processed successfully, less than
     *         zero in the event of a failure and zero if timeout was reached
     */
    public static int receive(byte[] output, long timeout) {
        Log.d(TAG, "receive");

        long timestamp = SystemClock.elapsedRealtime();

        int result = 0;

        try {
            result = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager().recv(output, timeout);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            result = -1;
        } finally {
            Log.d(TAG, "receive::result [" + result + "]");

            Log.h(TAG, output, result);

            Log.d(TAG, "receive::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }

        return result;
    }

    /**
     * Binds the Pinpad Service.<br>
     * Ensures the binding will be undone in the event of a service disconnection.
     */
    public static void register(@NotNull ServiceUtility.Callback callback) {
        Log.d(TAG, "register");

        ServiceUtility.register(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE, callback);
    }

    /**
     * Performs an ABECS PINPAD request using the default public data format.<br>
     * Does not wait the processing result.
     *
     * @param input {@code byte[]} as specified by ABECS PINPAD protocol
     * @param length {@code input} length
     * @return {@code int} bigger than zero if the request was sent successfully, less than zero
     *         otherwise
     */
    public static int send(IServiceCallback callback, byte[] input, int length) {
        Log.d(TAG, "send");

        long timestamp = SystemClock.elapsedRealtime();

        int result = 0;

        try {
            Log.h(TAG, input, length);

            String application = Application.getPackageContext().getPackageName();

            result = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager().send(application, callback, input, length);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            result = -1;
        } finally {
            Log.d(TAG, "send::result [" + result + "]");

            Log.d(TAG, "send::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }

        return result;
    }

    /**
     * Unbinds the Pinpad Service.
     */
    public static void unregister() {
        Log.d(TAG, "unregister");

        ServiceUtility.unregister(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }
}
