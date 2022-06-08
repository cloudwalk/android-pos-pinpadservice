package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class FCX {
    private static final String
            TAG = FCX.class.getSimpleName();

    private FCX() {
        Log.d(TAG, "FCX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_FCXOPT);
        list.add(ABECS.SPE_ARC);
        list.add(ABECS.SPE_EMVDATA);
        list.add(ABECS.SPE_TAGLIST);
        list.add(ABECS.SPE_TIMEOUT);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.PP_FCXRES);
        list.add(ABECS.PP_EMVDATA);
        list.add(ABECS.PP_ISRESULTS);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list);
    }
}
