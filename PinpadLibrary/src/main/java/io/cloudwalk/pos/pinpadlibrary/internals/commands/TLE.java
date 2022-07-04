package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import org.jetbrains.annotations.NotNull;

import io.cloudwalk.loglibrary.Log;

public class TLE {
    private static final String
            TAG = TLE.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        return CMD.parseRequestDataPacket(array, length);
    }

    public static String parseResponseDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        return CMD.parseResponseDataPacket(array, length);
    }

    public static byte[] buildRequestDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        return CMD.buildRequestDataPacket(string);
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        return CMD.buildResponseDataPacket(string);
    }
}
