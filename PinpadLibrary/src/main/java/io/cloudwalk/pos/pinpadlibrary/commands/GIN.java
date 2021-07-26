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

    private static final Queue<Long> sGIN_ACQIDX = new LinkedList<>();

    private static final Semaphore sSemaphore = new Semaphore(1, true);

    private GIN() {
        Log.d(TAG, "GIN");

        /* Nothing to do */
    }

    private static long getGIN_ACQIDX() {
        long output = 0;

        sSemaphore.acquireUninterruptibly();

        try {
            output = sGIN_ACQIDX.remove();
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sSemaphore.release();

        return output;
    }

    private static void setGIN_ACQIDX(long input) {
        sSemaphore.acquireUninterruptibly();

        try {
            sGIN_ACQIDX.add(input);
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

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,    new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT,  ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)]);

        if (output.getSerializable(ABECS.RSP_STAT) != ABECS.STAT.ST_OK) {
            return output;
        }

        byte[] RSP_LEN1     = new byte[3];

        byte[] GIN_MNAME    = null;     byte[] GIN_ACQNAM   = null;     byte[] GIN_CTLSVER   = null;
        byte[] GIN_MODEL    = null;     byte[] GIN_KRNLVER  = null;     byte[] GIN_MCTLSVER  = null;
        byte[] GIN_CTLSSUP  = null;     byte[] GIN_APPVERS  = null;     byte[] GIN_VCTLSVER  = null;
        byte[] GIN_SOVER    = null;     byte[] GIN_RUF1     = null;     byte[] GIN_RUF3      = null;
        byte[] GIN_SPECVER  = null;     byte[] GIN_RUF2     = null;     byte[] GIN_DUKPT     = null;
        byte[] GIN_MANVER   = null;
        byte[] GIN_SERNUM   = null;

        System.arraycopy(input, 6, RSP_LEN1,   0, 3);

        long GIN_ACQIDX = getGIN_ACQIDX();

        switch ((int) GIN_ACQIDX) {
            case 0:
                GIN_MNAME    = new byte[20];    GIN_MODEL    = new byte[19];
                GIN_CTLSSUP  = new byte[1];     GIN_SOVER    = new byte[20];
                GIN_SPECVER  = new byte[4];     GIN_MANVER   = new byte[16];
                GIN_SERNUM   = new byte[20];

                System.arraycopy(input,  9, GIN_MNAME,     0, 20);
                System.arraycopy(input, 29, GIN_MODEL,     0, 19);
                System.arraycopy(input, 48, GIN_CTLSSUP,   0,  1);
                System.arraycopy(input, 49, GIN_SOVER,     0, 20);
                System.arraycopy(input, 69, GIN_SPECVER,   0,  4);
                System.arraycopy(input, 73, GIN_MANVER,    0, 16);
                System.arraycopy(input, 89, GIN_SERNUM,    0, 20);
                break;

            case 2:
                GIN_ACQNAM   = new byte[8];     GIN_KRNLVER  = new byte[12];
                GIN_APPVERS  = new byte[13];    GIN_SPECVER  = new byte[4];
                GIN_RUF1     = new byte[3];     GIN_RUF2     = new byte[2];

                System.arraycopy(input,  9, GIN_ACQNAM,    0,  8);
                System.arraycopy(input, 17, GIN_KRNLVER,   0, 12);
                System.arraycopy(input, 29, GIN_APPVERS,   0, 13);
                System.arraycopy(input, 42, GIN_SPECVER,   0,  4);
                System.arraycopy(input, 46, GIN_RUF1,      0,  3);
                System.arraycopy(input, 49, GIN_RUF2,      0,  2);
                break;

            case 3: /* 2021-07-26: BCPP 001.19 from Verifone does not recognize "03" as an specific
                     * acquirer index */
                    // TODO: intercept @PinpadService?

                /*
                GIN_ACQNAM   = new byte[8];     GIN_KRNLVER  = new byte[12];
                GIN_CTLSVER  = new byte[4];     GIN_MCTLSVER = new byte[3];
                GIN_VCTLSVER = new byte[3];     GIN_APPVERS  = new byte[13];
                GIN_SPECVER  = new byte[4];     GIN_RUF3     = new byte[2];
                GIN_DUKPT    = new byte[1];     GIN_RUF2     = new byte[2];

                System.arraycopy(input,  9, GIN_ACQNAM,    0,  8);
                System.arraycopy(input, 17, GIN_KRNLVER,   0, 12);
                System.arraycopy(input, 29, GIN_CTLSVER,   0,  4);
                System.arraycopy(input, 33, GIN_MCTLSVER,  0,  3);
                System.arraycopy(input, 36, GIN_VCTLSVER,  0,  3);
                System.arraycopy(input, 39, GIN_APPVERS,   0, 13);
                System.arraycopy(input, 52, GIN_SPECVER,   0,  4);
                System.arraycopy(input, 56, GIN_RUF3,      0,  2);
                System.arraycopy(input, 58, GIN_DUKPT,     0,  1);
                System.arraycopy(input, 59, GIN_RUF2,      0,  2);
                break;
                 */

            default:
                GIN_ACQNAM   = new byte[20];    GIN_APPVERS  = new byte[13];
                GIN_SPECVER  = new byte[4];     GIN_RUF1     = new byte[3];
                GIN_RUF2     = new byte[2];

                System.arraycopy(input,  9, GIN_ACQNAM,    0, 20);
                System.arraycopy(input, 29, GIN_APPVERS,   0, 13);
                System.arraycopy(input, 42, GIN_SPECVER,   0,  4);
                System.arraycopy(input, 46, GIN_RUF1,      0,  3);
                System.arraycopy(input, 49, GIN_RUF2,      0,  2);

                break;
        }

        output.putLong  (ABECS.GIN_ACQIDX,   GIN_ACQIDX);

        output.putString(ABECS.GIN_MNAME,    (GIN_MNAME    != null) ? new String(GIN_MNAME)    : null);
        output.putString(ABECS.GIN_MODEL,    (GIN_MODEL    != null) ? new String(GIN_MODEL)    : null);
        output.putString(ABECS.GIN_CTLSSUP,  (GIN_CTLSSUP  != null) ? new String(GIN_CTLSSUP)  : null);
        output.putString(ABECS.GIN_SOVER,    (GIN_SOVER    != null) ? new String(GIN_SOVER)    : null);
        output.putString(ABECS.GIN_SPECVER,  (GIN_SPECVER  != null) ? "2.12"                   : null);
        output.putString(ABECS.GIN_MANVER,   (GIN_MANVER   != null) ? new String(GIN_MANVER)   : null);
        output.putString(ABECS.GIN_SERNUM,   (GIN_SERNUM   != null) ? new String(GIN_SERNUM)   : null);
        output.putString(ABECS.GIN_ACQNAM,   (GIN_ACQNAM   != null) ? String.format(US, (GIN_ACQIDX != 2) ? "%-20.20s" : "%-8.8s", "ABECS") : null);
        output.putString(ABECS.GIN_KRNLVER,  (GIN_KRNLVER  != null) ? "            "           : null);
        output.putString(ABECS.GIN_APPVERS,  (GIN_APPVERS  != null) ? new String(GIN_APPVERS)  : null);
        output.putString(ABECS.GIN_RUF1,     (GIN_RUF1     != null) ? ""                       : null);
        output.putString(ABECS.GIN_RUF2,     (GIN_RUF2     != null) ? ""                       : null);
        output.putString(ABECS.GIN_CTLSVER,  (GIN_CTLSVER  != null) ? new String(GIN_CTLSVER)  : null);
        output.putString(ABECS.GIN_MCTLSVER, (GIN_MCTLSVER != null) ? new String(GIN_MCTLSVER) : null);
        output.putString(ABECS.GIN_VCTLSVER, (GIN_VCTLSVER != null) ? new String(GIN_VCTLSVER) : null);
        output.putString(ABECS.GIN_DUKPT,    (GIN_DUKPT    != null) ? new String(GIN_DUKPT)    : null);
        output.putString(ABECS.GIN_RUF3,     (GIN_RUF3     != null) ? ""                       : null);

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
