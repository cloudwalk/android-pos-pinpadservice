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
    private static final String
            TAG = GIX.class.getSimpleName();

    private static final byte[]
            SPE_IDLIST = new byte[] { 0x00, 0x01 };

    private GIX() {
        Log.d(TAG, "GIX");

        /* Nothing to do */
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] RSP_ID       = new byte[3];
        byte[] RSP_STAT     = new byte[3];
        byte[] RSP_LEN1     = new byte[3];
        byte[] RSP_DATA     = null;

        System.arraycopy(input, 0, RSP_ID,   0, 3);
        System.arraycopy(input, 3, RSP_STAT, 0, 3);

        ABECS.STAT STAT = ABECS.STAT.values()[DataUtility.byteArrayToInt(RSP_STAT, RSP_STAT.length)];

        Bundle output = new Bundle();

        output.putString      (ABECS.RSP_ID,   new String(RSP_ID));
        output.putSerializable(ABECS.RSP_STAT, STAT);

        if (STAT != ABECS.STAT.ST_OK) {
            return output;
        }

        System.arraycopy(input, 6, RSP_LEN1, 0, 3);

        RSP_DATA = new byte[DataUtility.byteArrayToInt(RSP_LEN1, RSP_LEN1.length)];

        System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

        int cursor    = 0;
        int threshold = 0;

        while ((cursor += threshold) < RSP_DATA.length) {
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
                    output.putString(ABECS.PP_DSPTXTSZ, "0000");
                    break;

                case 0x8021: // TODO: intercept response @PinpadService?
                case 0x8062: // TODO: 0x8020, 0x8021 and 0x8062 are all coming back as { 0x00, 0x00 ... }
                    output.putString((tag != 0x8062) ? ABECS.PP_DSPGRSZ : ABECS.PP_TLRMEM, "00000000");
                    break;

                case 0x805A:
                    output.putString(ABECS.PP_BIGRAND, DataUtility.byteToHexString(V));
                    break;

                default:
                    if (tag >= 0x9100 && tag <= 0x9163) {
                        String key = ABECS.PP_KSNTDESPnn.replace("nn", String.format(US, "%02d", (tag - 0x9100)));

                        output.putString(key, DataUtility.byteToHexString(V));
                        continue;
                    }

                    if (tag >= 0x9200 && tag <= 0x9263) {
                        String key = ABECS.PP_KSNTDESDnn.replace("nn", String.format(US, "%02d", (tag - 0x9200)));

                        output.putString(key, DataUtility.byteToHexString(V));
                        continue;
                    }

                    if (tag >= 0x9300 && tag <= 0x9363) {
                        String key = ABECS.PP_TABVERnn  .replace("nn", String.format(US, "%02d", (tag - 0x9300)));

                        output.putString(key, new String(V));
                        continue;
                    }

                    /* Nothing to do */
                    break;
            }
        }

        return output;
    }

    public static byte[] buildRequestDataPacket(Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

        String CMD_ID       = input.getString(ABECS.CMD_ID);
        String SPE_IDLIST   = input.getString(ABECS.SPE_IDLIST);

        if (SPE_IDLIST != null) {
            ByteBuffer buffer = ByteBuffer.allocate(2);

            SPE_IDLIST = SPE_IDLIST.length() > 128 ? SPE_IDLIST.substring(0, 128) : SPE_IDLIST;

            byte[] T = GIX.SPE_IDLIST;
            byte[] L = buffer.putShort((short) (SPE_IDLIST.length() / 2)).array();
            byte[] V = DataUtility.hexStringToByteArray(SPE_IDLIST);

            stream[1].write(T);
            stream[1].write(L);
            stream[1].write(V);
        }

        byte[] CMD_DATA = stream[1].toByteArray();

        return DataUtility.concatByteArray(CMD_ID.getBytes(UTF_8), String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8), CMD_DATA);
    }
}
