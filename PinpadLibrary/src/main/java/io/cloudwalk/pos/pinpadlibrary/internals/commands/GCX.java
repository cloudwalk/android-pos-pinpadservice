package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class GCX {
    private static final String
            TAG = GCX.class.getSimpleName();

    private GCX() {
        Log.d(TAG, "GCX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_TRNTYPE);
        list.add(ABECS.SPE_ACQREF);
        list.add(ABECS.SPE_APPTYPE);
        list.add(ABECS.SPE_AIDLIST);
        list.add(ABECS.SPE_AMOUNT);
        list.add(ABECS.SPE_CASHBACK);
        list.add(ABECS.SPE_TRNCURR);
        list.add(ABECS.SPE_TRNDATE);
        list.add(ABECS.SPE_TRNTIME);
        list.add(ABECS.SPE_GCXOPT);
        list.add(ABECS.SPE_PANMASK);
        list.add(ABECS.SPE_EMVDATA);
        list.add(ABECS.SPE_TAGLIST);
        list.add(ABECS.SPE_TIMEOUT);
        list.add(ABECS.SPE_DSPMSG);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.PP_CARDTYPE);
        list.add(ABECS.PP_ICCSTAT);
        list.add(ABECS.PP_AIDTABINFO);
        list.add(ABECS.PP_PAN);
        list.add(ABECS.PP_PANSEQNO);
        list.add(ABECS.PP_TRK1INC);
        list.add(ABECS.PP_TRK2INC);
        list.add(ABECS.PP_TRK3INC);
        list.add(ABECS.PP_CHNAME);
        list.add(ABECS.PP_LABEL);
        list.add(ABECS.PP_ISSCNTRY);
        list.add(ABECS.PP_CARDEXP);
        list.add(ABECS.PP_EMVDATA);
        list.add(ABECS.PP_DEVTYPE);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list);
    }
}
