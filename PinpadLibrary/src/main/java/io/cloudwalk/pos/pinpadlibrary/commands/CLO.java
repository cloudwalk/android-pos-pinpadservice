package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class CLO {
    private static final String TAG = CLO.class.getSimpleName();

    private CLO() {
        Log.d(TAG, "CLO");

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

        output.putString      (ABECS.RSP_ID,    new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT,  ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)]);

        return output;
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        byte[] CMD_LEN1     = new byte[3];
        String CLO_MSG      = String.format(US, "%-32.32s", input.getString(ABECS.CLO_MSG, ""));

        stream[1].write(("" + CLO_MSG).getBytes());

        byte[] CMD_DATA = stream[1].toByteArray();

        CMD_LEN1 = String.format(US, "%03d", CMD_DATA.length).getBytes();

        stream[0].write(CMD_ID.getBytes(UTF_8));
        stream[0].write(CMD_LEN1);
        stream[0].write(CMD_DATA);

        return stream[0].toByteArray();
    }
}
