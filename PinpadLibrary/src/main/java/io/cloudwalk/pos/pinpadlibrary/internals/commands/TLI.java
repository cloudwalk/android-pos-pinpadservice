package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
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

        Bundle output = new Bundle();

        byte[] CMD_ID       = new byte[3];
        byte[] TLI_ACQIDX   = new byte[2];
        byte[] TLI_TABVER   = new byte[10];

        System.arraycopy(input, 0, CMD_ID,     0,  3);
        System.arraycopy(input, 6, TLI_ACQIDX, 0,  2);
        System.arraycopy(input, 8, TLI_TABVER, 0, 10);

        output.putString(ABECS.CMD_ID,      new String(CMD_ID));
        output.putString(ABECS.TLI_ACQIDX,  new String(TLI_ACQIDX));
        output.putString(ABECS.TLI_TABVER,  new String(TLI_TABVER));

        return output;
    }

    public static byte[] buildRequestDataPacket(Bundle input)
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
}
