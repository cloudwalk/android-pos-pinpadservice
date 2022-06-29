package io.cloudwalk.pos.pinpadlibrary;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Iterator;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

public class PinpadService {
    private static final String
            TAG = IPinpadService.class.getSimpleName().substring(1);

    public static final String
            SERVICE_ACTION = "io.cloudwalk.pos.pinpadservice.PinpadService";

    public static final String
            SERVICE_PACKAGE = "io.cloudwalk.pos.pinpadservice";

    /**
     * Binds the service.<br>
     * Ensures the binding will be undone in the event of a service disconnection.<br>
     *
     * @param string JSON string for identification, operation mode selection and key mapping
     *               dynamic definition.
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
                    Log.e(TAG, "register::entry type unsupported [" + entry + "]");
                }
            }
        } catch (Exception exception) {
            bundle = null;
        }

        ServiceUtility.register(SERVICE_PACKAGE, SERVICE_ACTION, bundle, callback);
    }

    /**
     * Unbinds the service.
     */
    public static void unregister() {
        Log.d(TAG, "unregister");

        ServiceUtility.unregister(SERVICE_PACKAGE, SERVICE_ACTION);
    }
}
