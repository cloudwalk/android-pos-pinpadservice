package io.cloudwalk.pos.pinpadservice;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IPinpadService;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;
import io.cloudwalk.utilitieslibrary.Application;

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
        // Log.d(TAG, "getInstance");

        return sPinpadAbstractionLayer;
    }

    public static void setConfig(byte[] keymap, boolean overwrite) {
        // Log.d(TAG, "setConfig::overwrite [" + overwrite + "]");

        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphore.acquireUninterruptibly();

                Context context = Application.getPackageContext();
                byte[]  content;

                try {
                    File    file   = new File ("/sdcard/PPComp", "DUKLINK.dat");
                    long    length = file.length();
                    boolean exists = file.length() > 0;

                    if (overwrite || !exists) {
                        context.openFileOutput(file.getName(), MODE_PRIVATE);

                        FileOutputStream writer = new FileOutputStream(file, false);

                        writer.write(keymap, 0, keymap.length);
                        writer.close();
                    }

                    content = new byte[(int) file.length()];

                    FileInputStream reader = new FileInputStream(file);

                    reader.read(content, 0, content.length);
                    reader.close();

                    Log.h(TAG, content, content.length);
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                }

                sSemaphore.release();
            }
        }.start();
    }

    /**
     * @return {@link IPinpadManager}
     */
    @Override
    public IPinpadManager getPinpadManager(Bundle bundle) {
        // Log.d(TAG, "getPinpadManager");

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
