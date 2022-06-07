package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class CLX {
    private static final String
            TAG = CLX.class.getSimpleName();

    private CLX() {
        Log.d(TAG, "CLX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_DSPMSG   = input.getString(ABECS.SPE_DSPMSG);
        String SPE_MFNAME   = input.getString(ABECS.SPE_MFNAME);

        if (SPE_DSPMSG != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.S, "001B", SPE_DSPMSG));
        }

        if (SPE_MFNAME != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.S, "001E", SPE_MFNAME));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
