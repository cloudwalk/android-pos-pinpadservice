package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class OPN {
    private static final String
            TAG = OPN.class.getSimpleName();

    private OPN() {
        Log.d(TAG, "OPN");

        /* Nothing to do */
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] OPN_CRKSLEN  = new byte[3];
        byte[] OPN_CRKSEC   = new byte[512];

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,    new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT,  ABECS.STAT.values()[DataUtility.getIntFromByteArray(RSP_STAT, RSP_STAT.length)]);

        if (length > 6) {
            System.arraycopy(input,  9, OPN_CRKSLEN, 0, 3);
            System.arraycopy(input, 12, OPN_CRKSEC,  0, 512);

            output.putString(ABECS.OPN_CRKSLEN, new String(OPN_CRKSLEN));
            output.putString(ABECS.OPN_CRKSEC,  new String(OPN_CRKSEC));
        }

        return output;
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String OPN_OPMODE   = input.getString(ABECS.OPN_OPMODE);
        byte[] OPN_MODLEN   = new byte[3];
        String OPN_MOD      = input.getString(ABECS.OPN_MOD);
        byte[] OPN_EXPLEN   = new byte[1];
        String OPN_EXP      = input.getString(ABECS.OPN_EXP);

        if (OPN_OPMODE != null) {
            stream[1].write(("" + OPN_OPMODE).getBytes(UTF_8));

            OPN_MODLEN = String.format(US, "%03d", (OPN_MOD.length() / 2)).getBytes(UTF_8);

            stream[1].write(OPN_MODLEN);
            stream[1].write(OPN_MOD.getBytes(UTF_8));

            OPN_EXPLEN = String.format(US, "%01d", (OPN_EXP.length() / 2)).getBytes(UTF_8);

            stream[1].write(OPN_EXPLEN);
            stream[1].write(OPN_EXP.getBytes(UTF_8));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
