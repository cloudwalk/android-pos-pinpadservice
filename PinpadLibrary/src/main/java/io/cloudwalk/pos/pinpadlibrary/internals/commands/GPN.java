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

public class GPN {
    private static final String
            TAG = GPN.class.getSimpleName();

    private GPN() {
        Log.d(TAG, "GPN");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        Bundle response = new Bundle();

        response.putString(ABECS.CMD_ID,      new String(input,  0,  3));
        response.putString(ABECS.GPN_METHOD,  new String(input,  6,  1));
        response.putString(ABECS.GPN_KEYIDX,  new String(input,  7,  2));
        response.putString(ABECS.GPN_WKENC,   new String(input,  9, 32));
        response.putString(ABECS.GPN_PAN,     new String(input, 43, 19));
        response.putString(ABECS.GPN_ENTRIES, new String(input, 62,  1));
        response.putString(ABECS.GPN_MIN1,    new String(input, 63,  2));
        response.putString(ABECS.GPN_MAX1,    new String(input, 65,  2));
        response.putString(ABECS.GPN_MSG1,    new String(input, 67, 32));

        return response;
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] GPN_PINBLK   = new byte[16];
        byte[] GPN_KSN      = new byte[20];

        System.arraycopy(input,  0, RSP_ID,     0,  3);
        System.arraycopy(input,  3, RSP_STAT,   0,  3);

        Bundle response = new Bundle();

        response.putString(ABECS.RSP_ID, new String(RSP_ID));
        response.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[PinpadUtility.getIntFromDigitsArray(RSP_STAT, RSP_STAT.length)]);

        if (ABECS.STAT.ST_OK != response.getSerializable(ABECS.RSP_STAT)) {
            return response;
        }

        System.arraycopy(input,  9, GPN_PINBLK, 0, 16);
        System.arraycopy(input, 25, GPN_KSN,    0, 20);

        response.putString(ABECS.GPN_PINBLK, new String(GPN_PINBLK));
        response.putString(ABECS.GPN_KSN,    new String(GPN_KSN));

        return response;
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String GPN_METHOD   = input.getString(ABECS.GPN_METHOD);
        String GPN_KEYIDX   = input.getString(ABECS.GPN_KEYIDX);
        String GPN_WKENC    = input.getString(ABECS.GPN_WKENC);
        String GPN_PAN      = input.getString(ABECS.GPN_PAN);
        String GPN_ENTRIES  = input.getString(ABECS.GPN_ENTRIES);
        String GPN_MIN1     = input.getString(ABECS.GPN_MIN1);
        String GPN_MAX1     = input.getString(ABECS.GPN_MAX1);
        String GPN_MSG1     = input.getString(ABECS.GPN_MSG1);

        byte[] GPN_PANLEN = String.format(US, "%02d", GPN_PAN.trim().length()).getBytes(UTF_8);

        GPN_PAN = String.format(US, "%19.19s", GPN_PAN);

        stream[1].write(GPN_METHOD .getBytes(UTF_8));
        stream[1].write(GPN_KEYIDX .getBytes(UTF_8));
        stream[1].write(GPN_WKENC  .getBytes(UTF_8));
        stream[1].write(GPN_PANLEN);
        stream[1].write(GPN_PAN    .getBytes(UTF_8));
        stream[1].write(GPN_ENTRIES.getBytes(UTF_8));
        stream[1].write(GPN_MIN1   .getBytes(UTF_8));
        stream[1].write(GPN_MAX1   .getBytes(UTF_8));
        stream[1].write(GPN_MSG1   .getBytes(UTF_8));

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket::input [" + input + "]");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        String RSP_ID       = null;
        int    RSP_STAT     = ABECS.STAT.ST_INTERR.ordinal();
        byte[] GPN_PINBLK   = null;
        byte[] GPN_KSN      = null;

        for (String T : input.keySet()) {
            switch (T) {
                case ABECS.RSP_ID:
                    RSP_ID = input.getString(T);
                    break;

                case ABECS.RSP_STAT:
                    RSP_STAT = ((ABECS.STAT) input.getSerializable(T)).ordinal();
                    break;

                case ABECS.GPN_PINBLK:
                    GPN_PINBLK = input.getString(T).getBytes(UTF_8);
                    break;

                case ABECS.GPN_KSN:
                    GPN_KSN = input.getString(T).getBytes(UTF_8);
                    break;

                default:
                    throw new RuntimeException("Unknown or unhandled TAG [" + T + "]");
            }
        }

        stream.write(RSP_ID.getBytes(UTF_8));
        stream.write(String.format(US, "%03d", RSP_STAT).getBytes(UTF_8));

        if (RSP_STAT != ABECS.STAT.ST_OK.ordinal()) {
            stream.write(0x00);
            stream.write(0x00);
            stream.write(0x00);

            return stream.toByteArray();
        }

        int RSP_LEN1 = GPN_PINBLK.length + GPN_KSN.length;

        stream.write(String.format(US, "%03d", RSP_LEN1).getBytes(UTF_8));
        stream.write(GPN_PINBLK); stream.write(GPN_KSN);

        return stream.toByteArray();
    }
}
