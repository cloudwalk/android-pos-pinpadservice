package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.exceptions.ServiceInstanceException;
import io.cloudwalk.pos.pinpadlibrary.utilities.ServiceUtility;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class PinpadManager {
    private static final String TAG_LOGCAT = PinpadManager.class.getSimpleName();

    private static final
            String ACTION_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice.PinpadService";

    private static final
            String PACKAGE_PINPAD_SERVICE = "io.cloudwalk.pos.pinpadservice";

    private static final
            Semaphore sSemaphore = new Semaphore(1, true);

    private static
            IServiceCallback sCallback = null;

    /**
     * Constructor.
     */
    private PinpadManager() {
        /* Nothing to do */
    }

    public static Bundle request(@NotNull Bundle bundle)
            throws Exception {
        IPinpadService service = null;

        try {
            register();

            service = IPinpadService.Stub.asInterface(retrieve());

            sSemaphore.acquireUninterruptibly();

            IServiceCallback callback = (sCallback != null) ? sCallback : new IServiceCallback.Stub() {
                @Override
                public int onSelectionRequired(Bundle output) {
                    Log.d(TAG_LOGCAT, "onSelectionRequired");

                    output.get(null);

                    Log.d(TAG_LOGCAT, "onSelectionRequired::output [" + output.toString() + "]");

                    return 0;
                }

                @Override
                public void onNotificationThrow(Bundle output, int type) {
                    Log.d(TAG_LOGCAT, "onNotificationThrow");

                    output.get(null);

                    Log.d(TAG_LOGCAT, "onNotificationThrow::output [" + output.toString() + "] [" + type + "]");
                }
            };

            sSemaphore.release();

            service.getPinpadManager().registerCallback(callback);

            return service.getPinpadManager().request(bundle);
        } catch (Exception exception) {
            throw new ServiceInstanceException();
        }
    }

    /**
     * It can intentionally take up to 2750 milliseconds of processing time waiting for a valid
     * reference to the Pinpad Service.
     * It's mostly an internal helper method and it shouldn't required direct handling.
     *
     * @return {@link IBinder}
     */
    public static IBinder retrieve() {
        return ServiceUtility.retrieve(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }

    /**
     * Requests the interruption of the current request.
     */
    public static void abort() {
        try {
            Bundle input = new Bundle();

            input.putString(ABECS.CMD_ID, "CAN");

            request(input);
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
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
        ServiceUtility.execute(runnable);
    }

    /**
     * Binds the Pinpad Service.<br>
     * Ensures the binding will be undone in the event of a service disconnection.
     */
    public static void register() {
        ServiceUtility.register(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }

    /**
     * See {@link PinpadManager#register()}.
     *
     * @param callback {@link IServiceCallback}
     */
    public static void register(IServiceCallback callback) {
        register();

        sSemaphore.acquireUninterruptibly();

        sCallback = callback;

        sSemaphore.release();
    }

    /**
     * Unbinds the Pinpad Service.
     */
    public static void unregister() {
        ServiceUtility.unregister(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }
}
