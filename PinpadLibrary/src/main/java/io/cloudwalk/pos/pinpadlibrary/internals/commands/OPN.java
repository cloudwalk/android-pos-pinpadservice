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

public class OPN {
    private static final String
            TAG = OPN.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID, new String(array, 0, 3));

        if (length > 3) {
            if (CMD.parseInt(array, 3, 3) > 0) {
                request.put(ABECS.OPN_OPMODE, new String(array, 6, 1));

                int OPN_MODLEN = CMD.parseInt(array, 7, 3);

                request.put(ABECS.OPN_MOD, new String(array, 10, OPN_MODLEN * 2));

                OPN_MODLEN = (OPN_MODLEN * 2) + 10;

                int OPN_EXPLEN = CMD.parseInt(array, OPN_MODLEN++, 1);

                request.put(ABECS.OPN_EXP, new String(array, OPN_MODLEN, OPN_EXPLEN * 2));
            }
        }

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
                if (length >= 10) {
                    response[0].put(ABECS.OPN_CRKSEC, new String(array, 12, 512));
                }
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

        byte[] CMD_ID = null;

        try {
            JSONObject request = new JSONObject(string);

            if (request.has(ABECS.OPN_OPMODE)) {
                byte[] OPN_OPMODE = null;
                byte[] OPN_MODLEN = null;       byte[] OPN_MOD    = null;
                byte[] OPN_EXPLEN = null;       byte[] OPN_EXP    = null;

                try {
                    OPN_OPMODE = request.getString(ABECS.OPN_OPMODE).getBytes(UTF_8);
                    OPN_MOD    = request.getString(ABECS.OPN_MOD)   .getBytes(UTF_8);
                    OPN_EXP    = request.getString(ABECS.OPN_EXP)   .getBytes(UTF_8);

                    stream[1].write(OPN_OPMODE);

                    OPN_MODLEN = String.format(US, "%03d", (OPN_MOD.length / 2)).getBytes(UTF_8);

                    stream[1].write(OPN_MODLEN);
                    stream[1].write(OPN_MOD);

                    OPN_EXPLEN = String.format(US, "%01d", (OPN_EXP.length / 2)).getBytes(UTF_8);

                    stream[1].write(OPN_EXPLEN);
                    stream[1].write(OPN_EXP);
                } finally {
                    ByteUtility.clear(OPN_OPMODE, OPN_MODLEN, OPN_MOD, OPN_EXPLEN, OPN_EXP);
                }
            }

            CMD_ID = request.getString(ABECS.CMD_ID).getBytes(UTF_8);

            stream[0].write(CMD_ID);

            byte[] CMD_DATA = null;

            try {
                CMD_DATA = stream[1].toByteArray();

                stream[0].write(String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8));
                stream[0].write(CMD_DATA);
            } finally {
                ByteUtility.clear(CMD_DATA);
            }

            byte[] array = stream[0].toByteArray();

            return array;
        } finally {
            ByteUtility.clear(stream);

            ByteUtility.clear(CMD_ID);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        byte[] RSP_ID      = null;
        byte[] RSP_LEN1    = null;
        byte[] OPN_CRKSLEN = null;
        byte[] OPN_CRKSEC  = null;
        byte[] RSP_DATA    = null;

        try {
            JSONObject json = new JSONObject(string);

            RSP_ID = json.getString(ABECS.RSP_ID).getBytes(UTF_8);

            stream[0].write(RSP_ID);

            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String entry = it.next();

                byte[] array = null;

                switch (entry) {
                    case ABECS.RSP_ID:
                        /* Nothing to do */
                        break;

                    case ABECS.RSP_STAT:
                        array = String.format(US, "%03d", ABECS.STAT.valueOf(json.getString(entry)).ordinal()).getBytes(UTF_8);

                        stream[0].write(array);
                        break;

                    case ABECS.OPN_CRKSEC:
                        OPN_CRKSEC  = json.getString(entry).getBytes(UTF_8);

                        OPN_CRKSLEN = String.format(US, "%03d", OPN_CRKSEC.length / 2).getBytes(UTF_8);

                        stream[1].write(OPN_CRKSLEN);
                        stream[1].write(OPN_CRKSEC );
                        break;

                    default:
                        throw new RuntimeException("Unknown or unhandled TAG [" + entry + "]");
                }

                ByteUtility.clear(array);
            }

            RSP_DATA = stream[1].toByteArray();
            RSP_LEN1 = String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8);

            stream[0].write(RSP_LEN1);
            stream[0].write(RSP_DATA);

            byte[] response = stream[0].toByteArray();

            return response;
        } finally {
            ByteUtility.clear(stream);

            ByteUtility.clear(RSP_ID, RSP_LEN1, OPN_CRKSLEN, OPN_CRKSEC, RSP_DATA);
        }
    }
}
