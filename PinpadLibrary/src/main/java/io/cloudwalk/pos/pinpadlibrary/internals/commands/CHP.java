package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
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

        Bundle output = new Bundle();

        byte[] CMD_ID       = new byte[3];
        byte[] CHP_SLOT     = new byte[1];
        byte[] CHP_OPER     = new byte[1];
        byte[] CHP_CMDLEN   = new byte[3];
        byte[] CHP_CMD      = null;
        byte[] CHP_PINFMT   = new byte[1];
        byte[] CHP_PINMSG   = new byte[32];

        System.arraycopy(input, 0, CMD_ID,     0, 3);

        output.putString(ABECS.CMD_ID,   new String(CMD_ID));

        System.arraycopy(input, 6, CHP_SLOT,   0, 1);
        System.arraycopy(input, 7, CHP_OPER,   0, 1);
        System.arraycopy(input, 8, CHP_CMDLEN, 0, 3);

        CHP_CMD = new byte[DataUtility.getIntFromByteArray(CHP_CMDLEN, CHP_CMDLEN.length)];

        output.putString(ABECS.CHP_SLOT, new String(CHP_SLOT));
        output.putString(ABECS.CHP_OPER, new String(CHP_OPER));

        if (CHP_CMD.length < 1) {
            return output;
        }

        System.arraycopy(input, 11, CHP_CMD, 0, CHP_CMD.length);

        output.putString(ABECS.CHP_CMD,  DataUtility.getHexStringFromByteArray(CHP_CMD));

        if (output.getString(ABECS.CHP_OPER).equals("3")) {
            System.arraycopy(input, CHP_CMD.length + 11, CHP_PINFMT, 0, 1);
            System.arraycopy(input, CHP_CMD.length + 12, CHP_PINMSG, 0, 32);

            output.putString(ABECS.CHP_PINFMT, new String(CHP_PINFMT));
            output.putString(ABECS.CHP_PINMSG, new String(CHP_PINMSG));
        }

        return output;
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

        ABECS.STAT status   = ABECS.STAT.values()[DataUtility.getIntFromByteArray(RSP_STAT, RSP_STAT.length)];
            Bundle output   = new Bundle();

        output.putString      (ABECS.RSP_ID, new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT, status);

        if (status != ABECS.STAT.ST_OK) return output;

        System.arraycopy(input, 9, CHP_RSPLEN, 0, 3);

        int i = DataUtility.getIntFromByteArray(CHP_RSPLEN, 3) * 2;

        System.arraycopy(input, 12, CHP_RSP,   0, i);

        output.putString(ABECS.CHP_RSP, new String(CHP_RSP).substring(0, i));

        return output;
    }

    public static byte[] buildRequestDataPacket(Bundle input)
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
            stream[1].write(DataUtility.getByteArrayFromHexString(CHP_CMD));
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
}
