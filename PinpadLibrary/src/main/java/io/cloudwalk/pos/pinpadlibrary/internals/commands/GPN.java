package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
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

        Bundle output = new Bundle();

        byte[] CMD_ID       = new byte[3];
        byte[] GPN_METHOD   = new byte[1];
        byte[] GPN_KEYIDX   = new byte[2];
        byte[] GPN_WKENC    = new byte[32];
        byte[] GPN_PANLEN   = new byte[2];
        byte[] GPN_PAN      = new byte[19];
        byte[] GPN_ENTRIES  = new byte[1];
        byte[] GPN_MIN1     = new byte[2];
        byte[] GPN_MAX1     = new byte[2];
        byte[] GPN_MSG1     = new byte[32];

        System.arraycopy(input,  0, CMD_ID,      0,  3);
        System.arraycopy(input,  6, GPN_METHOD,  0,  1);
        System.arraycopy(input,  7, GPN_KEYIDX,  0,  2);
        System.arraycopy(input,  9, GPN_WKENC,   0, 32);
        System.arraycopy(input, 41, GPN_PANLEN,  0,  2);
        System.arraycopy(input, 43, GPN_PAN,     0, 19);
        System.arraycopy(input, 62, GPN_ENTRIES, 0,  1);
        System.arraycopy(input, 63, GPN_MIN1,    0,  2);
        System.arraycopy(input, 65, GPN_MAX1,    0,  2);
        System.arraycopy(input, 67, GPN_MSG1,    0, 32);

        output.putString(ABECS.CMD_ID,          new String(CMD_ID));
        output.putString(ABECS.GPN_METHOD,      new String(GPN_METHOD));
        output.putString(ABECS.GPN_KEYIDX,      new String(GPN_KEYIDX));
        output.putString(ABECS.GPN_WKENC,       new String(GPN_WKENC));
        output.putString(ABECS.GPN_PAN,         new String(GPN_PAN));
        output.putString(ABECS.GPN_ENTRIES,     new String(GPN_ENTRIES));
        output.putString(ABECS.GPN_MIN1,        new String(GPN_MIN1));
        output.putString(ABECS.GPN_MAX1,        new String(GPN_MAX1));
        output.putString(ABECS.GPN_MSG1,        new String(GPN_MSG1));

        return output;
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

        ABECS.STAT status   = ABECS.STAT.values()[DataUtility.getIntFromByteArray(RSP_STAT, RSP_STAT.length)];
            Bundle output   = new Bundle();

        output.putString      (ABECS.RSP_ID, new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT, status);

        if (status != ABECS.STAT.ST_OK) return output;

        System.arraycopy(input,  9, GPN_PINBLK, 0, 16);
        System.arraycopy(input, 25, GPN_KSN,    0, 20);

        output.putString(ABECS.GPN_PINBLK, new String(GPN_PINBLK));
        output.putString(ABECS.GPN_KSN,    new String(GPN_KSN));

        return output;
    }

    public static byte[] buildRequestDataPacket(Bundle input)
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
}
