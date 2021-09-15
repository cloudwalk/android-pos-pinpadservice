package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class GCD {
    private static final String
            TAG = GCD.class.getSimpleName();

    private GCD() {
        Log.d(TAG, "GCD");

        /* Nothing to do */
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        return PinpadUtility.CMD.parseResponseDataPacket(input, length);
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_MSGIDX   = input.getString(ABECS.SPE_MSGIDX);
        String SPE_MINDIG   = input.getString(ABECS.SPE_MINDIG);
        String SPE_MAXDIG   = input.getString(ABECS.SPE_MAXDIG);
        String SPE_TIMEOUT  = input.getString(ABECS.SPE_TIMEOUT);

        if (SPE_MSGIDX != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.X, "000B", SPE_MSGIDX));
        }

        if (SPE_MINDIG != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.X, "000D", SPE_MINDIG));
        }

        if (SPE_MAXDIG != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.X, "000E", SPE_MAXDIG));
        }

        if (SPE_TIMEOUT != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.X, "000C", SPE_TIMEOUT));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
