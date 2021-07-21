package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;

public class PinpadManager {
    private static final String TAG = PinpadManager.class.getSimpleName();

    private static final String ACTION_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice.PinpadService";

    private static final String PACKAGE_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice";

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

    public static byte[] request(@NotNull byte[] input) {
        Log.d(TAG, "request");

        long timestamp = SystemClock.elapsedRealtime();

        byte[] output = new byte[0];

        IPinpadManager pinpad = null;

        try {
            pinpad = IPinpadService.Stub.asInterface(retrieve()).getPinpadManager();

            output = pinpad.request(input);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            output = new byte[] { 0x15 }; /* NAK */
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
