package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class CLX {
    private static final String
            TAG = CLX.class.getSimpleName();

    private CLX() {
        Log.d(TAG, "CLX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_DSPMSG);
        list.add(ABECS.SPE_MFNAME);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        return PinpadUtility.CMD.buildResponseDataPacket(input);
    }
}
