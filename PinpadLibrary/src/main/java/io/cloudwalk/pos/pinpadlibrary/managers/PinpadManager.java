package io.cloudwalk.pos.pinpadlibrary.managers;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
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
        return ServiceUtility.retrieve(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }

    public static Bundle request(@NotNull Bundle bundle) {
        IPinpadService service = null;

        try {
            try {
                service = IPinpadService.Stub.asInterface(retrieve());
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }

            // TODO: return parse(service.request(build(bundle)));

            throw new NoSuchMethodException();
        } catch (Exception exception) {
            Bundle output = new Bundle();

            String CMD_ID = bundle.getString(ABECS.CMD_ID);

            if (CMD_ID != null) {
                output.putString(ABECS.RSP_ID, CMD_ID);
            }

            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.ST_INTERR);

            output.putSerializable("exception", exception);

            return output;
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
    public static void register(@NotNull ServiceUtility.Callback callback) {
        ServiceUtility.register(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE, callback);
    }

    /**
     * Unbinds the Pinpad Service.
     */
    public static void unregister() {
        ServiceUtility.unregister(PACKAGE_PINPAD_SERVICE, ACTION_PINPAD_SERVICE);
    }
}
