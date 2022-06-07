package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class MNU {
    private static final String
            TAG = MNU.class.getSimpleName();

    private MNU() {
        Log.d(TAG, "MNU");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_TIMEOUT);
        list.add(ABECS.SPE_DSPMSG);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.PP_VALUE);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list);
    }
}
