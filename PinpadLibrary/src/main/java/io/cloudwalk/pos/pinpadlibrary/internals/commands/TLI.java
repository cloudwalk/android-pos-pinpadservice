package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class TLI {
    private static final String
            TAG = TLI.class.getSimpleName();

    private TLI() {
        Log.d(TAG, "TLI");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        Bundle response = new Bundle();

        response.putString(ABECS.CMD_ID,      new String(input, 0,  3));
        response.putString(ABECS.TLI_ACQIDX,  new String(input, 6,  2));
        response.putString(ABECS.TLI_TABVER,  new String(input, 8, 10));

        return response;
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        return PinpadUtility.CMD.parseResponseDataPacket(input, length);
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String TLI_ACQIDX   = input.getString(ABECS.TLI_ACQIDX);
        String TLI_TABVER   = input.getString(ABECS.TLI_TABVER);

        stream[1].write(TLI_ACQIDX.getBytes(UTF_8));
        stream[1].write(TLI_TABVER.getBytes(UTF_8));

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        return PinpadUtility.CMD.buildResponseDataPacket(input);
    }
}
