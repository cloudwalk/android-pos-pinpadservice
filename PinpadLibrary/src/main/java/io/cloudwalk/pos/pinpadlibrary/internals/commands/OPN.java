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

public class OPN {
    private static final String
            TAG = OPN.class.getSimpleName();

    private OPN() {
        Log.d(TAG, "OPN");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        byte[] CMD_ID       = new byte[3];
        byte[] OPN_OPMODE   = new byte[1];
        byte[] OPN_MODLEN   = new byte[3];
        byte[] OPN_MOD      = null;
        byte[] OPN_EXPLEN   = new byte[1];
        byte[] OPN_EXP      = null;

        System.arraycopy(input, 0, CMD_ID, 0, 3);

        Bundle response = new Bundle();

        response.putString(ABECS.CMD_ID, new String(CMD_ID));

        if (length < 7) {
            return response;
        }

        System.arraycopy(input, 6, OPN_OPMODE, 0, 1);
        System.arraycopy(input, 7, OPN_MODLEN, 0, 3);

        OPN_MOD = new byte[PinpadUtility.getIntFromDigitsArray(OPN_MODLEN, OPN_MODLEN.length) * 2];

        System.arraycopy(input,                  10, OPN_MOD,    0, OPN_MOD.length);
        System.arraycopy(input, OPN_MOD.length + 10, OPN_EXPLEN, 0, 1);

        response.putString(ABECS.OPN_OPMODE, new String(OPN_OPMODE));
        response.putString(ABECS.OPN_MOD,    new String(OPN_MOD));

        OPN_EXP = new byte[PinpadUtility.getIntFromDigitsArray(OPN_EXPLEN, OPN_EXPLEN.length) * 2];

        System.arraycopy(input, OPN_MOD.length + 11, OPN_EXP,    0, OPN_EXP.length);

        response.putString(ABECS.OPN_EXP,    new String(OPN_EXP));

        return response;
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

        Bundle response = new Bundle();

        response.putString(ABECS.RSP_ID, new String(RSP_ID));
        response.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[PinpadUtility.getIntFromDigitsArray(RSP_STAT, RSP_STAT.length)]);

        if (ABECS.STAT.ST_OK != response.getSerializable(ABECS.RSP_STAT)) {
            return response;
        }

        System.arraycopy(input,  9, OPN_CRKSLEN, 0,   3);
        System.arraycopy(input, 12, OPN_CRKSEC,  0, 512);

        response.putString(ABECS.OPN_CRKSEC, new String(OPN_CRKSEC));

        return response;
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String OPN_OPMODE   = input.getString(ABECS.OPN_OPMODE);
        String OPN_MOD      = input.getString(ABECS.OPN_MOD);
        String OPN_EXP      = input.getString(ABECS.OPN_EXP);

        if (OPN_OPMODE != null) {
            stream[1].write(("" + OPN_OPMODE).getBytes(UTF_8));

            byte[] OPN_MODLEN = String.format(US, "%03d", (OPN_MOD.length() / 2)).getBytes(UTF_8);

            stream[1].write(OPN_MODLEN);
            stream[1].write(OPN_MOD.getBytes(UTF_8));

            byte[] OPN_EXPLEN = String.format(US, "%01d", (OPN_EXP.length() / 2)).getBytes(UTF_8);

            stream[1].write(OPN_EXPLEN);
            stream[1].write(OPN_EXP.getBytes(UTF_8));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        String RSP_ID       = null;
        int    RSP_STAT     = ABECS.STAT.ST_INTERR.ordinal();
        byte[] OPN_CRKSEC   = null;

        for (String T : input.keySet()) {
            switch (T) {
                case ABECS.RSP_ID:
                    RSP_ID = input.getString(T);
                    break;

                case ABECS.RSP_STAT:
                    RSP_STAT = ((ABECS.STAT) input.getSerializable(T)).ordinal();
                    break;

                case ABECS.OPN_CRKSEC:
                    OPN_CRKSEC = input.getString(T).getBytes(UTF_8);
                    break;

                default:
                    throw new RuntimeException("Unknown or unhandled TAG [" + T + "]");
            }
        }

        stream.write(RSP_ID.getBytes(UTF_8));
        stream.write(String.format(US, "%03d", RSP_STAT).getBytes(UTF_8));

        if (OPN_CRKSEC == null || RSP_STAT != ABECS.STAT.ST_OK.ordinal()) {
            stream.write(0x00);
            stream.write(0x00);
            stream.write(0x00);

            return stream.toByteArray();
        }

        stream.write(String.format(US, "%03d", OPN_CRKSEC.length + 3).getBytes(UTF_8));
        stream.write(String.format(US, "%03d", OPN_CRKSEC.length / 2).getBytes(UTF_8));
        stream.write(OPN_CRKSEC);

        return stream.toByteArray();
    }
}
