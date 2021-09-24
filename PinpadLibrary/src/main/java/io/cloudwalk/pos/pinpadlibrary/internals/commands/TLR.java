package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class TLR {
    private static final String
            TAG = TLR.class.getSimpleName();

    private TLR() {
        Log.d(TAG, "TLR");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String TLR_NREC     = input.getString(ABECS.TLR_NREC);
        String TLR_DATA     = input.getString(ABECS.TLR_DATA);

        TLR_DATA = TLR_DATA.length() > 999 ? TLR_DATA.substring(0, 999) : TLR_DATA;

        stream[1].write(TLR_NREC.getBytes(UTF_8));
        stream[1].write(TLR_DATA.getBytes(UTF_8));

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
