package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class OPN {
    private static final String TAG = OPN.class.getSimpleName();

    private OPN() {
        Log.d(TAG, "OPN");

        /* Nothing to do */
    }

    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] OPN_CRKSLEN  = new byte[3];
        byte[] OPN_CRKSEC   = new byte[512];

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,    new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT,  ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)]);

        if (length > 6) {
            System.arraycopy(input,  9, OPN_CRKSLEN, 0, 3);
            System.arraycopy(input, 12, OPN_CRKSEC,  0, 512);

            output.putLong  (ABECS.OPN_CRKSLEN, DataUtility.byteArrayToInt(OPN_CRKSLEN, OPN_CRKSLEN.length));
            output.putString(ABECS.OPN_CRKSEC,  new String(OPN_CRKSEC));
        }

        return output;
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        byte[] CMD_LEN1     = new byte[3];
        long   OPN_OPMODE   = input.getLong  (ABECS.OPN_OPMODE, -1);
        byte[] OPN_MODLEN   = new byte[3];
        String OPN_MOD      = input.getString(ABECS.OPN_MOD);
        byte[] OPN_EXPLEN   = new byte[1];
        String OPN_EXP      = input.getString(ABECS.OPN_EXP);

        switch ((int) OPN_OPMODE) {
            case -1:
                /* Nothing to do */
                break;

            default:
                stream[1].write(("" + OPN_OPMODE).getBytes());

                OPN_MODLEN = String.format(US, "%03d", (OPN_MOD.length() / 2)).getBytes();

                stream[1].write(OPN_MODLEN);
                stream[1].write(OPN_MOD.getBytes());

                OPN_EXPLEN = String.format(US, "%01d", (OPN_EXP.length() / 2)).getBytes();

                stream[1].write(OPN_EXPLEN);
                stream[1].write(OPN_EXP.getBytes());
                break;
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        CMD_LEN1 = String.format(US, "%03d", CMD_DATA.length).getBytes();

        stream[0].write(CMD_ID.getBytes(UTF_8));
        stream[0].write(CMD_LEN1);
        stream[0].write(CMD_DATA);

        return stream[0].toByteArray();
    }
}
