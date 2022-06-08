package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class GTK {
    private static final String
            TAG = GTK.class.getSimpleName();

    private GTK() {
        Log.d(TAG, "GTK");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_TRACKS);
        list.add(ABECS.SPE_MTHDDAT);
        list.add(ABECS.SPE_IVCBC);
        list.add(ABECS.SPE_OPNDIG);
        list.add(ABECS.SPE_KEYIDX);
        list.add(ABECS.SPE_WKENC);
        list.add(ABECS.SPE_PBKMOD);
        list.add(ABECS.SPE_PBKEXP);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.PP_ENCPAN);
        list.add(ABECS.PP_TRACK1);
        list.add(ABECS.PP_TRACK2);
        list.add(ABECS.PP_TRACK3);
        list.add(ABECS.PP_TRK1KSN);
        list.add(ABECS.PP_TRK2KSN);
        list.add(ABECS.PP_TRK3KSN);
        list.add(ABECS.PP_ENCPANKSN);
        list.add(ABECS.PP_ENCKRAND);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list);
    }
}
