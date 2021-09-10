package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class GOX {
    private static final String
            TAG = GOX.class.getSimpleName();

    private GOX() {
        Log.d(TAG, "GCX");

        /* Nothing to do */
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        return PinpadUtility.CMD.parseResponseDataPacket(input, length);
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_ACQREF   = input.getString(ABECS.SPE_ACQREF);
        String SPE_TRNTYPE  = input.getString(ABECS.SPE_TRNTYPE);
        String SPE_AMOUNT   = input.getString(ABECS.SPE_AMOUNT);
        String SPE_CASHBACK = input.getString(ABECS.SPE_CASHBACK);
        String SPE_TRNCURR  = input.getString(ABECS.SPE_TRNCURR);
        String SPE_GOXOPT   = input.getString(ABECS.SPE_GOXOPT);
        String SPE_MTHDPIN  = input.getString(ABECS.SPE_MTHDPIN);
        String SPE_KEYIDX   = input.getString(ABECS.SPE_KEYIDX);
        String SPE_WKENC    = input.getString(ABECS.SPE_WKENC);
        String SPE_DSPMSG   = input.getString(ABECS.SPE_DSPMSG);
        String SPE_TRMPAR   = input.getString(ABECS.SPE_TRMPAR);
        String SPE_EMVDATA  = input.getString(ABECS.SPE_EMVDATA);
        String SPE_TAGLIST  = input.getString(ABECS.SPE_TAGLIST);
        String SPE_TIMEOUT  = input.getString(ABECS.SPE_TIMEOUT);

        if (SPE_ACQREF != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0010", SPE_ACQREF));
        }

        if (SPE_TRNTYPE != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "0021", SPE_TRNTYPE));
        }

        if (SPE_AMOUNT != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0013", SPE_AMOUNT));
        }

        if (SPE_CASHBACK != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0014", SPE_CASHBACK));
        }

        if (SPE_TRNCURR != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0022", SPE_TRNCURR));
        }

        if (SPE_GOXOPT != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0018", SPE_GOXOPT));
        }

        if (SPE_MTHDPIN != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0002", SPE_MTHDPIN));
        }

        if (SPE_KEYIDX != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "0009", SPE_KEYIDX));
        }

        if (SPE_WKENC != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.N, "000A", SPE_WKENC));
        }

        if (SPE_DSPMSG != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.S, "001B", SPE_DSPMSG));
        }

        if (SPE_TRMPAR != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "001A", SPE_TRMPAR));
        }

        if (SPE_EMVDATA != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "0005", SPE_EMVDATA));
        }

        if (SPE_TAGLIST != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.B, "0004", SPE_TAGLIST));
        }

        if (SPE_TIMEOUT != null) {
            stream[1].write(PinpadUtility.buildRequestTLV(ABECS.TYPE.X, "000C", SPE_TIMEOUT));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
