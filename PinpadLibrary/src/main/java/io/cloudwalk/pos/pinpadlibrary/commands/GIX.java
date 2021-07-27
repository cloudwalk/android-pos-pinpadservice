package io.cloudwalk.pos.pinpadlibrary.commands;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class GIX {
    private static final String TAG = GIX.class.getSimpleName();

    private GIX() {
        Log.d(TAG, "GIX");

        /* Nothing to do */
    }

    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] RSP_LEN1     = new byte[3];
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

        int cursor    = 0;
        int threshold = 0;

        do {
            byte[] T = new byte[4];
            byte[] L = new byte[4];

            System.arraycopy(RSP_DATA, cursor, T, 2, 2); cursor += 2;

            int tag = ByteBuffer.wrap(T).getInt();

            System.arraycopy(RSP_DATA, cursor, L, 2, 2); cursor += 2;

            threshold = ByteBuffer.wrap(L).getInt();

            byte[] V = new byte[threshold];

            System.arraycopy(RSP_DATA, cursor, V, 0, threshold);

            switch (tag) {
                case 0x8001: output.putString(ABECS.PP_SERNUM,      new String(V)); break;
                case 0x8002: output.putString(ABECS.PP_PARTNBR,     new String(V)); break;
                case 0x8003: output.putString(ABECS.PP_MODEL,       new String(V)); break;
                case 0x8004: output.putString(ABECS.PP_MNNAME,      new String(V)); break;
                case 0x8005: output.putString(ABECS.PP_CAPAB,       new String(V)); break;
                case 0x8006: output.putString(ABECS.PP_SOVER,       new String(V)); break;
                case 0x8007: output.putString(ABECS.PP_SPECVER,     new String(V)); break;
                case 0x8008: output.putString(ABECS.PP_MANVERS,     new String(V)); break;
                case 0x8009: output.putString(ABECS.PP_APPVERS,     new String(V)); break;
                case 0x800A: output.putString(ABECS.PP_GENVERS,     new String(V)); break;
                case 0x8010: output.putString(ABECS.PP_KRNLVER,     new String(V)); break;
                case 0x8011: output.putString(ABECS.PP_CTLSVER,     new String(V)); break;
                case 0x8012: output.putString(ABECS.PP_MCTLSVER,    new String(V)); break;
                case 0x8013: output.putString(ABECS.PP_VCTLSVER,    new String(V)); break;
                case 0x8014: output.putString(ABECS.PP_AECTLSVER,   new String(V)); break;
                case 0x8015: output.putString(ABECS.PP_DPCTLSVER,   new String(V)); break;
                case 0x8016: output.putString(ABECS.PP_PUREVER,     new String(V)); break;
                case 0x8032: output.putString(ABECS.PP_MKTDESP,     new String(V)); break;
                case 0x8033: output.putString(ABECS.PP_MKTDESD,     new String(V)); break;
                case 0x8035: output.putString(ABECS.PP_DKPTTDESP,   new String(V)); break;
                case 0x8036: output.putString(ABECS.PP_DKPTTDESD,   new String(V)); break;

                case 0x8020: // TODO: intercept response @PinpadService?
                    output.putLong(ABECS.PP_DSPTXTSZ, 0L);
                    break;

                case 0x8021: // TODO: intercept response @PinpadService?
                    output.putLong(ABECS.PP_DSPGRSZ, 0L);
                    break;

                case 0x805A:
                    output.putString(ABECS.PP_BIGRAND, DataUtility.byteToHexString(V));
                    break;

                case 0x8062: // TODO: intercept response @PinpadService?
                    output.putString(ABECS.PP_TLRMEM, "00000000");
                    break;

                default:
                    if (tag >= 0x9100 && tag <= 0x9163) {
                        Log.d(TAG, "PP_KSNTDESPnn"); Log.h(TAG, V, V.length);

                        // TODO: PP_KSNTDESPnn
                    }

                    if (tag >= 0x9200 && tag <= 0x9263) {
                        Log.d(TAG, "PP_KSNTDESDnn"); Log.h(TAG, V, V.length);

                        // TODO: PP_KSNTDESDnn
                    }

                    if (tag >= 0x9300 && tag <= 0x9363) {
                        Log.d(TAG, "PP_TABVERnn");   Log.h(TAG, V, V.length);

                        // TODO: PP_TABVERnn
                    }

                    /* Nothing to do */
                    break;
            }
        } while ((cursor += threshold) < RSP_DATA.length);

        return output;
    }

    public static byte[] buildDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        byte[] CMD_LEN1     = new byte[3];
        byte[] CMD_DATA     = stream[1].toByteArray();

        CMD_LEN1 = String.format(US, "%03d", CMD_DATA.length).getBytes();

        stream[0].write(CMD_ID.getBytes(UTF_8));
        stream[0].write(CMD_LEN1);
        stream[0].write(CMD_DATA);

        return stream[0].toByteArray();
    }
}
