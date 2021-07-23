package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.Application;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeoutException;

public class PinpadManager {
    private static final String TAG = PinpadManager.class.getSimpleName();

    private static final String ACTION_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice.PinpadService";

    private static final String PACKAGE_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice";

    private static final byte ACK = 0x06;

    private static final byte NAK = 0x15;

    /**
     * Constructor.
     */
    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        /* Nothing to do */
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
    public static Bundle request(@NotNull Bundle input) {
        Log.d(TAG, "request");

        long timestamp = SystemClock.elapsedRealtime();

        Bundle output = null;

        try {
            byte[] request  = PinpadUtility.build(input);

            IPinpadManager pinpad = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager();

            byte[] response = new byte[2048 + 4];

            int retry  = 3;
            int status = 0;

            do {
                Log.h(TAG, request, request.length);

                String application = Application.getPackageContext().getPackageName();

                status = pinpad.send(application, request, request.length);

                Log.d(TAG, "request::pinpad.send(byte[]) [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException();
                }

                status = pinpad.recv(response, 2000);

                Log.d(TAG, "request::pinpad.recv(byte[], long) [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException();
                }

                if (--retry <= 0 && status == 0) {
                    throw new TimeoutException();
                }

                if (status > 0) {
                    Log.h(TAG, response, status);
                }
            } while (status <= 0);

            do {
                status = pinpad.recv(response, 10000);

                Log.d(TAG, "request::pinpad.recv(byte[], long) [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException();
                }

                if (status > 0) {
                    Log.h(TAG, response, status);
                }
            } while (status <= 0);

            output = PinpadUtility.parse(response);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            output = new Bundle();

            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.ST_INTERR);
            output.putSerializable(ABECS.RSP_EXCEPTION, exception);
        } finally {
            Log.d(TAG, "request::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "]");
        }

        return output;
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
     * Binds the Pinpad Service.<br>
     * Ensures the binding will be undone in the event of a service disconnection.
     */
    public static void register(@NotNull ServiceUtility.Callback callback) {
        Log.d(TAG, "register");

        ServiceUtility.register(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE, callback);
    }

    /**
     * Unbinds the Pinpad Service.
     */
    public static void unregister() {
        Log.d(TAG, "unregister");

        ServiceUtility.unregister(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }
}
