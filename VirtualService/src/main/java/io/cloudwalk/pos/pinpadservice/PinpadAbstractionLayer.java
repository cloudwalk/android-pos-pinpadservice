package io.cloudwalk.pos.pinpadservice;

import android.os.Bundle;

import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;

public class PinpadAbstractionLayer extends IPinpadService.Stub {
    private static final String
            TAG = PinpadAbstractionLayer.class.getSimpleName();

    private static final PinpadAbstractionLayer
            sPinpadAbstractionLayer = new PinpadAbstractionLayer();

    private static final Semaphore
            sSemaphore = new Semaphore(1, true);

    /**
     * Constructor.
     */
    private PinpadAbstractionLayer() {
        Log.d(TAG, "PinpadAbstractionLayer");

        /* Nothing to do */
    }

    /**
     * @return {@link PinpadAbstractionLayer}
     */
    public static PinpadAbstractionLayer getInstance() {
        Log.d(TAG, "getInstance");

        return sPinpadAbstractionLayer;
    }

    public static void setConfig(byte[] keymap, boolean overwrite) {
        Log.d(TAG, "setConfig::overwrite [" + overwrite + "]");

        /* 2021-11-19: no virtual usage */
    }

    /**
     * @return {@link IPinpadManager}
     */
    @Override
    public IPinpadManager getPinpadManager(Bundle bundle) {
        Log.d(TAG, "getPinpadManager");

        try {
            sSemaphore.acquireUninterruptibly();

            String version = (bundle != null) ? bundle.getString("version", "") : "";

            switch (version) {
                // case...
                // case...

                default:
                    return PinpadManager.getInstance();
            }
        } finally {
            sSemaphore.release();
        }
    }
}
