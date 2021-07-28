package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.util.Locale.US;

public class CLX {
    private static final String TAG = CLX.class.getSimpleName();

    private CLX() {
        Log.d(TAG, "CLX");

        /* Nothing to do */
    }

    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,   new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)]);

        return output;
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_DSPMSG   = input.getString(ABECS.SPE_DSPMSG);
        String SPE_MFNAME   = input.getString(ABECS.SPE_MFNAME);

        if (SPE_DSPMSG != null) { /* 2021-07-26: BCPP 001.19 from Verifone is ignoring both
                                   * SPE_DSPMSG and SPE_MFNAME */
            stream[1].write(String.format(US, "%.128s", SPE_DSPMSG).getBytes());
        }

        if (SPE_MFNAME != null) {
            SPE_MFNAME = SPE_MFNAME.toUpperCase();

            stream[1].write(String.format(US, "%-8.8s", SPE_MFNAME).getBytes());
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(), String.format(US, "%03d", CMD_DATA.length).getBytes(), CMD_DATA);
    }
}
