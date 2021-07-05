package io.cloudwalk.pos.pinpadservice;

import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;

public class PinpadAbstractionLayer extends IPinpadService.Stub {
    private static final String TAG_LOGCAT = PinpadAbstractionLayer.class.getSimpleName();

    private static final PinpadAbstractionLayer
            sPinpadAbstractionLayer = new PinpadAbstractionLayer();

    /**
     * Constructor.
     */
    private PinpadAbstractionLayer() {
        Log.d(TAG_LOGCAT, "PinpadAbstractionLayer");

        /* Nothing to do */
    }

    /**
     * @return {@link PinpadAbstractionLayer}
     */
    public static PinpadAbstractionLayer getInstance() {
        Log.d(TAG_LOGCAT, "getInstance");

        return sPinpadAbstractionLayer;
    }

    /**
     * @return {@link IPinpadManager}
     */
    @Override
    public IPinpadManager getPinpadManager() {
        Log.d(TAG_LOGCAT, "getPinpadManager");

        return PinpadManager.getInstance();
    }
}
