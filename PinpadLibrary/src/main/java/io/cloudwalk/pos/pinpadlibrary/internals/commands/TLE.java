package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Bundle;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class TLE {
    private static final String
            TAG = TLE.class.getSimpleName();

    private TLE() {
        Log.d(TAG, "TLE");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        return DataUtility.concatByteArray(input.getString(ABECS.CMD_ID).getBytes(UTF_8), "000".getBytes(UTF_8));
    }
}
