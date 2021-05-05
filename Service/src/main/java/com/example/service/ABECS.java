package com.example.service;

import android.os.Bundle;
import android.util.Log;

import com.example.service.utilities.ServiceUtility;

/**
 *
 */
public class ABECS extends IABECS.Stub {
    private static final String TAG_LOGCAT = ABECS.class.getSimpleName();

    private static final ABECS sABECS = new ABECS();

    /**
     * Constructor.
     */
    private ABECS() {
        Log.d(TAG_LOGCAT, "ABECS");
    }

    /**
     * @return {@link ABECS}
     */
    public static ABECS getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sABECS;
    }

    /**
     *
     * @param input
     * @return
     */
    @Override
    public Bundle run(Bundle input) {
        Log.d(TAG_LOGCAT, "run::input [" + ((input != null) ? input.toString() : null) + "]");

        return ServiceUtility.getInstance().run(input);
    }
}
