package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;

public class GPN {
    private static final String
            TAG = GPN.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID,      new String(array,  0,  3));
        request.put(ABECS.GPN_METHOD,  new String(array,  6,  1));
        request.put(ABECS.GPN_KEYIDX,  new String(array,  7,  2));
        request.put(ABECS.GPN_WKENC,   new String(array,  9, 32));
        request.put(ABECS.GPN_PAN,     new String(array, 43, 19));
        request.put(ABECS.GPN_ENTRIES, new String(array, 62,  1));
        request.put(ABECS.GPN_MIN1,    new String(array, 63,  2));
        request.put(ABECS.GPN_MAX1,    new String(array, 65,  2));
        request.put(ABECS.GPN_MSG1,    new String(array, 67, 32));

        return request.toString();
    }

    public static String parseResponseDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        JSONObject[] response = {
                new JSONObject(),
                null
        };

        response[0].put(ABECS.RSP_ID, new String(array, 0, 3));

        String RSP_STAT = ABECS.STAT.values()[CMD.parseInt(array, 3, 3)].name();

        response[0].put(ABECS.RSP_STAT, RSP_STAT);

        switch (RSP_STAT) {
            case "ST_OK":
                response[0].put(ABECS.GPN_PINBLK, new String(array,  9, 16));
                response[0].put(ABECS.GPN_KSN,    new String(array, 25, 20));
                /* no break */

            default:
                return response[0].toString();
        }
    }

    public static byte[] buildRequestDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        byte[] CMD_ID      = null;
        byte[] GPN_METHOD  = null;          byte[] GPN_KEYIDX  = null;
        byte[] GPN_WKENC   = null;          byte[] GPN_PANLEN  = null;
        byte[] GPN_ENTRIES = null;          byte[] GPN_MIN1    = null;
        byte[] GPN_MAX1    = null;          byte[] GPN_MSG1    = null;

        try {
            JSONObject request = new JSONObject(string);

            CMD_ID      = request.getString(ABECS.CMD_ID)     .getBytes(UTF_8);
            GPN_METHOD  = request.getString(ABECS.GPN_METHOD) .getBytes(UTF_8);
            GPN_KEYIDX  = request.getString(ABECS.GPN_KEYIDX) .getBytes(UTF_8);
            GPN_WKENC   = request.getString(ABECS.GPN_WKENC)  .getBytes(UTF_8);
            GPN_ENTRIES = request.getString(ABECS.GPN_ENTRIES).getBytes(UTF_8);
            GPN_MIN1    = request.getString(ABECS.GPN_MIN1)   .getBytes(UTF_8);
            GPN_MAX1    = request.getString(ABECS.GPN_MAX1)   .getBytes(UTF_8);
            GPN_MSG1    = request.getString(ABECS.GPN_MSG1)   .getBytes(UTF_8);

            String GPN_PAN = request.getString(ABECS.GPN_PAN);

            GPN_PANLEN = String.format(US, "%02d",    GPN_PAN.trim().length()).getBytes(UTF_8);
            GPN_PAN    = String.format(US, "%19.19s", GPN_PAN);

            stream[0].write(CMD_ID);
            stream[1].write(GPN_METHOD);    stream[1].write(GPN_KEYIDX);
            stream[1].write(GPN_WKENC);     stream[1].write(GPN_PANLEN);

            byte[] array = null;

            try {
                array = GPN_PAN.getBytes(UTF_8);

                stream[1].write(array);
            } finally {
                ByteUtility.clear(array);
            }

            stream[1].write(GPN_ENTRIES);   stream[1].write(GPN_MIN1);
            stream[1].write(GPN_MAX1);      stream[1].write(GPN_MSG1);

            byte[] CMD_DATA = null;

            try {
                CMD_DATA = stream[1].toByteArray();

                stream[0].write(String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8));
                stream[0].write(CMD_DATA);
            } finally {
                ByteUtility.clear(CMD_DATA);
            }

            array = stream[0].toByteArray();

            return array;
        } finally {
            ByteUtility.clear(stream);

            ByteUtility.clear(CMD_ID, GPN_METHOD, GPN_KEYIDX, GPN_WKENC, GPN_PANLEN,
                    GPN_ENTRIES, GPN_MIN1, GPN_MAX1, GPN_MSG1);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        byte[] RSP_ID     = null;
        byte[] RSP_STAT   = null;       byte[] RSP_LEN1   = null;
        byte[] GPN_PINBLK = null;       byte[] GPN_KSN    = null;
        byte[] RSP_DATA   = null;

        try {
            JSONObject json = new JSONObject(string);

            RSP_ID     = json.getString(ABECS.RSP_ID)    .getBytes(UTF_8);
            GPN_PINBLK = json.optString(ABECS.GPN_PINBLK).getBytes(UTF_8);
            GPN_KSN    = json.optString(ABECS.GPN_KSN)   .getBytes(UTF_8);

            stream[1].write(GPN_PINBLK);
            stream[1].write(GPN_KSN);

            RSP_STAT = String.format(US, "%03d", ABECS.STAT.valueOf(json.getString(ABECS.RSP_STAT)).ordinal()).getBytes(UTF_8);

            RSP_DATA = stream[1].toByteArray();

            RSP_LEN1 = String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8);

            stream[0].write(RSP_ID);
            stream[0].write(RSP_STAT);
            stream[0].write(RSP_LEN1);
            stream[0].write(RSP_DATA);

            byte[] response = stream[0].toByteArray();

            return response;
        } finally {
            ByteUtility.clear(stream);

            ByteUtility.clear(RSP_ID, RSP_STAT, RSP_LEN1, GPN_PINBLK, GPN_KSN, RSP_DATA);
        }
    }
}
