package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class GTK {
    private static final String
            TAG = GTK.class.getSimpleName();

    private GTK() {
        Log.d(TAG, "GTK");

        /* Nothing to do */
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] RSP_LEN1     = new byte[3];
        byte[] RSP_DATA     = null;

        Bundle output = PinpadUtility.CMD.parseResponseDataPacket(input, length);

        switch ((ABECS.STAT) output.getSerializable(ABECS.RSP_STAT)) {
            case ST_OK:
                if (length > 6) {
                    System.arraycopy(input, 6, RSP_LEN1, 0, 3);

                    RSP_DATA = new byte[DataUtility.getIntFromByteArray(RSP_LEN1, RSP_LEN1.length)];

                    System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

                    output.putAll(PinpadUtility.parseResponseTLV(RSP_DATA, RSP_DATA.length));
                }

                /* no break */

            default:
                return output;
        }
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_TRACKS   = input.getString(ABECS.SPE_TRACKS);
        String SPE_MTHDDAT  = input.getString(ABECS.SPE_MTHDDAT);
        String SPE_IVCBC    = input.getString(ABECS.SPE_IVCBC);
        String SPE_OPNDIG   = input.getString(ABECS.SPE_OPNDIG);
        String SPE_KEYIDX   = input.getString(ABECS.SPE_KEYIDX);
        String SPE_WKENC    = input.getString(ABECS.SPE_WKENC);
        String SPE_PBKMOD   = input.getString(ABECS.SPE_PBKMOD);
        String SPE_PBKEXP   = input.getString(ABECS.SPE_PBKEXP);

        if (SPE_TRACKS != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0007", SPE_TRACKS));
        }

        if (SPE_MTHDDAT != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0003", SPE_MTHDDAT));
        }

        if (SPE_IVCBC != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "001D", SPE_IVCBC));
        }

        if (SPE_OPNDIG != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0008", SPE_OPNDIG));
        }

        if (SPE_KEYIDX != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0009", SPE_KEYIDX));
        }

        if (SPE_WKENC != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "000A", SPE_WKENC));
        }

        if (SPE_PBKMOD != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "0024", SPE_PBKMOD));
        }

        if (SPE_PBKEXP != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "0025", SPE_PBKEXP));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
