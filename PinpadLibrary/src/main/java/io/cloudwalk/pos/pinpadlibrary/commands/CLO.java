package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import io.cloudwalk.pos.loglibrary.Log;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CLO {
    private static final String TAG = CLO.class.getSimpleName();

    private CLO() {
        Log.d(TAG, "CLO");

        /* Nothing to do */
    }

    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        return new Bundle();
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        return TAG.getBytes(UTF_8);
    }
}
