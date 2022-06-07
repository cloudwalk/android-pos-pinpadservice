package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

public class GCX {
    private static final String
            TAG = GCX.class.getSimpleName();

    private GCX() {
        Log.d(TAG, "GCX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_TRNTYPE  = input.getString(ABECS.SPE_TRNTYPE);
        String SPE_ACQREF   = input.getString(ABECS.SPE_ACQREF);
        String SPE_APPTYPE  = input.getString(ABECS.SPE_APPTYPE);
        String SPE_AIDLIST  = input.getString(ABECS.SPE_AIDLIST);
        String SPE_AMOUNT   = input.getString(ABECS.SPE_AMOUNT);
        String SPE_CASHBACK = input.getString(ABECS.SPE_CASHBACK);
        String SPE_TRNCURR  = input.getString(ABECS.SPE_TRNCURR);
        String SPE_TRNDATE  = input.getString(ABECS.SPE_TRNDATE);
        String SPE_TRNTIME  = input.getString(ABECS.SPE_TRNTIME);
        String SPE_GCXOPT   = input.getString(ABECS.SPE_GCXOPT);
        String SPE_PANMASK  = input.getString(ABECS.SPE_PANMASK);
        String SPE_EMVDATA  = input.getString(ABECS.SPE_EMVDATA);
        String SPE_TAGLIST  = input.getString(ABECS.SPE_TAGLIST);
        String SPE_TIMEOUT  = input.getString(ABECS.SPE_TIMEOUT);
        String SPE_DSPMSG   = input.getString(ABECS.SPE_DSPMSG);

        if (SPE_TRNTYPE  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.B, "0021", SPE_TRNTYPE));
        }

        if (SPE_ACQREF   != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0010", SPE_ACQREF));
        }

        if (SPE_APPTYPE  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0011", SPE_APPTYPE));
        }

        if (SPE_AIDLIST  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.A, "0012", SPE_AIDLIST));
        }

        if (SPE_AMOUNT   != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0013", SPE_AMOUNT));
        }

        if (SPE_CASHBACK != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0014", SPE_CASHBACK));
        }

        if (SPE_TRNCURR  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0022", SPE_TRNCURR));
        }

        if (SPE_TRNDATE  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0015", SPE_TRNDATE));
        }

        if (SPE_TRNTIME  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0016", SPE_TRNTIME));
        }

        if (SPE_GCXOPT   != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0017", SPE_GCXOPT));
        }

        if (SPE_PANMASK  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.N, "0023", SPE_PANMASK));
        }

        if (SPE_EMVDATA  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.B, "0005", SPE_EMVDATA));
        }

        if (SPE_TAGLIST  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.B, "0004", SPE_TAGLIST));
        }

        if (SPE_TIMEOUT  != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.X, "000C", SPE_TIMEOUT));
        }

        if (SPE_DSPMSG   != null) {
            stream[1].write(PinpadUtility.buildTLV(ABECS.TYPE.S, "001B", SPE_DSPMSG));
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
