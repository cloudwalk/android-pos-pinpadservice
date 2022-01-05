package io.cloudwalk.pos.pinpadservice.utilities;

import android.os.Bundle;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.cloudwalk.loglibrary.Log;

public class VendorUtility {
    private static final String
            TAG = VendorUtility.class.getSimpleName();

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    private VendorUtility() {
        Log.d(TAG, "VendorUtility");
    }

    public static void abort() {
        Log.d(TAG, "abort");

        // TODO: platform specific code
    }

    public static void request(Bundle bundle) {
        Log.d(TAG, "request");

        // TODO: platform specific code
    }
}
