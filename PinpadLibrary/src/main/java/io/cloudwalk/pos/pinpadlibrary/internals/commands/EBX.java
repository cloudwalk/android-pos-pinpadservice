package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class EBX {
    private static final String
            TAG = EBX.class.getSimpleName();

    private EBX() {
        Log.d(TAG, "EBX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_DATAIN);
        list.add(ABECS.SPE_MTHDDAT);
        list.add(ABECS.SPE_KEYIDX);
        list.add(ABECS.SPE_WKENC);
        list.add(ABECS.SPE_IVCBC);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.PP_DATAOUT);
        list.add(ABECS.PP_KSN);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list);
    }
}
