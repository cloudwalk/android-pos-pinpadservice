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
    public static Bundle request(@NotNull Bundle input, IServiceCallback callback) {
        Log.d(TAG, "request");

        long overhead  = SystemClock.elapsedRealtime();
        long timestamp = overhead;

        Bundle output;
        String CMD_ID  = input.getString(ABECS.CMD_ID, "UNKNOWN");

        try {
            acquire();

            byte[] request  = PinpadUtility.buildRequestDataPacket(input);
            byte[] response;

            int retry  = 3;
            int status = 0;

            timestamp = SystemClock.elapsedRealtime();

            do {
                do { response = new byte[2048 + 4]; } while (receive(response, 0) != 0);

                status = send(request, request.length, callback);

                Log.d(TAG, "request::send [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                }

                status = receive(response, 2000);

                Log.d(TAG, "request::receive [" + status + "]");

                if (status != 0 && status != 1) {
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
                response = new byte[2048 + 4];
                status   = receive(response, 10000);

                Log.d(TAG, "request::receive [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException("request::status [" + status + "]");
                } else {
                    if (response[0] == EOT) {
                        throw new InterruptedException();
                    }
                }

                // TODO: break loop for non-blocking instructions
            } while (status <= 0);

            if (timestamp >= overhead) {
                timestamp = SystemClock.elapsedRealtime() - timestamp;
            }

            output = PinpadUtility.parseResponseDataPacket(response, status);
        } catch (InterruptedException exception) {
            Log.d(TAG, Log.getStackTraceString(exception));

            output = new Bundle();

            output.putSerializable(ABECS.RSP_ID, CMD_ID);
            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.ST_CANCEL);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            output = new Bundle();

            output.putSerializable(ABECS.RSP_EXCEPTION, exception);
            output.putSerializable(ABECS.RSP_ID, CMD_ID);
            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.ST_INTERR);
        } finally {
            if (timestamp >= overhead) {
                timestamp = SystemClock.elapsedRealtime() - timestamp;
            }

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

            byte[] request = new byte[] { CAN };

            int status = send(request, request.length, null);

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
     * See {@link PinpadManager#send(byte[], int, IServiceCallback)}.
     *
     * @param response {@code byte[]} as specified by ABECS PINPAD protocol
     * @param timeout self-describing (milliseconds)
     * @return {@code int} bigger than zero if the request was processed successfully, less than
     *         zero in the event of a failure and zero if timeout was reached
     */
    public static int receive(byte[] response, long timeout) {
        Log.d(TAG, "receive");

        long timestamp = SystemClock.elapsedRealtime();

        Bundle bundle = new Bundle();
        int    result = 0;

        try {
            bundle.putLong("timeout", timeout);

            result = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager(null).recv(bundle);

            if (result > 0) {
                System.arraycopy(bundle.getByteArray("response"), 0, response, 0, result);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            result = -1;
        } finally {
            Log.h(IPinpadService.class.getSimpleName().substring(1), response, result);

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

        register(null, callback);
    }

    public static void register(Bundle extras, @NotNull ServiceUtility.Callback callback) {
        Log.d(TAG, "register");

        ServiceUtility.register(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE, extras, callback);
    }

    /**
     * Performs an ABECS PINPAD request using the default public data format.<br>
     * Does not wait the processing result.
     *
     * @param request {@code byte[]} as specified by ABECS PINPAD protocol
     * @param length {@code input} length
     * @return {@code int} bigger than zero if the request was sent successfully, less than zero
     *         otherwise
     */
    public static int send(byte[] request, int length, IServiceCallback callback) {
        Log.d(TAG, "send");

        long timestamp = SystemClock.elapsedRealtime();

        Bundle bundle = new Bundle();
        int    result = 0;

        try {
            Log.h(IPinpadService.class.getSimpleName().substring(1), request, length);

            String packageName = Application.getPackageContext().getPackageName();

            byte[] courrier = new byte[length];

            System.arraycopy(request, 0, courrier, 0, length);

            bundle.putString   ("application_id", packageName);
            bundle.putByteArray("request", courrier);

            result = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager(null).send(bundle, callback);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            result = -1;
        } finally {
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
