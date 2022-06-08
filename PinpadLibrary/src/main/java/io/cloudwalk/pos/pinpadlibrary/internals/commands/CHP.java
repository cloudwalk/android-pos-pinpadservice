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

public class CHP {
    private static final String
            TAG = CHP.class.getSimpleName();

    private CHP() {
        Log.d(TAG, "CHP");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        Bundle response = new Bundle();

        byte[] CMD_ID       = new byte[3];
        byte[] CHP_SLOT     = new byte[1];
        byte[] CHP_OPER     = new byte[1];
        byte[] CHP_CMDLEN   = new byte[3];
        byte[] CHP_CMD      = null;
        byte[] CHP_PINFMT   = new byte[1];
        byte[] CHP_PINMSG   = new byte[32];

        System.arraycopy(input, 0, CMD_ID,     0, 3);

        response.putString(ABECS.CMD_ID, new String(CMD_ID));

        System.arraycopy(input, 6, CHP_SLOT,   0, 1);
        System.arraycopy(input, 7, CHP_OPER,   0, 1);
        System.arraycopy(input, 8, CHP_CMDLEN, 0, 3);

        CHP_CMD = new byte[PinpadUtility.getIntFromDigitsArray(CHP_CMDLEN, CHP_CMDLEN.length) * 2];

        response.putString(ABECS.CHP_SLOT, new String(CHP_SLOT));
        response.putString(ABECS.CHP_OPER, new String(CHP_OPER));

        if (CHP_CMD.length < 1) {
            return response;
        }

        System.arraycopy(input, 11, CHP_CMD, 0, CHP_CMD.length);

        response.putString(ABECS.CHP_CMD, new String(CHP_CMD));

        if (!response.getString(ABECS.CHP_OPER).equals("3")) {
            return response;
        }

        System.arraycopy(input, CHP_CMD.length + 11, CHP_PINFMT, 0, 1);
        System.arraycopy(input, CHP_CMD.length + 12, CHP_PINMSG, 0, 32);

        response.putString(ABECS.CHP_PINFMT, new String(CHP_PINFMT));
        response.putString(ABECS.CHP_PINMSG, new String(CHP_PINMSG));

        return response;
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] CHP_RSPLEN   = new byte[3];
        byte[] CHP_RSP      = new byte[514];

        System.arraycopy(input,  0, RSP_ID,     0,  3);
        System.arraycopy(input,  3, RSP_STAT,   0,  3);

        Bundle response = new Bundle();

        response.putString(ABECS.RSP_ID, new String(RSP_ID));
        response.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[PinpadUtility.getIntFromDigitsArray(RSP_STAT, RSP_STAT.length)]);

        if (ABECS.STAT.ST_OK != response.getSerializable(ABECS.RSP_STAT)) {
            return response;
        }

        System.arraycopy(input, 9, CHP_RSPLEN, 0, 3);

        int i = PinpadUtility.getIntFromDigitsArray(CHP_RSPLEN, CHP_RSPLEN.length) * 2;

        System.arraycopy(input, 12, CHP_RSP,   0, i);

        response.putString(ABECS.CHP_RSP, new String(CHP_RSP, 0, i));

        return response;
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String CHP_SLOT     = input.getString(ABECS.CHP_SLOT);
        String CHP_OPER     = input.getString(ABECS.CHP_OPER);
        String CHP_CMD      = input.getString(ABECS.CHP_CMD);
        String CHP_PINFMT   = input.getString(ABECS.CHP_PINFMT);
        String CHP_PINMSG   = input.getString(ABECS.CHP_PINMSG);

        stream[1].write(CHP_SLOT.getBytes(UTF_8));
        stream[1].write(CHP_OPER.getBytes(UTF_8));

        byte[] CHP_CMDLEN   = String.format(US, "%03d", (CHP_CMD != null) ? (CHP_CMD.length() / 2) : 0).getBytes(UTF_8);

        stream[1].write(CHP_CMDLEN);

        if (CHP_CMD    != null) {
            stream[1].write(CHP_CMD   .getBytes(UTF_8));
        }

        if (CHP_PINFMT != null) {
            stream[1].write(CHP_PINFMT.getBytes(UTF_8));
        }

        if (CHP_PINMSG != null) {
            stream[1].write(CHP_PINMSG.getBytes(UTF_8));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket::input [" + input + "]");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        String RSP_ID       = null;
        int    RSP_STAT     = ABECS.STAT.ST_INTERR.ordinal();
        byte[] CHP_RSP      = null;

        for (String T : input.keySet()) {
            switch (T) {
                case ABECS.RSP_ID:
                    RSP_ID = input.getString(T);
                    break;

                case ABECS.RSP_STAT:
                    RSP_STAT = ((ABECS.STAT) input.getSerializable(T)).ordinal();
                    break;

                case ABECS.CHP_RSP:
                    CHP_RSP = input.getString(T).getBytes(UTF_8);
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

        stream.write(String.format(US, "%03d", CHP_RSP.length + 3).getBytes(UTF_8));
        stream.write(String.format(US, "%03d", CHP_RSP.length / 2).getBytes(UTF_8));
        stream.write(CHP_RSP);

        return stream.toByteArray();
    }
}
