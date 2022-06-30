package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;

public class TLR {
    private static final String
            TAG = TLR.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID,     new String(array, 0,  3));
        request.put(ABECS.TLI_ACQIDX, new String(array, 6,  2));
        request.put(ABECS.TLI_TABVER, new String(array, 8 ,10));

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

        byte[] CMD_ID   = null;
        byte[] TLR_NREC = null;
        byte[] TLR_DATA = null;

        try {
            JSONObject request = new JSONObject(string);

            CMD_ID   = request.getString(ABECS.CMD_ID)  .getBytes(UTF_8);
            TLR_NREC = request.getString(ABECS.TLR_NREC).getBytes(UTF_8);
            TLR_DATA = request.getString(ABECS.TLR_DATA).getBytes(UTF_8);

            stream[0].write(CMD_ID);
            stream[1].write(TLR_NREC);
            stream[1].write(TLR_DATA);

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

            ByteUtility.clear(CMD_ID, TLR_NREC, TLR_DATA);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        return CMD.buildResponseDataPacket(string);
    }
}
