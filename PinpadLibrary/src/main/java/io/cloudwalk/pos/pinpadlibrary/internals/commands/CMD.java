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

    private static int _getInt(byte[] array, int offset, int length)
            throws Exception {
        // Log.d(TAG, "_getInt::array.length [" + array.length + "] offset [" + offset + "] length [" + length + "]");

        length = Math.max(length, 0);
        length = Math.min(length, array.length);

        offset = Math.max(offset, 0);

        int response = 0;

        for (int i = length - 1, j = offset; (j < array.length && i >= 0); i--, j++) {
            if (array[j] < 0x30 || array[j] > 0x39) {
                String message = String.format(US, "_getInt::array[%d] [%02X]", j, array[j]);

                throw new IllegalArgumentException(message);
            }

            response += (array[j] - 0x30) * ((i > 0) ? (Math.pow(10, i)) : 1);
        }

        return response;
    }

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

        response[0].put(ABECS.RSP_STAT, ABECS.STAT.values()[_getInt(array, 3, 3)].name());

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

        try {
            JSONObject buffer = new JSONObject(string);

            stream[0].write(buffer.getString(ABECS.CMD_ID).getBytes(UTF_8));

            for (Iterator<String> it = buffer.keys(); it.hasNext(); ) {
                String entry = it.next();

                switch (entry) {
                    case ABECS.CMD_ID:
                        /* Nothing to do */
                        break;

                    default:
                        byte[] array = PinpadUtility.buildTLV(entry, buffer.getString(entry));

                        stream[1].write(array);

                        Log.h(TAG, array, array.length);

                        ByteUtility.clear(array);
                        break;
                }
            }

            byte[] CMD_DATA = stream[1].toByteArray();

            stream[0].write(String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8));
            stream[0].write(CMD_DATA);

            byte[] request = stream[0].toByteArray();

            return request;
        } finally {
            ByteUtility.clear(stream[0]);
            ByteUtility.clear(stream[1]);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        try {
            JSONObject buffer = new JSONObject(string);

            stream[0].write(buffer.getString(ABECS.RSP_ID).getBytes(UTF_8));

            for (Iterator<String> it = buffer.keys(); it.hasNext(); ) {
                String entry = it.next();

                switch (entry) {
                    case ABECS.RSP_ID:
                        /* Nothing to do */
                        break;

                    case ABECS.RSP_STAT:
                        int RSP_STAT = ABECS.STAT.valueOf(buffer.getString(entry)).ordinal();

                        stream[0].write(String.format(US, "%03d", RSP_STAT).getBytes(UTF_8));
                        break;

                    default:
                        byte[] array = PinpadUtility.buildTLV(entry, buffer.getString(entry));

                        stream[1].write(array);

                        Log.h(TAG, array, array.length);

                        ByteUtility.clear(array);
                        break;
                }
            }

            byte[] RSP_DATA = stream[1].toByteArray();

            stream[0].write(String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8));
            stream[0].write(RSP_DATA);

            byte[] response = stream[0].toByteArray();

            return response;
        } finally {
            ByteUtility.clear(stream[0]);
            ByteUtility.clear(stream[1]);
        }
    }
}
