package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

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

    public static JSONObject parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        JSONObject response = new JSONObject();

        String RSP_ID = new String(input, 0, 3);
        ABECS.STAT RSP_STAT = ABECS.STAT.values()[getInt(input, 3, 3)];

        response.put(ABECS.RSP_ID, RSP_ID);
        response.put(ABECS.RSP_STAT, RSP_STAT.name());

        if (length >= 10) {
            Log.h(TAG, input, input.length);

            JSONObject RSP_DATA = PinpadUtility.parseTLV(input, input.length, 9);

            for (Iterator<String> it = RSP_DATA.keys(); it.hasNext(); ) {
                String entry = it.next();
                response.put(entry, RSP_DATA.getString(entry));
            }
        }

        return response;
    }

    public static byte[] buildRequestDataPacket(JSONObject input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
        };

        try {
            String CMD_ID = input.has(ABECS.CMD_ID) ? input.getString(ABECS.CMD_ID) : "UNKNOWN";

            stream[0].write(CMD_ID.getBytes(UTF_8));

            for (Iterator<String> it = input.keys(); it.hasNext(); ) {
                String entry = it.next();

                switch (entry) {
                    case ABECS.CMD_ID:
                        /* Nothing to do */
                        break;

                    default:
                        stream[1].write(PinpadUtility.buildTLV(entry, input.getString(entry)));
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

    public static int getInt(byte[] input, int length, int offset) {
        Log.d(TAG, "getInt::input.length [" + input.length + "] length [" + length + "]");

        length = Math.max(length, 0);
        length = Math.min(length, input.length);

        offset = Math.max(offset, 0);

        int response = 0;

        for (int i = length - 1, j = offset; i >= offset; i--, j++) {
            if (input[j] < 0x30 || input[j] > 0x39) {
                String message = String.format(US, "getInt::input[%d] [%02X]", j, input[j]);

                throw new IllegalArgumentException(message);
            }

            response += (input[j] - 0x30) * ((i > 0) ? (Math.pow(10, i)) : 1);
        }

        return response;
    }
}
