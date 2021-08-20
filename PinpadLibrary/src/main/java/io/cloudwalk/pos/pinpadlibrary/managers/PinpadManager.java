package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.Application;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeoutException;

public class PinpadManager {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

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

    private static final byte
            NAK = 0x15;

    /**
     * Constructor.
     */
    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        /* Nothing to do */
    }

    /**
     *
     * @param output
     * @param timeout
     * @return
     */
    private static int recv(byte[] output, long timeout) {
        return receive(output, timeout);
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
     *
     * @param input
     * @return
     */
    public static Bundle request(IServiceCallback callback, @NotNull Bundle input) {
        Log.d(TAG, "request");

        long overhead  = SystemClock.elapsedRealtime();
        long timestamp = overhead;

        Bundle output = null;

        String CMD_ID = input.getString(ABECS.CMD_ID, "UNKNOWN");

        try {
            byte[] request  = PinpadUtility.buildRequestDataPacket(input);
            byte[] response = new byte[2048 + 4];

            int retry  = 3;
            int status = 0;

            timestamp = SystemClock.elapsedRealtime();

            switch (CMD_ID) {
                // TODO: review all commands that shouldn't be preceded by an abort
                case ABECS.TLR:
                case ABECS.TLE:
                case ABECS.GCX:
                // case ABECS.GOX:
                // case ABECS.FNX:
                    break;

                case "UNKNOWN":
                    Log.e(TAG, "request::ABECS.CMD_ID [" + CMD_ID + "]");
                    /* no break */

                default: abort(); break;
            }

            do {
                status = send(callback, request, request.length);

                if (status < 0) {
                    throw new RuntimeException();
                }

                status = recv(response, 2000);

                if (status < 0) {
                    throw new RuntimeException();
                } else {
                    if (response[0] != ACK) {
                        if (--retry <= 0) {
                            throw (status != 0) ? new RuntimeException() : new TimeoutException();
                        }
                    }
                }
            } while (response[0] != ACK);

            do {
                status = recv(response, 10000);

                if (status < 0) {
                    throw new RuntimeException();
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
            overhead = SystemClock.elapsedRealtime() - overhead;

            Log.d(TAG, "request::timestamp " + ((!CMD_ID.equals("UNKNOWN")) ? "(" + CMD_ID + ") " : "") + "[" + overhead + "] [" + (overhead - timestamp) + "]");
        }

        return output;
    }

    /**
     *
     */
    public static void abort() {
        Log.d(TAG, "abort");

        long timestamp = SystemClock.elapsedRealtime();

        try {
            byte[] request  = new byte[] { CAN };
            byte[] response = new byte[2048 + 4];

            int retry  = 3;
            int status = 0;

            do {
                status = send(null, request, request.length);

                if (status < 0) {
                    throw new RuntimeException();
                }

                status = recv(response, 2000);

                if (status < 0) {
                    throw new RuntimeException();
                } else {
                    if (response[0] != EOT) {
                        if (--retry <= 0) {
                            throw (status != 0) ? new RuntimeException() : new TimeoutException();
                        }
                    }
                }
            } while (response[0] != EOT);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
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
     *
     * @param output
     * @param timeout
     * @return
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
     *
     * @param input
     * @param length
     * @return
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
