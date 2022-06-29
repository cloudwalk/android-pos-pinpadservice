package io.cloudwalk.pos.pinpadlibrary;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Iterator;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;
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
            JSONObject json = new JSONObject(string);

            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String entry = it.next();

                if (entry.endsWith(".dat")) {
                    bundle.putByteArray(entry, ByteUtility.fromHexString(json.getString(entry)));
                } else {
                    bundle.putString   (entry, json.getString(entry));
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
