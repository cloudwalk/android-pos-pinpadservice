package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class GIX {
    private static final String
            TAG = GIX.class.getSimpleName();

    private static final byte[]
            SPE_IDLIST = new byte[] { 0x00, 0x01 };

    private GIX() {
        Log.d(TAG, "GIX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_IDLIST   = input.getString(ABECS.SPE_IDLIST);

        if (SPE_IDLIST != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.B, "0001", SPE_IDLIST));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
