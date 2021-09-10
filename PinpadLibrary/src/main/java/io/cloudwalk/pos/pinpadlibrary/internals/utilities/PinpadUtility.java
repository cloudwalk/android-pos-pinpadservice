package io.cloudwalk.pos.pinpadlibrary.internals.utilities;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CEX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CLX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.EBX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GCX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GED;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GIX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GOX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GTK;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.OPN;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.RMC;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLE;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLI;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLR;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

public class PinpadUtility {
    private static final String
            TAG = PinpadUtility.class.getSimpleName();

    public static class CMD {
        private CMD() {
            Log.d(TAG, "CMD");

            /* Nothing to do */
        }

        public static Bundle parseResponseDataPacket(byte[] input, int length)
                throws Exception {
            Log.d(TAG, "parseResponseDataPacket");

            byte[] RSP_ID       = new byte[3];
            byte[] RSP_STAT     = new byte[3];
            byte[] RSP_LEN1     = new byte[3];
            byte[] RSP_DATA     = null;

            System.arraycopy(input, 0, RSP_ID, 0, 3);
            System.arraycopy(input, 3, RSP_STAT, 0, 3);

            Bundle output = new Bundle();

            output.putString(ABECS.RSP_ID, new String(RSP_ID));
            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[DataUtility.getIntFromByteArray(RSP_STAT, RSP_STAT.length)]);

            switch ((ABECS.STAT) output.getSerializable(ABECS.RSP_STAT)) {
                case ST_OK:
                case ST_TABVERDIF:
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
    }

    private PinpadUtility() {
        Log.d(TAG, "PinpadUtility");

        /* Nothing to do */
    }

    private static byte[] wrapDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "wrapDataPacket");

        byte[] pkt = new byte[2044 + 4];

        pkt[0] = 0x16; /* PKTSTART */

        int threshold = Math.min(length, 2044 + 4);

        int j = 1;

        for (int i = 0; i < threshold; i++) {
            switch (input[i]) {
                case 0x13: /* DC3 */
                    pkt[j++] = 0x13;
                    pkt[j++] = 0x33;
                    break;

                case 0x16: /* SYN */
                    pkt[j++] = 0x13;
                    pkt[j++] = 0x36;
                    break;

                case 0x17: /* ETB */
                    pkt[j++] = 0x13;
                    pkt[j++] = 0x37;
                    break;

                default:
                    pkt[j++] = input[i];
                    break;
            }
        }

        pkt[j] = 0x17; /* PKTSTOP */

        byte[] crc = new byte[length + 1];

        System.arraycopy(input, 0, crc, 0, length);

        crc[length] = pkt[j];

        crc = DataUtility.CRC16_XMODEM(crc);

        System.arraycopy(crc, 0, pkt, j + 1, crc.length);

        pkt = Arrays.copyOf(pkt, j + 1 + crc.length);

