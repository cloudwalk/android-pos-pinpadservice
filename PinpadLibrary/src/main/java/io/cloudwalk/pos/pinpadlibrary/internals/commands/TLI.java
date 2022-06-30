package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;

public class TLI {
    private static final String
            TAG = TLI.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID,     new String(array, 0,  3));
        request.put(ABECS.TLI_ACQIDX, new String(array, 6,  2));
        request.put(ABECS.TLI_TABVER, new String(array, 8, 10));

        return request.toString();
    }

    public static String parseResponseDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        return CMD.parseResponseDataPacket(array, length);
    }

    public static byte[] buildRequestDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        byte[] CMD_ID     = null;
        byte[] TLI_ACQIDX = null;
        byte[] TLI_TABVER = null;

        try {
            JSONObject request = new JSONObject(string);

            CMD_ID     = request.getString(ABECS.CMD_ID)    .getBytes(UTF_8);
            TLI_ACQIDX = request.getString(ABECS.TLI_ACQIDX).getBytes(UTF_8);
            TLI_TABVER = request.getString(ABECS.TLI_TABVER).getBytes(UTF_8);

            stream[0].write(CMD_ID);
            stream[1].write(TLI_ACQIDX);
            stream[1].write(TLI_TABVER);

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

            ByteUtility.clear(CMD_ID, TLI_ACQIDX, TLI_TABVER);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        return CMD.buildRequestDataPacket(string);
    }
}
