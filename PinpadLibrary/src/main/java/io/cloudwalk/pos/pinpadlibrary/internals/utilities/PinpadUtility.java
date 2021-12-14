package io.cloudwalk.pos.pinpadlibrary.internals.utilities;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CEX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CHP;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CLX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.EBX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.FCX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GCD;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GCX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GED;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GIX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GOX;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GPN;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GTK;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.MNU;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.OPN;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.RMC;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLE;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLI;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLR;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

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

        public static Bundle parseRequestDataPacket(byte[] input, int length)
                throws Exception {
            Log.d(TAG, "parseRequestDataPacket::length [" + length + "]");

            byte[] CMD_ID   = new byte[3];
            byte[] CMD_LEN1 = new byte[3];
            byte[] CMD_DATA = null;

            System.arraycopy(input, 0, CMD_ID,   0, 3);
            System.arraycopy(input, 3, CMD_LEN1, 0, 3);

            Bundle output = new Bundle();

            output.putString(ABECS.CMD_ID, new String(CMD_ID));

            if (length < 7) {
                return output;
            }

            int len1 = DataUtility.getIntFromByteArray(CMD_LEN1, CMD_LEN1.length);

            CMD_DATA = new byte[len1];

            System.arraycopy(input, 6, CMD_DATA, 0, CMD_DATA.length);

            output.putAll(PinpadUtility.parseTLV(CMD_DATA, CMD_DATA.length));

            return output;
        }

        public static Bundle parseResponseDataPacket(byte[] input, int length)
                throws Exception {
            Log.d(TAG, "parseResponseDataPacket::length [" + length + "]");

            byte[] RSP_ID   = new byte[3];
            byte[] RSP_STAT = new byte[3];
            byte[] RSP_LEN1 = new byte[3];
            byte[] RSP_DATA = null;

            System.arraycopy(input, 0, RSP_ID,   0, 3);
            System.arraycopy(input, 3, RSP_STAT, 0, 3);
            System.arraycopy(input, 6, RSP_LEN1, 0, 3);

            Bundle output = new Bundle();

            output.putString      (ABECS.RSP_ID,   new String(RSP_ID));
            output.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[DataUtility.getIntFromByteArray(RSP_STAT, RSP_STAT.length)]);

            if (length < 10) {
                return output;
            }

            int len1 = DataUtility.getIntFromByteArray(RSP_LEN1, RSP_LEN1.length);

            RSP_DATA = new byte[len1];

            System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

            output.putAll(PinpadUtility.parseTLV(RSP_DATA, RSP_DATA.length));

            return output;
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

        // TODO: validate CRC

        byte[] output = new byte[j];

        System.arraycopy(pkt, 0, output, 0, j);

        return output;
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket");

        byte[] request = unwrapDataPacket(input, length);

        String CMD_ID = String.format(US, "%c%c%c", request[0], request[1], request[2]);

        switch (CMD_ID) {
            case ABECS.OPN: return null; // TODO: OPN.parseRequestDataPacket(request, request.length);

            case ABECS.CHP: return null; // TODO: CHP.parseRequestDataPacket(request, request.length);
            case ABECS.GPN: return null; // TODO: GPN.parseRequestDataPacket(request, request.length);
            case ABECS.RMC: return null; // TODO: RMC.parseRequestDataPacket(request, request.length);

            case ABECS.TLI: return null; // TODO: TLI.parseRequestDataPacket(request, request.length);
            case ABECS.TLR: return null; // TODO: TLR.parseRequestDataPacket(request, request.length);
            case ABECS.TLE: return null; // TODO: TLE.parseRequestDataPacket(request, request.length);

            case ABECS.GIX: case ABECS.CLX:
            case ABECS.CEX: case ABECS.EBX: case ABECS.GCD: case ABECS.GTK: case ABECS.MNU:
            case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                return CMD.parseRequestDataPacket(request, request.length);

            default:
                /* Nothing to do */
                break;
        }

        throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
    }

    public static Bundle parseResponseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket");

        byte[] response = unwrapDataPacket(input, length);

        String CMD_ID = String.format(US, "%c%c%c", response[0], response[1], response[2]);

        switch (CMD_ID) {
            case ABECS.OPN: return OPN.parseResponseDataPacket(response, response.length);
            case ABECS.CHP: return CHP.parseResponseDataPacket(response, response.length);
            case ABECS.GPN: return GPN.parseResponseDataPacket(response, response.length);

            case ABECS.GIX: case ABECS.CLX:
            case ABECS.CEX: case ABECS.EBX: case ABECS.GCD: case ABECS.GTK: case ABECS.MNU: case ABECS.RMC:
            case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
            case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                return CMD.parseResponseDataPacket(response, response.length);

            default:
                /* Nothing to do */
                break;
        }

        throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
    }

    public static Bundle parseTLV(byte[] stream, int length) {
        Bundle output = new Bundle();

        int cursor    = 0;
        int threshold = 0;

        while ((cursor += threshold) < length) {
            byte[] T = new byte[4];
            byte[] L = new byte[4];

            System.arraycopy(stream, cursor, T, 2, 2);

            cursor += 2;

            int tag = ByteBuffer.wrap(T).getInt();

            System.arraycopy(stream, cursor, L, 2, 2);

            cursor += 2;

            threshold = ByteBuffer.wrap(L).getInt();

            byte[] V = new byte[threshold];

            System.arraycopy(stream, cursor, V, 0, threshold);

            switch (tag) {
                case 0x0002: output.putString(ABECS.SPE_MTHDPIN,    new String(V)); break;
                case 0x0003: output.putString(ABECS.SPE_MTHDDAT,    new String(V)); break;
                case 0x0006: output.putString(ABECS.SPE_CEXOPT,     new String(V)); break;
                case 0x0007: output.putString(ABECS.SPE_TRACKS,     new String(V)); break;
                case 0x0008: output.putString(ABECS.SPE_OPNDIG,     new String(V)); break;
                case 0x0009: output.putString(ABECS.SPE_KEYIDX,     new String(V)); break;
                case 0x0010: output.putString(ABECS.SPE_ACQREF,     new String(V)); break;
                case 0x0011: output.putString(ABECS.SPE_APPTYPE,    new String(V)); break;
                case 0x0012: output.putString(ABECS.SPE_AIDLIST,    new String(V)); break;
                case 0x0013: output.putString(ABECS.SPE_AMOUNT,     new String(V)); break;
                case 0x0014: output.putString(ABECS.SPE_CASHBACK,   new String(V)); break;
                case 0x0015: output.putString(ABECS.SPE_TRNDATE,    new String(V)); break;
                case 0x0016: output.putString(ABECS.SPE_TRNTIME,    new String(V)); break;
                case 0x0017: output.putString(ABECS.SPE_GCXOPT,     new String(V)); break;
                case 0x0018: output.putString(ABECS.SPE_GOXOPT,     new String(V)); break;
                case 0x0019: output.putString(ABECS.SPE_FCXOPT,     new String(V)); break;
                case 0x001B: output.putString(ABECS.SPE_DSPMSG,     new String(V)); break;
                case 0x001C: output.putString(ABECS.SPE_ARC,        new String(V)); break;
                case 0x001E: output.putString(ABECS.SPE_MFNAME,     new String(V)); break;

                case 0x0020:
                    ArrayList<String> list = output.getStringArrayList(ABECS.SPE_MNUOPT);

                    if (list == null) {
                        list = new ArrayList<>(0);
                    }

                    list.add(new String(V));

                    output.putStringArrayList(ABECS.SPE_MNUOPT, list);
                    break;

                case 0x0022: output.putString(ABECS.SPE_TRNCURR,    new String(V)); break;
                case 0x0023: output.putString(ABECS.SPE_PANMASK,    new String(V)); break;

                case 0x0001: output.putString(ABECS.SPE_IDLIST,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0004: output.putString(ABECS.SPE_TAGLIST,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0005: output.putString(ABECS.SPE_EMVDATA,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000A: output.putString(ABECS.SPE_WKENC,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000B: output.putString(ABECS.SPE_MSGIDX,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000C: output.putString(ABECS.SPE_TIMEOUT,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000D: output.putString(ABECS.SPE_MINDIG,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000E: output.putString(ABECS.SPE_MAXDIG,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000F: output.putString(ABECS.SPE_DATAIN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x001A: output.putString(ABECS.SPE_TRMPAR,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x001D: output.putString(ABECS.SPE_IVCBC,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0021: output.putString(ABECS.SPE_TRNTYPE,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0024: output.putString(ABECS.SPE_PBKMOD,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0025: output.putString(ABECS.SPE_PBKEXP,     DataUtility.getHexStringFromByteArray(V)); break;

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

                case 0x8044:
                    int i;

                    for (i = 0; i < V.length; i++) {
                        if (V[i] < 0x20 || V[i] > 0x7E) {
                            break;
                        }
                    }

                    output.putString(ABECS.PP_TRACK1, (i < V.length) ? DataUtility.getHexStringFromByteArray(V) : new String(V));
                    break;

                case 0x804D: output.putString(ABECS.PP_VALUE,       new String(V)); break;
                case 0x804F: output.putString(ABECS.PP_CARDTYPE,    new String(V)); break;
                case 0x8050: output.putString(ABECS.PP_ICCSTAT,     new String(V)); break;
                case 0x8051: output.putString(ABECS.PP_AIDTABINFO,  new String(V)); break;
                case 0x8052: output.putString(ABECS.PP_PAN,         new String(V)); break;
                case 0x8053: output.putString(ABECS.PP_PANSEQNO,    new String(V)); break;
                case 0x8055: output.putString(ABECS.PP_CHNAME,      new String(V)); break;
                case 0x8056: output.putString(ABECS.PP_GOXRES,      new String(V)); break;
                case 0x8058: output.putString(ABECS.PP_FCXRES,      new String(V)); break;
                case 0x805B: output.putString(ABECS.PP_LABEL,       new String(V)); break;
                case 0x805C: output.putString(ABECS.PP_ISSCNTRY,    new String(V)); break;
                case 0x805D: output.putString(ABECS.PP_CARDEXP,     new String(V)); break;
                case 0x8060: output.putString(ABECS.PP_DEVTYPE,     new String(V)); break;

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
                case 0x8059: output.putString(ABECS.PP_ISRESULTS,   DataUtility.getHexStringFromByteArray(V)); break;
                case 0x805A: output.putString(ABECS.PP_BIGRAND,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8062: output.putString(ABECS.PP_TLRMEM,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8063: output.putString(ABECS.PP_ENCKRAND,    DataUtility.getHexStringFromByteArray(V)); break;

                case 0x001F: /* SPE_MFINFO  */
                case 0x8020: /* PP_DSPTXTSZ */
                case 0x8021: /* PP_DSPGRSZ  */
                case 0x8022: /* PP_MFSUP    */
                case 0x805E: /* PP_MFNAME   */
                    /* 2021-12-14: out-of-scope */

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

                    // TODO: add unknown or out-of-scope TAG to bundle: e.g. output.putString("0x1234", "0123...ABCDEF");
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
            case ABECS.CHP: request = CHP.buildRequestDataPacket(input); break;
            case ABECS.EBX: request = EBX.buildRequestDataPacket(input); break;
            case ABECS.GCD: request = GCD.buildRequestDataPacket(input); break;
            case ABECS.GPN: request = GPN.buildRequestDataPacket(input); break;
            case ABECS.GTK: request = GTK.buildRequestDataPacket(input); break;
            case ABECS.MNU: request = MNU.buildRequestDataPacket(input); break;
            case ABECS.RMC: request = RMC.buildRequestDataPacket(input); break;

            case ABECS.TLI: request = TLI.buildRequestDataPacket(input); break;
            case ABECS.TLR: request = TLR.buildRequestDataPacket(input); break;
            case ABECS.TLE: request = TLE.buildRequestDataPacket(input); break;

            case ABECS.GCX: request = GCX.buildRequestDataPacket(input); break;
            case ABECS.GED: request = GED.buildRequestDataPacket(input); break;
            case ABECS.GOX: request = GOX.buildRequestDataPacket(input); break;
            case ABECS.FCX: request = FCX.buildRequestDataPacket(input); break;

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
