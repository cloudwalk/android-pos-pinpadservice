package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class CEX {
    private static final String
            TAG = CEX.class.getSimpleName();

    private CEX() {
        Log.d(TAG, "CEX");

        /* Nothing to do */
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] RSP_LEN1     = new byte[3];
        byte[] RSP_DATA     = null;

        Bundle output = PinpadUtility.CMD.parseResponseDataPacket(input, length);

        switch ((ABECS.STAT) output.getSerializable(ABECS.RSP_STAT)) {
            case ST_OK:
                System.arraycopy(input, 6, RSP_LEN1, 0, 3);

                RSP_DATA = new byte[DataUtility.getIntFromByteArray(RSP_LEN1, RSP_LEN1.length)];

                System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

                output.putAll(PinpadUtility.parseResponseTLV(RSP_DATA, RSP_DATA.length));

                /* no break */

            default:
                return output;
        }
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_CEXOPT   = input.getString(ABECS.SPE_CEXOPT);
        String SPE_TIMEOUT  = input.getString(ABECS.SPE_TIMEOUT);
        String SPE_PANMASK  = input.getString(ABECS.SPE_PANMASK);

        if (SPE_CEXOPT  != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.A, "0006", SPE_CEXOPT));
        }

        if (SPE_TIMEOUT != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.X, "000C", SPE_TIMEOUT));
        }

        if (SPE_PANMASK != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0023", SPE_PANMASK));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
