package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class GIX {
    private static final String TAG = GIX.class.getSimpleName();

    private GIX() {
        Log.d(TAG, "GIX");

        /* Nothing to do */
    }

    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] RSP_LEN1     = new byte[3];
        byte[] RSP_DATA     = null;

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);
        System.arraycopy(input, 6, RSP_LEN1, 0, 3);

        ABECS.STAT STAT = ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)];

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,    new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT,  STAT);

        if (STAT != ABECS.STAT.ST_OK) return output;

        RSP_DATA = new byte[DataUtility.byteArrayToInt(RSP_LEN1, RSP_LEN1.length)];

        System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

        int i = 0;

        do {
            byte[] T = new byte[4];
            byte[] L = new byte[4];

            System.arraycopy(RSP_DATA, i, T, 2, 2); i += 2;

            System.arraycopy(RSP_DATA, i, L, 2, 2); i += 2;

            int threshold = ByteBuffer.wrap(L).getInt();

            byte[] V = new byte[threshold];

            System.arraycopy(RSP_DATA, i, V, 0, threshold);

            i += threshold;

            switch (ByteBuffer.wrap(T).getInt()) {
                case 0x8001:
                    output.putString(ABECS.PP_SERNUM,  new String(V));
                    break;

                case 0x8002:
                    output.putString(ABECS.PP_PARTNBR, new String(V));
                    break;

                default:
                    /* Nothing to do */
                    break;
            }
        } while (i < RSP_DATA.length);

        return output;
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        byte[] CMD_LEN1     = new byte[3];
        byte[] CMD_DATA     = stream[1].toByteArray();

        CMD_LEN1 = String.format(US, "%03d", CMD_DATA.length).getBytes();

        stream[0].write(CMD_ID.getBytes(UTF_8));
        stream[0].write(CMD_LEN1);
        stream[0].write(CMD_DATA);

        return stream[0].toByteArray();
    }
}
