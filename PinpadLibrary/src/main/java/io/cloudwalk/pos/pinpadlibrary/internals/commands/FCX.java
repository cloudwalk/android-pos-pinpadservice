package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class FCX {
    private static final String
            TAG = FCX.class.getSimpleName();

    private FCX() {
        Log.d(TAG, "FCX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_FCXOPT   = input.getString(ABECS.SPE_FCXOPT);
        String SPE_ARC      = input.getString(ABECS.SPE_ARC);
        String SPE_EMVDATA  = input.getString(ABECS.SPE_EMVDATA);
        String SPE_TAGLIST  = input.getString(ABECS.SPE_TAGLIST);
        String SPE_TIMEOUT  = input.getString(ABECS.SPE_TIMEOUT);

        if (SPE_FCXOPT  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0019", SPE_FCXOPT));
        }

        if (SPE_ARC     != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.A, "001C", SPE_ARC));
        }

        if (SPE_EMVDATA != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.B, "0005", SPE_EMVDATA));
        }

        if (SPE_TAGLIST != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.B, "0004", SPE_TAGLIST));
        }

        if (SPE_TIMEOUT != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.X, "000C", SPE_TIMEOUT));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
