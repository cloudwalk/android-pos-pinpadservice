package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class RMC {
    private static final String
            TAG = RMC.class.getSimpleName();

    private RMC() {
        Log.d(TAG, "RMC");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        Bundle output = new Bundle();

        byte[] CMD_ID       = new byte[3];
        byte[] RMC_MSG      = new byte[32];

        System.arraycopy(input, 0, CMD_ID,  0,  3);
        System.arraycopy(input, 6, RMC_MSG, 0, 32);

        output.putString(ABECS.CMD_ID,  new String(CMD_ID));
        output.putString(ABECS.RMC_MSG, new String(RMC_MSG));

        return output;
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String RMC_MSG      = input.getString(ABECS.RMC_MSG);

        stream[1].write(RMC_MSG.getBytes(UTF_8));

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
