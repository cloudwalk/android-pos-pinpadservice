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

public class CHP {
    private static final String
            TAG = CHP.class.getSimpleName();

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        JSONObject request = new JSONObject();

        request.put(ABECS.CMD_ID,   new String(array, 0, 3));
        request.put(ABECS.CHP_SLOT, new String(array, 6, 1));
        request.put(ABECS.CHP_OPER, new String(array, 7, 1));

        int CHP_CMDLEN = CMD.parseInt(array, 8, 3);

        if (CHP_CMDLEN > 0) {
            request.put(ABECS.CHP_CMD,     new String(array, 11, CHP_CMDLEN));
            request.put(ABECS.CHP_PINFMT,  new String(array, CHP_CMDLEN + 11,  1));
            request.put(ABECS.CHP_PINMSG,  new String(array, CHP_CMDLEN + 12, 32));
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
                    int CHP_RSPLEN = CMD.parseInt(array, 9, 3);

                    response[0].put(ABECS.CHP_RSP, new String(array, 12, CHP_RSPLEN));
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

        byte[] CMD_ID     = null;
        byte[] CHP_SLOT   = null;           byte[] CHP_OPER   = null;
        byte[] CHP_CMDLEN = null;           byte[] CHP_CMD    = null;
        byte[] CHP_PINFMT = null;           byte[] CHP_PINMSG = null;

        try {
            JSONObject request = new JSONObject(string);

            CMD_ID   = request.getString(ABECS.CMD_ID)  .getBytes(UTF_8);
            CHP_SLOT = request.getString(ABECS.CHP_SLOT).getBytes(UTF_8);
            CHP_OPER = request.getString(ABECS.CHP_OPER).getBytes(UTF_8);

            stream[0].write(CMD_ID);
            stream[1].write(CHP_SLOT);
            stream[1].write(CHP_OPER);

            switch (CHP_OPER[0]) {
                case (byte) 0x30:
                case (byte) 0x31:
                    CHP_CMDLEN = String.format(US, "%03d", 0).getBytes(UTF_8);

                    stream[1].write(CHP_CMDLEN);
                    break;

                case (byte) 0x32:
                case (byte) 0x33:
                    CHP_CMD = request.getString(ABECS.CHP_CMD).getBytes(UTF_8);

                    CHP_CMDLEN = String.format(US, "%03d", CHP_CMD.length / 2).getBytes(UTF_8);

                    stream[1].write(CHP_CMDLEN);
                    stream[1].write(CHP_CMD);

                    if (CHP_OPER[0] != (byte) 0x32) {
                        CHP_PINFMT = request.getString(ABECS.CHP_PINFMT).getBytes(UTF_8);
                        CHP_PINMSG = request.getString(ABECS.CHP_PINMSG).getBytes(UTF_8);

                        stream[1].write(CHP_PINFMT);
                        stream[1].write(CHP_PINMSG);
                    }
                    break;

                default:
                    /* Nothing to do */
                    break;
            }

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

            ByteUtility.clear(CMD_ID, CHP_SLOT, CHP_OPER, CHP_CMDLEN, CHP_CMD, CHP_PINFMT, CHP_PINMSG);
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
        byte[] RSP_LEN1   = null;
        byte[] CHP_RSPLEN = null;
        byte[] CHP_RSP    = null;
        byte[] RSP_DATA   = null;

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

                    case ABECS.CHP_RSP:
                        CHP_RSP = json.getString(entry).getBytes(UTF_8);

                        CHP_RSPLEN = String.format(US, "%03d", CHP_RSP.length / 2).getBytes(UTF_8);

                        stream[1].write(CHP_RSPLEN);
                        stream[1].write(CHP_RSP);
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

            ByteUtility.clear(RSP_ID, RSP_LEN1, CHP_RSPLEN, CHP_RSP, RSP_DATA);
        }
    }
}
