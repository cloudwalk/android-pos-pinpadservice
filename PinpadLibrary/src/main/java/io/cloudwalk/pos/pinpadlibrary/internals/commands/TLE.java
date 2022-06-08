package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class TLE {
    private static final String
            TAG = TLE.class.getSimpleName();

    private TLE() {
        Log.d(TAG, "TLE");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        Bundle response = new Bundle();

        response.putString(ABECS.CMD_ID, String.format(US, "%c%c%c", input[0], input[1], input[2]));

        return response;
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        return PinpadUtility.CMD.parseResponseDataPacket(input, length);
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        return DataUtility.concatByteArray(input.getString(ABECS.CMD_ID).getBytes(UTF_8), "000".getBytes(UTF_8));
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        return PinpadUtility.CMD.buildResponseDataPacket(input);
    }
}
