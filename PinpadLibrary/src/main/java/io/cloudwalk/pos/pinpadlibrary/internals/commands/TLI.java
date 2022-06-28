package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import org.jetbrains.annotations.NotNull;

import io.cloudwalk.loglibrary.Log;

public class TLI {
    private static final String
            TAG = TLI.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        throw new RuntimeException("TODO: parseRequestDataPacket");
    }

    public static String parseResponseDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        throw new RuntimeException("TODO: parseResponseDataPacket");
    }

    public static byte[] buildRequestDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        throw new RuntimeException("TODO: buildRequestDataPacket");
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        throw new RuntimeException("TODO: buildResponseDataPacket");
    }
}
