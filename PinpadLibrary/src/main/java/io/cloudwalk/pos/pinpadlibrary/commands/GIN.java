package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class GIN {
    private static final String TAG = GIN.class.getSimpleName();

    private static final Queue<Long> sQueue = new LinkedList<>();

    private static final Semaphore sSemaphore = new Semaphore(1, true);

    private GIN() {
        Log.d(TAG, "GIN");

        /* Nothing to do */
    }

    private static long getGIN_ACQIDX() {
        long output = 0;

        sSemaphore.acquireUninterruptibly();

        try {
            output = sQueue.remove();
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sSemaphore.release();

        return output;
    }

    private static void setGIN_ACQIDX(long input) {
        sSemaphore.acquireUninterruptibly();

        try {
            sQueue.add(input);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sSemaphore.release();
    }

    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] RSP_LEN1     = new byte[3];
        long   GIN_ACQIDX   = getGIN_ACQIDX();
        byte[] RSP_DATA     = null;

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);
        System.arraycopy(input, 6, RSP_LEN1, 0, 3);

        ABECS.STAT STAT = ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)];

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,    new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT,  STAT);

        if (STAT != ABECS.STAT.ST_OK) return output;

        RSP_DATA = new byte[DataUtility.byteArrayToInt(RSP_LEN1, RSP_LEN1.length)];

        System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

        String response = new String(RSP_DATA);

        switch ((int) GIN_ACQIDX) {
            case 0:
                output.putString(ABECS.GIN_MNAME,       response.substring( 0, 20));
                output.putString(ABECS.GIN_MODEL,       response.substring(20, 39));
                output.putString(ABECS.GIN_CTLSSUP,     response.substring(39, 40));
                output.putString(ABECS.GIN_SOVER,       response.substring(40, 60));
                output.putString(ABECS.GIN_SPECVER,     response.substring(60, 64));
                output.putString(ABECS.GIN_MANVER,      response.substring(64, 70));
                output.putString(ABECS.GIN_SERNUM,      response.substring(70, 90));
                break;

            case 2:
                output.putString(ABECS.GIN_ACQNAM,      response.substring( 0,  8));
                output.putString(ABECS.GIN_KRNLVER,     response.substring( 8, 20));
                output.putString(ABECS.GIN_APPVERS,     response.substring(20, 33));
                output.putString(ABECS.GIN_SPECVER,     response.substring(33, 37));
                output.putString(ABECS.GIN_RUF1,        response.substring(37, 40));
                output.putString(ABECS.GIN_RUF2,        response.substring(40, 42));
                break;

            case 3: /* 2021-07-26: BCPP 001.19 from Verifone does not recognize "03" as an specific
                     * acquirer index */
                // TODO: intercept @PinpadService?

                /*
                output.putString(ABECS.GIN_ACQNAM,      response.substring( 0,  6));
                output.putString(ABECS.GIN_KRNLVER,     response.substring( 6, 10));
                output.putString(ABECS.GIN_CTLSVER,     response.substring(10, 14));
                output.putString(ABECS.GIN_MCTLSVER,    response.substring(14, 17));
                output.putString(ABECS.GIN_VCTLSVER,    response.substring(17, 20));
                output.putString(ABECS.GIN_APPVERS,     response.substring(20, 33));
                output.putString(ABECS.GIN_SPECVER,     response.substring(33, 37));
                output.putString(ABECS.GIN_RUF3,        response.substring(37, 39));
                output.putString(ABECS.GIN_DUKPT,       response.substring(39, 40));
                output.putString(ABECS.GIN_RUF2,        response.substring(40, 42));
                */

                output.putSerializable(ABECS.RSP_STAT,  ABECS.STAT.ST_INTERR);
                break;

            default:
                output.putString(ABECS.GIN_ACQNAM,      response.substring( 0, 20));
                output.putString(ABECS.GIN_APPVERS,     response.substring(20, 33));
                output.putString(ABECS.GIN_SPECVER,     response.substring(33, 37));
                output.putString(ABECS.GIN_RUF1,        response.substring(37, 40));
                output.putString(ABECS.GIN_RUF2,        response.substring(40, 42));
                break;
        }

        output.putLong(ABECS.GIN_ACQIDX, GIN_ACQIDX);

        return output;
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        byte[] CMD_LEN1     = new byte[3];
        long   GIN_ACQIDX   = input.getLong  (ABECS.GIN_ACQIDX, 0);

        setGIN_ACQIDX(GIN_ACQIDX);

        stream[1].write(String.format(US, "%02d", GIN_ACQIDX).getBytes());

        byte[] CMD_DATA = stream[1].toByteArray();

        CMD_LEN1 = String.format(US, "%03d", CMD_DATA.length).getBytes();

        stream[0].write(CMD_ID.getBytes(UTF_8));
        stream[0].write(CMD_LEN1);
        stream[0].write(CMD_DATA);

        return stream[0].toByteArray();
    }
}
