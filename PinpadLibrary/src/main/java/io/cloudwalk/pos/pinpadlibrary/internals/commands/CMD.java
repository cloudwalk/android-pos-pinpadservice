package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;

public class CMD {
    private static final String
            TAG = CMD.class.getSimpleName();

    private CMD() {
        /* Nothing to do */
    }

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        JSONObject[] request = {
                new JSONObject(),
                null
        };

        request[0].put(ABECS.CMD_ID, new String(array, 0, 3));

        if (length >= 7) {
            String CMD_DATA = PinpadUtility.parseTLV(array, 6, array.length);

            for (Iterator<String> it = (request[1] = new JSONObject(CMD_DATA)).keys(); it.hasNext(); ) {
                String entry = it.next();
                request[0].put(entry, request[1].getString(entry));
            }
        }

        return request[0].toString();
    }

    public static String parseResponseDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        JSONObject[] response = {
                new JSONObject(),
                null
        };

        response[0].put(ABECS.RSP_ID, new String(array, 0, 3));

        response[0].put(ABECS.RSP_STAT, ABECS.STAT.values()[parseInt(array, 3, 3)].name());

        if (length >= 10) {
            String RSP_DATA = PinpadUtility.parseTLV(array, 9, array.length);

            for (Iterator<String> it = (response[1] = new JSONObject(RSP_DATA)).keys(); it.hasNext(); ) {
                String entry = it.next();
                response[0].put(entry, response[1].getString(entry));
            }
        }

        return response[0].toString();
    }

    public static byte[] buildRequestDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        byte[] CMD_ID   = null;
        byte[] CMD_LEN1 = null;
        byte[] CMD_DATA = null;

        try {
            JSONObject json = new JSONObject(string);

            CMD_ID = json.getString(ABECS.CMD_ID).getBytes(UTF_8);

            stream[0].write(CMD_ID);

            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String entry = it.next();

                switch (entry) {
                    case ABECS.CMD_ID:
                        /* Nothing to do */
                        break;

                    default:
                        byte[] array = null;

                        try {
                            array = PinpadUtility.buildTLV(entry, json.getString(entry));

                            stream[1].write(array);
                        } finally {
                            if (array != null) {
                                Log.h(TAG, array, array.length);
                            }

                            ByteUtility.clear(array);
                        }
                        break;
                }
            }

            CMD_DATA = stream[1].toByteArray();

            CMD_LEN1 = String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8);

            stream[0].write(CMD_LEN1);
            stream[0].write(CMD_DATA);

            byte[] request = stream[0].toByteArray();

            return request;
        } finally {
            ByteUtility.clear(stream);

            ByteUtility.clear(CMD_ID, CMD_LEN1, CMD_DATA);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        byte[] RSP_ID   = null;
        byte[] RSP_LEN1 = null;
        byte[] RSP_DATA = null;

        try {
            JSONObject json = new JSONObject(string);

            RSP_ID = json.getString(ABECS.RSP_ID).getBytes(UTF_8);

            stream[0].write(RSP_ID);

            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String entry = it.next();

                byte[] array = null;

                try {
                    switch (entry) {
                        case ABECS.RSP_ID:
                            /* Nothing to do */
                            break;

                        case ABECS.RSP_STAT:
                            array = String.format(US, "%03d", ABECS.STAT.valueOf(json.getString(entry)).ordinal()).getBytes(UTF_8);

                            stream[0].write(array);
                            break;

                        default:
                            array = PinpadUtility.buildTLV(entry, json.getString(entry));

                            stream[1].write(array);
                            break;
                    }
                } finally {
                    if (array != null) {
                        Log.h(TAG, array, array.length);
                    }

                    ByteUtility.clear(array);
                }
            }

            RSP_DATA = stream[1].toByteArray();

            RSP_LEN1 = String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8);

            stream[0].write(RSP_LEN1);
            stream[0].write(RSP_DATA);

            byte[] response = stream[0].toByteArray();

            return response;
        } finally {
            ByteUtility.clear(stream);

            ByteUtility.clear(RSP_ID, RSP_LEN1, RSP_DATA);
        }
    }

    public static int parseInt(byte[] array, int offset, int length)
            throws Exception {
        // Log.d(TAG, "parseInt");

        length = Math.max(length, 0);
        length = Math.min(length, array.length);

        offset = Math.max(offset, 0);

        int response = 0;

        for (int i = length - 1, j = offset; (j < array.length && i >= 0); i--, j++) {
            if (array[j] < 0x30 || array[j] > 0x39) {
                String message = String.format(US, "parseInt::array[%d] [%02X]", j, array[j]);

                throw new IllegalArgumentException(message);
            }

            response += (array[j] - 0x30) * ((i > 0) ? (Math.pow(10, i)) : 1);
        }

        return response;
    }
}
