package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class GOX {
    private static final String
            TAG = GOX.class.getSimpleName();

    private GOX() {
        Log.d(TAG, "GCX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_ACQREF);
        list.add(ABECS.SPE_TRNTYPE);
        list.add(ABECS.SPE_AMOUNT);
        list.add(ABECS.SPE_CASHBACK);
        list.add(ABECS.SPE_TRNCURR);
        list.add(ABECS.SPE_GOXOPT);
        list.add(ABECS.SPE_MTHDPIN);
        list.add(ABECS.SPE_KEYIDX);
        list.add(ABECS.SPE_WKENC);
        list.add(ABECS.SPE_DSPMSG);
        list.add(ABECS.SPE_TRMPAR);
        list.add(ABECS.SPE_EMVDATA);
        list.add(ABECS.SPE_TAGLIST);
        list.add(ABECS.SPE_TIMEOUT);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.PP_GOXRES);
        list.add(ABECS.PP_PINBLK);
        list.add(ABECS.PP_KSN);
        list.add(ABECS.PP_EMVDATA);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list);
    }
}