        return pkt;
    }

    private static byte[] unwrapDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "unwrapDataPacket");

        byte[] pkt = new byte[2048 - 4];

        int threshold = Math.min(length, 2044 + 4);

        int j = 0;

        for (int i = 1; i < threshold; i++) {
            switch (input[i]) {
                case 0x16: /* PKTSTART */
                    continue;

                case 0x17: /* PKTSTOP  */
                    i = threshold;
                    continue;

                case 0x13:
                    switch (input[++i]) {
                        case 0x33: /* DC3 */
                            pkt[j++] = 0x13;
                            break;

                        case 0x36: /* SYN */
                            pkt[j++] = 0x16;
                            break;

                        case 0x37: /* ETB */
                            pkt[j++] = 0x17;
                            break;

                        default:
                            pkt[j++] = input[i];
                            break;
                    }
                    break;

                default:
                    pkt[j++] = input[i];
                    break;
            }
        }

        // TODO: validate CRC and throw exception

        byte[] output = new byte[j];

        System.arraycopy(pkt, 0, output, 0, j);

        return output;
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] response = unwrapDataPacket(input, length);

        String CMD_ID = String.format(US, "%c%c%c", response[0], response[1], response[2]);

        switch (CMD_ID) {
            case ABECS.OPN: return OPN.parseResponseDataPacket(response, response.length);
            case ABECS.GIX: return GIX.parseResponseDataPacket(response, response.length);
            case ABECS.CLX: return CLX.parseResponseDataPacket(response, response.length);

            case ABECS.CEX: return CEX.parseResponseDataPacket(response, response.length);
            case ABECS.EBX: return EBX.parseResponseDataPacket(response, response.length);
            case ABECS.GTK: return GTK.parseResponseDataPacket(response, response.length);
            case ABECS.RMC: return RMC.parseResponseDataPacket(response, response.length);

            case ABECS.TLI: return TLI.parseResponseDataPacket(response, response.length);
            case ABECS.TLR: return TLR.parseResponseDataPacket(response, response.length);
            case ABECS.TLE: return TLE.parseResponseDataPacket(response, response.length);

            case ABECS.GCX: return GCX.parseResponseDataPacket(response, response.length);
            case ABECS.GED: return GED.parseResponseDataPacket(response, response.length);
            case ABECS.GOX: return GOX.parseResponseDataPacket(response, response.length);

            default:
                /* Nothing to do */
                break;
        }

        throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
    }

    public static Bundle parseResponseTLV(byte[] stream, int length) {
        Bundle output = new Bundle();

        int cursor    = 0;
        int threshold = 0;

        while ((cursor += threshold) < length) {
            byte[] T = new byte[4];
            byte[] L = new byte[4];

            System.arraycopy(stream, cursor, T, 2, 2); cursor += 2;

            int tag = ByteBuffer.wrap(T).getInt();

            System.arraycopy(stream, cursor, L, 2, 2); cursor += 2;

            threshold = ByteBuffer.wrap(L).getInt();

            byte[] V = new byte[threshold];

            System.arraycopy(stream, cursor, V, 0, threshold);

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
                case 0x8040: output.putString(ABECS.PP_EVENT,       new String(V)); break;
                case 0x8041: output.putString(ABECS.PP_TRK1INC,     new String(V)); break;
                case 0x8042: output.putString(ABECS.PP_TRK2INC,     new String(V)); break;
                case 0x8043: output.putString(ABECS.PP_TRK3INC,     new String(V)); break;
                case 0x8044: output.putString(ABECS.PP_TRACK1,      new String(V)); break; // TODO: should have same format as PP_TRACK2 and PP_TRACK3
                case 0x804F: output.putString(ABECS.PP_CARDTYPE,    new String(V)); break;
                case 0x8050: output.putString(ABECS.PP_ICCSTAT,     new String(V)); break;
                case 0x8051: output.putString(ABECS.PP_AIDTABINFO,  new String(V)); break;
                case 0x8052: output.putString(ABECS.PP_PAN,         new String(V)); break;
                case 0x8053: output.putString(ABECS.PP_PANSEQNO,    new String(V)); break;
                case 0x8055: output.putString(ABECS.PP_CHNAME,      new String(V)); break;
                case 0x8056: output.putString(ABECS.PP_GOXRES,      new String(V)); break;
                case 0x805B: output.putString(ABECS.PP_LABEL,       new String(V)); break;
                case 0x805C: output.putString(ABECS.PP_ISSCNTRY,    new String(V)); break;
                case 0x805D: output.putString(ABECS.PP_CARDEXP,     new String(V)); break;
                case 0x8060: output.putString(ABECS.PP_DEVTYPE,     new String(V)); break;

                case 0x8020: // TODO: intercept response @PinpadService!?
                    output.putString(ABECS.PP_DSPTXTSZ, "0000");
                    break;

                case 0x8021: // TODO: intercept response @PinpadService!?
                case 0x8062: // TODO: 0x8020, 0x8021 and 0x8062 are all coming back as { 0x00, 0x00 ... }
                    output.putString((tag != 0x8062) ? ABECS.PP_DSPGRSZ : ABECS.PP_TLRMEM, "00000000");
                    break;

                case 0x8045: output.putString(ABECS.PP_TRACK2,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8046: output.putString(ABECS.PP_TRACK3,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8047: output.putString(ABECS.PP_TRK1KSN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8048: output.putString(ABECS.PP_TRK2KSN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8049: output.putString(ABECS.PP_TRK3KSN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804A: output.putString(ABECS.PP_ENCPAN,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804B: output.putString(ABECS.PP_ENCPANKSN,   DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804C: output.putString(ABECS.PP_KSN,         DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804E: output.putString(ABECS.PP_DATAOUT,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8054: output.putString(ABECS.PP_EMVDATA,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8057: output.putString(ABECS.PP_PINBLK,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8063: output.putString(ABECS.PP_ENCKRAND,    DataUtility.getHexStringFromByteArray(V)); break;

                case 0x805A:
                    output.putString(ABECS.PP_BIGRAND, DataUtility.getHexStringFromByteArray(V));
                    break;

                default:
                    if (tag >= 0x9100 && tag <= 0x9163) {
                        String key = ABECS.PP_KSNTDESPnn.replace("nn", String.format(US, "%02d", (tag - 0x9100)));

                        output.putString(key, DataUtility.getHexStringFromByteArray(V));
                        continue;
                    }

                    if (tag >= 0x9200 && tag <= 0x9263) {
                        String key = ABECS.PP_KSNTDESDnn.replace("nn", String.format(US, "%02d", (tag - 0x9200)));

                        output.putString(key, DataUtility.getHexStringFromByteArray(V));
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

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        byte[] request = null;

        String CMD_ID = input.getString(ABECS.CMD_ID, "UNKNOWN");

        switch (CMD_ID) {
            case ABECS.OPN: request = OPN.buildRequestDataPacket(input); break;
            case ABECS.GIX: request = GIX.buildRequestDataPacket(input); break;
            case ABECS.CLX: request = CLX.buildRequestDataPacket(input); break;

            case ABECS.CEX: request = CEX.buildRequestDataPacket(input); break;
            case ABECS.EBX: request = EBX.buildRequestDataPacket(input); break;
            case ABECS.GTK: request = GTK.buildRequestDataPacket(input); break;
            case ABECS.RMC: request = RMC.buildRequestDataPacket(input); break;

            case ABECS.TLI: request = TLI.buildRequestDataPacket(input); break;
            case ABECS.TLR: request = TLR.buildRequestDataPacket(input); break;
            case ABECS.TLE: request = TLE.buildRequestDataPacket(input); break;

            case ABECS.GCX: request = GCX.buildRequestDataPacket(input); break;
            case ABECS.GED: request = GED.buildRequestDataPacket(input); break;
            case ABECS.GOX: request = GOX.buildRequestDataPacket(input); break;

            default:
                /* Nothing to do */
                break;
        }

        if (request != null) {
            if (request.length <= 2048) {
                return wrapDataPacket(request, request.length);
            }

            throw new RuntimeException("CMD_ID [" + CMD_ID + "] packet exceeds maximum length (2048)");
        } else {
            throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
        }
    }

    public static byte[] buildRequestTLV(@NotNull ABECS.TYPE type, @NotNull String tag, @NotNull String value)
            throws Exception {
        Log.d(TAG, "buildRequestTLV");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        byte[] T = null;
        byte[] L = null;
        byte[] V = null;

        switch (type) {
            case A: case S: case N:
                T = DataUtility.getByteArrayFromHexString(tag);
                L = ByteBuffer.allocate(2).putShort((short) (value.length())).array();
                V = value.getBytes(UTF_8);
                break;

            case H: case X: case B:
                T = DataUtility.getByteArrayFromHexString(tag);
                L = ByteBuffer.allocate(2).putShort((short) (value.length() / 2)).array();
                V = DataUtility.getByteArrayFromHexString(value);
                break;
        }

        stream.write(T);
        stream.write(L);
        stream.write(V);

        return stream.toByteArray();
    }
}
