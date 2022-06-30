package io.cloudwalk.pos.pinpadlibrary.internals.utilities;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CHP;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.CMD;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.GPN;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.OPN;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.RMC;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLE;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLI;
import io.cloudwalk.pos.pinpadlibrary.internals.commands.TLR;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;

public class PinpadUtility {
    private static final String
            TAG = PinpadUtility.class.getSimpleName();

    private static byte[] _buildTLV(ABECS.TYPE type, String name, String value)
            throws Exception {
        // Log.d(TAG, "_buildTLV");

        byte[] T = null;
        byte[] L = null;
        byte[] V = null;

        try {
            T = ByteUtility.fromHexString(name);

            short[] length = {
                    (short) (value.length()),
                    (short) (value.length() / 2)
            };

            switch (type) {
                case A: case S: case N:
                    L = ByteBuffer.allocate(2).putShort(length[0]).array();
                    V = value.getBytes(UTF_8);
                    break;

                case H: case X: case B:
                    L = ByteBuffer.allocate(2).putShort(length[1]).array();
                    V = ByteUtility.fromHexString(value);
                    break;

                default:
                    L = new byte[0];
                    V = new byte[0];
                    break;
            }

            byte[] response = new byte[T.length + L.length + V.length];

            System.arraycopy(T, 0, response, 0, T.length);
            System.arraycopy(L, 0, response, T.length, L.length);
            System.arraycopy(V, 0, response, T.length + L.length, V.length);

            return response;
        } finally {
            ByteUtility.clear(T, L, V);
        }
    }

    private static byte[] _unwrapDataPacket(byte[] array, int length)
            throws Exception {
        // Log.d(TAG, "_unwrapDataPacket");

        byte[] pkt = new byte[2048 - 4];

        try {
            int threshold = Math.min(length, 2044 + 4);

            int j = 0;

            for (int i = 1; i < threshold; i++) {
                switch (array[i]) {
                    case 0x16: /* PKTSTART */
                        continue;

                    case 0x17: /* PKTSTOP  */
                        i = threshold;
                        continue;

                    case 0x13:
                        switch (array[++i]) {
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
                                pkt[j++] = array[i];
                                break;
                        }
                        break;

                    default:
                        pkt[j++] = array[i];
                        break;
                }
            }

            // TODO: validate CRC

            byte[] response = new byte[j];

            System.arraycopy(pkt, 0, response, 0, j);

            return response;
        } finally {
            ByteUtility.clear(pkt);
        }
    }

    private static byte[] _wrapDataPacket(byte[] array, int length)
            throws Exception {
        // Log.d(TAG, "_wrapDataPacket");

        byte[][] pkt = {
                new byte[2044 + 4],
                null
        };

        byte[][] crc = {
                new byte[length + 1],
                null
        };

        try {
            pkt[0][0] = 0x16; /* PKTSTART */

            int threshold = Math.min(length, 2044 + 4);

            int j = 1;

            for (int i = 0; i < threshold; i++) {
                switch (array[i]) {
                    case 0x13: /* DC3 */
                        pkt[0][j++] = 0x13;
                        pkt[0][j++] = 0x33;
                        break;

                    case 0x16: /* SYN */
                        pkt[0][j++] = 0x13;
                        pkt[0][j++] = 0x36;
                        break;

                    case 0x17: /* ETB */
                        pkt[0][j++] = 0x13;
                        pkt[0][j++] = 0x37;
                        break;

                    default:
                        pkt[0][j++] = array[i];
                        break;
                }
            }

            pkt[0][j] = 0x17; /* PKTSTOP */

            System.arraycopy(array, 0, crc[0], 0, length);

            crc[0][length] = pkt[0][j];

            crc[1] = ByteUtility.crc16(crc[0], 0, crc[0].length);

            System.arraycopy(crc[1], 0, pkt[0], j + 1, crc[1].length);

            pkt[1] = Arrays.copyOf(pkt[0], j + 1 + crc[1].length);

            return pkt[1];
        } finally {
            ByteUtility.clear(pkt[0], crc[0], crc[1]);
        }
    }

    private PinpadUtility() {
        Log.d(TAG, "PinpadUtility");

        /* Nothing to do */
    }

    public static String parseRequestDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket::array.length [" + array.length + "] length [" + length + "]");

        byte[] stream = _unwrapDataPacket(array, length);

        String CMD_ID;

        switch (CMD_ID = new String(stream, 0, 3)) {
            case ABECS.OPN:
                return OPN.parseRequestDataPacket(stream, stream.length);

            case ABECS.CHP:
                return CHP.parseRequestDataPacket(stream, stream.length);
            case ABECS.GPN:
                return GPN.parseRequestDataPacket(stream, stream.length);
            case ABECS.RMC:
                return RMC.parseRequestDataPacket(stream, stream.length);

            case ABECS.TLI:
                return TLI.parseRequestDataPacket(stream, stream.length);
            case ABECS.TLR:
                return TLR.parseRequestDataPacket(stream, stream.length);
            case ABECS.TLE:
                return TLE.parseRequestDataPacket(stream, stream.length);

            case ABECS.GIX: case ABECS.CLX:
            case ABECS.CEX: case ABECS.EBX: case ABECS.GTK:
            case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                return CMD.parseRequestDataPacket(stream, stream.length);

            case ABECS.GCD: case ABECS.MNU: // TODO: artificial GCD and MNU
            default:
                throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
        }
    }

    public static String parseResponseDataPacket(byte[] array, int length)
            throws Exception {
        Log.d(TAG, "parseResponseDataPacket::array.length [" + array.length + "] length [" + length + "]");

        byte[] stream = null;

        try {
            stream = _unwrapDataPacket(array, length);

            String RSP_ID;

            switch (RSP_ID = new String(stream, 0, 3)) {
                case ABECS.OPN:
                    return OPN.parseResponseDataPacket(stream, stream.length);

                case ABECS.CHP:
                    return CHP.parseResponseDataPacket(stream, stream.length);
                case ABECS.GPN:
                    return GPN.parseResponseDataPacket(stream, stream.length);
                case ABECS.RMC:
                    return RMC.parseResponseDataPacket(stream, stream.length);

                case ABECS.TLI:
                    return TLI.parseResponseDataPacket(stream, stream.length);
                case ABECS.TLR:
                    return TLR.parseResponseDataPacket(stream, stream.length);
                case ABECS.TLE:
                    return TLE.parseResponseDataPacket(stream, stream.length);

                case ABECS.GIX: case ABECS.CLX:
                case ABECS.CEX: case ABECS.EBX: case ABECS.GTK:
                case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                    return CMD.parseResponseDataPacket(stream, stream.length);

                case ABECS.GCD: case ABECS.MNU: // TODO: artificial GCD and MNU
                default:
                    throw new RuntimeException("Unknown or unhandled RSP_ID [" + RSP_ID + "]");
            }
        } finally {
            ByteUtility.clear(stream);
        }
    }

    public static String parseTLV(byte[] array, int offset, int length)
            throws Exception {
        Log.d(TAG, "parseTLV::array.length [" + array.length + "] offset [" + offset + "] length [" + length + "]");

        length = Math.max(length, 0);
        length = Math.min(length, array.length);

        offset = Math.max(offset, 0);

        JSONObject response = new JSONObject();

        int cursor    = offset;
        int threshold = 0;

        while ((cursor += threshold) < length) {
            byte[] T = new byte[4];
            byte[] L = new byte[4];
            byte[] V = null;

            try {
                System.arraycopy(array, cursor, T, 2, 2);

                cursor += 2;

                int tag = ByteBuffer.wrap(T).getInt();

                System.arraycopy(array, cursor, L, 2, 2);

                cursor += 2;

                threshold = ByteBuffer.wrap(L).getInt();

                V = new byte[threshold];

                System.arraycopy(array, cursor, V, 0, threshold);

                switch (tag) {
                    case 0x0002: response.put(ABECS.SPE_MTHDPIN,    new String(V)); break;
                    case 0x0003: response.put(ABECS.SPE_MTHDDAT,    new String(V)); break;
                    case 0x0006: response.put(ABECS.SPE_CEXOPT,     new String(V)); break;
                    case 0x0007: response.put(ABECS.SPE_TRACKS,     new String(V)); break;
                    case 0x0008: response.put(ABECS.SPE_OPNDIG,     new String(V)); break;
                    case 0x0009: response.put(ABECS.SPE_KEYIDX,     new String(V)); break;
                    case 0x0010: response.put(ABECS.SPE_ACQREF,     new String(V)); break;
                    case 0x0011: response.put(ABECS.SPE_APPTYPE,    new String(V)); break;
                    case 0x0012: response.put(ABECS.SPE_AIDLIST,    new String(V)); break;
                    case 0x0013: response.put(ABECS.SPE_AMOUNT,     new String(V)); break;
                    case 0x0014: response.put(ABECS.SPE_CASHBACK,   new String(V)); break;
                    case 0x0015: response.put(ABECS.SPE_TRNDATE,    new String(V)); break;
                    case 0x0016: response.put(ABECS.SPE_TRNTIME,    new String(V)); break;
                    case 0x0017: response.put(ABECS.SPE_GCXOPT,     new String(V)); break;
                    case 0x0018: response.put(ABECS.SPE_GOXOPT,     new String(V)); break;
                    case 0x0019: response.put(ABECS.SPE_FCXOPT,     new String(V)); break;
                    case 0x001B: response.put(ABECS.SPE_DSPMSG,     new String(V)); break;
                    case 0x001C: response.put(ABECS.SPE_ARC,        new String(V)); break;
                    case 0x001E: response.put(ABECS.SPE_MFNAME,     new String(V)); break;

                    case 0x0020:
                        JSONArray SPE_MNUOPT = (response.has(ABECS.SPE_MNUOPT)) ? response.getJSONArray(ABECS.SPE_MNUOPT) : new JSONArray();

                        SPE_MNUOPT.put(new String(V));

                        response.put(ABECS.SPE_MNUOPT, SPE_MNUOPT);
                        break;

                    case 0x0022: response.put(ABECS.SPE_TRNCURR,    new String(V)); break;
                    case 0x0023: response.put(ABECS.SPE_PANMASK,    new String(V)); break;

                    case 0x0001: response.put(ABECS.SPE_IDLIST,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x0004: response.put(ABECS.SPE_TAGLIST,    ByteUtility.getHexString(V, V.length)); break;
                    case 0x0005: response.put(ABECS.SPE_EMVDATA,    ByteUtility.getHexString(V, V.length)); break;
                    case 0x000A: response.put(ABECS.SPE_WKENC,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x000B: response.put(ABECS.SPE_MSGIDX,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x000C: response.put(ABECS.SPE_TIMEOUT,    ByteUtility.getHexString(V, V.length)); break;
                    case 0x000D: response.put(ABECS.SPE_MINDIG,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x000E: response.put(ABECS.SPE_MAXDIG,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x000F: response.put(ABECS.SPE_DATAIN,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x001A: response.put(ABECS.SPE_TRMPAR,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x001D: response.put(ABECS.SPE_IVCBC,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x0021: response.put(ABECS.SPE_TRNTYPE,    ByteUtility.getHexString(V, V.length)); break;
                    case 0x0024: response.put(ABECS.SPE_PBKMOD,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x0025: response.put(ABECS.SPE_PBKEXP,     ByteUtility.getHexString(V, V.length)); break;

                    case 0x8001: response.put(ABECS.PP_SERNUM,      new String(V)); break;
                    case 0x8002: response.put(ABECS.PP_PARTNBR,     new String(V)); break;
                    case 0x8003: response.put(ABECS.PP_MODEL,       new String(V)); break;
                    case 0x8004: response.put(ABECS.PP_MNNAME,      new String(V)); break;
                    case 0x8005: response.put(ABECS.PP_CAPAB,       new String(V)); break;
                    case 0x8006: response.put(ABECS.PP_SOVER,       new String(V)); break;
                    case 0x8007: response.put(ABECS.PP_SPECVER,     new String(V)); break;
                    case 0x8008: response.put(ABECS.PP_MANVERS,     new String(V)); break;
                    case 0x8009: response.put(ABECS.PP_APPVERS,     new String(V)); break;
                    case 0x800A: response.put(ABECS.PP_GENVERS,     new String(V)); break;
                    case 0x8010: response.put(ABECS.PP_KRNLVER,     new String(V)); break;
                    case 0x8011: response.put(ABECS.PP_CTLSVER,     new String(V)); break;
                    case 0x8012: response.put(ABECS.PP_MCTLSVER,    new String(V)); break;
                    case 0x8013: response.put(ABECS.PP_VCTLSVER,    new String(V)); break;
                    case 0x8014: response.put(ABECS.PP_AECTLSVER,   new String(V)); break;
                    case 0x8015: response.put(ABECS.PP_DPCTLSVER,   new String(V)); break;
                    case 0x8016: response.put(ABECS.PP_PUREVER,     new String(V)); break;
                    case 0x8032: response.put(ABECS.PP_MKTDESP,     new String(V)); break;
                    case 0x8033: response.put(ABECS.PP_MKTDESD,     new String(V)); break;
                    case 0x8035: response.put(ABECS.PP_DKPTTDESP,   new String(V)); break;
                    case 0x8036: response.put(ABECS.PP_DKPTTDESD,   new String(V)); break;
                    case 0x8040: response.put(ABECS.PP_EVENT,       new String(V)); break;
                    case 0x8041: response.put(ABECS.PP_TRK1INC,     new String(V)); break;
                    case 0x8042: response.put(ABECS.PP_TRK2INC,     new String(V)); break;
                    case 0x8043: response.put(ABECS.PP_TRK3INC,     new String(V)); break;

                    case 0x8044:
                        int i; // TODO: improve detection?!

                        for (i = 0; i < V.length; i++) {
                            if (V[i] < 0x20 || V[i] > 0x7E) {
                                break;
                            }
                        }

                        response.put(ABECS.PP_TRACK1, (i < V.length) ? ByteUtility.getHexString(V, V.length) : new String(V));
                        break;

                    case 0x804D: response.put(ABECS.PP_VALUE,       new String(V)); break;
                    case 0x804F: response.put(ABECS.PP_CARDTYPE,    new String(V)); break;
                    case 0x8050: response.put(ABECS.PP_ICCSTAT,     new String(V)); break;
                    case 0x8051: response.put(ABECS.PP_AIDTABINFO,  new String(V)); break;
                    case 0x8052: response.put(ABECS.PP_PAN,         new String(V)); break;
                    case 0x8053: response.put(ABECS.PP_PANSEQNO,    new String(V)); break;
                    case 0x8055: response.put(ABECS.PP_CHNAME,      new String(V)); break;
                    case 0x8056: response.put(ABECS.PP_GOXRES,      new String(V)); break;
                    case 0x8058: response.put(ABECS.PP_FCXRES,      new String(V)); break;
                    case 0x805B: response.put(ABECS.PP_LABEL,       new String(V)); break;
                    case 0x805C: response.put(ABECS.PP_ISSCNTRY,    new String(V)); break;
                    case 0x805D: response.put(ABECS.PP_CARDEXP,     new String(V)); break;
                    case 0x8060: response.put(ABECS.PP_DEVTYPE,     new String(V)); break;

                    case 0x8045: response.put(ABECS.PP_TRACK2,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x8046: response.put(ABECS.PP_TRACK3,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x8047: response.put(ABECS.PP_TRK1KSN,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x8048: response.put(ABECS.PP_TRK2KSN,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x8049: response.put(ABECS.PP_TRK3KSN,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x804A: response.put(ABECS.PP_ENCPAN,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x804B: response.put(ABECS.PP_ENCPANKSN,   ByteUtility.getHexString(V, V.length)); break;
                    case 0x804C: response.put(ABECS.PP_KSN,         ByteUtility.getHexString(V, V.length)); break;
                    case 0x804E: response.put(ABECS.PP_DATAOUT,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x8054: response.put(ABECS.PP_EMVDATA,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x8057: response.put(ABECS.PP_PINBLK,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x8059: response.put(ABECS.PP_ISRESULTS,   ByteUtility.getHexString(V, V.length)); break;
                    case 0x805A: response.put(ABECS.PP_BIGRAND,     ByteUtility.getHexString(V, V.length)); break;
                    case 0x8062: response.put(ABECS.PP_TLRMEM,      ByteUtility.getHexString(V, V.length)); break;
                    case 0x8063: response.put(ABECS.PP_ENCKRAND,    ByteUtility.getHexString(V, V.length)); break;

                    case 0x001F: /* SPE_MFINFO  */
                    case 0x8020: /* PP_DSPTXTSZ */
                    case 0x8021: /* PP_DSPGRSZ  */
                    case 0x8022: /* PP_MFSUP    */
                    case 0x805E: /* PP_MFNAME   */
                        /* 2021-12-14: out of scope */
                        break;

                    default:
                        String key;

                        if (tag >= 0x9100 && tag <= 0x9163) {
                            key = ABECS.PP_KSNTDESPnn.replace("nn", String.format(US, "%02d", (tag - 0x9100)));

                            response.put(key, ByteUtility.getHexString(V, V.length));
                        } else if (tag >= 0x9200 && tag <= 0x9263) {
                            key = ABECS.PP_KSNTDESDnn.replace("nn", String.format(US, "%02d", (tag - 0x9200)));

                            response.put(key, ByteUtility.getHexString(V, V.length));
                        } else if (tag >= 0x9300 && tag <= 0x9363) {
                            key = ABECS.PP_TABVERnn  .replace("nn", String.format(US, "%02d", (tag - 0x9300)));

                            response.put(key, new String(V));
                        } else {
                            key = String.format(US, "%04X", tag);

                            response.put(key, ByteUtility.getHexString(V, V.length));
                        }
                        break;
                }
            } finally {
                ByteUtility.clear(T, L, V);
            }
        }

        return response.toString();
    }

    public static byte[] buildRequestDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket::string [" + string + "]");

        byte[] stream = null;

        try {
            String CMD_ID = "UNKNOWN";

            try {
                CMD_ID = (new JSONObject(string)).getString(ABECS.CMD_ID);
            } catch (Exception ignored) { }

            switch (CMD_ID) {
                case ABECS.OPN:
                    stream = OPN.buildRequestDataPacket(string);
                    break;

                case ABECS.CHP:
                    stream = CHP.buildRequestDataPacket(string);
                    break;
                case ABECS.GPN:
                    stream = GPN.buildRequestDataPacket(string);
                    break;
                case ABECS.RMC:
                    stream = RMC.buildRequestDataPacket(string);
                    break;

                case ABECS.TLI:
                    stream = TLI.buildRequestDataPacket(string);
                    break;
                case ABECS.TLR:
                    stream = TLR.buildRequestDataPacket(string);
                    break;
                case ABECS.TLE:
                    stream = TLE.buildRequestDataPacket(string);
                    break;

                case ABECS.GIX: case ABECS.CLX:
                case ABECS.CEX: case ABECS.EBX: case ABECS.GTK:
                case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                    stream = CMD.buildRequestDataPacket(string);
                    break;

                case ABECS.GCD: case ABECS.MNU: // TODO: artificial GCD and MNU
                default:
                    /* Nothing to do */
                    break;
            }

            if (stream != null) {
                if (stream.length <= 2048) {
                    return _wrapDataPacket(stream, stream.length);
                }

                throw new RuntimeException("CMD_ID [" + CMD_ID + "] packet exceeds maximum length (2048)");
            } else {
                throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
            }
        } finally {
            ByteUtility.clear(stream);
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull String string)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket::string [" + string + "]");

        byte[] stream = null;

        try {
            String RSP_ID = "UNKNOWN";

            try {
                RSP_ID = (new JSONObject(string)).getString(ABECS.RSP_ID);
            } catch (Exception ignored) { }

            switch (RSP_ID) {
                case ABECS.OPN:
                    stream = OPN.buildResponseDataPacket(string);
                    break;

                case ABECS.CHP:
                    stream = CHP.buildResponseDataPacket(string);
                    break;
                case ABECS.GPN:
                    stream = GPN.buildResponseDataPacket(string);
                    break;
                case ABECS.RMC:
                    stream = RMC.buildResponseDataPacket(string);
                    break;

                case ABECS.TLI:
                    stream = TLI.buildResponseDataPacket(string);
                    break;
                case ABECS.TLR:
                    stream = TLR.buildResponseDataPacket(string);
                    break;
                case ABECS.TLE:
                    stream = TLE.buildResponseDataPacket(string);
                    break;

                case ABECS.GIX: case ABECS.CLX:
                case ABECS.CEX: case ABECS.EBX: case ABECS.GTK:
                case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                    stream = CMD.buildResponseDataPacket(string);
                    break;

                case ABECS.GCD: case ABECS.MNU: // TODO: artificial GCD and MNU
                default:
                    /* Nothing to do */
                    break;
            }

            if (stream != null) {
                if (stream.length <= 2048) {
                    return _wrapDataPacket(stream, stream.length);
                }

                throw new RuntimeException("RSP_ID [" + RSP_ID + "] packet exceeds maximum length (2048)");
            } else {
                throw new RuntimeException("Unknown or unhandled RSP_ID [" + RSP_ID + "]");
            }
        } finally {
            ByteUtility.clear(stream);
        }
    }

    public static byte[] buildTLV(@NotNull String name, @NotNull String value)
            throws Exception {
        Log.d(TAG, "buildTLV::name [" + name + "] value [" + value.replace("\n", "\\n").replace("\r", "\\r") + "]");

        switch (name) {
            case ABECS.SPE_MTHDPIN:
                return _buildTLV(ABECS.TYPE.N, "0002", value);
            case ABECS.SPE_MTHDDAT:
                return _buildTLV(ABECS.TYPE.N, "0003", value);
            case ABECS.SPE_CEXOPT:
                return _buildTLV(ABECS.TYPE.A, "0006", value);
            case ABECS.SPE_TRACKS:
                return _buildTLV(ABECS.TYPE.N, "0007", value);
            case ABECS.SPE_OPNDIG:
                return _buildTLV(ABECS.TYPE.N, "0008", value);
            case ABECS.SPE_KEYIDX:
                return _buildTLV(ABECS.TYPE.N, "0009", value);
            case ABECS.SPE_ACQREF:
                return _buildTLV(ABECS.TYPE.N, "0010", value);
            case ABECS.SPE_APPTYPE:
                return _buildTLV(ABECS.TYPE.N, "0011", value);
            case ABECS.SPE_AIDLIST:
                return _buildTLV(ABECS.TYPE.A, "0012", value);
            case ABECS.SPE_AMOUNT:
                return _buildTLV(ABECS.TYPE.N, "0013", value);
            case ABECS.SPE_CASHBACK:
                return _buildTLV(ABECS.TYPE.N, "0014", value);
            case ABECS.SPE_TRNDATE:
                return _buildTLV(ABECS.TYPE.N, "0015", value);
            case ABECS.SPE_TRNTIME:
                return _buildTLV(ABECS.TYPE.N, "0016", value);
            case ABECS.SPE_GCXOPT:
                return _buildTLV(ABECS.TYPE.N, "0017", value);
            case ABECS.SPE_GOXOPT:
                return _buildTLV(ABECS.TYPE.N, "0018", value);
            case ABECS.SPE_FCXOPT:
                return _buildTLV(ABECS.TYPE.N, "0019", value);
            case ABECS.SPE_DSPMSG:
                return _buildTLV(ABECS.TYPE.S, "001B", value);
            case ABECS.SPE_ARC:
                return _buildTLV(ABECS.TYPE.A, "001C", value);
            case ABECS.SPE_MFNAME:
                return _buildTLV(ABECS.TYPE.A, "001E", value);

            case ABECS.SPE_MNUOPT:
                ByteArrayOutputStream[] stream = {
                        new ByteArrayOutputStream()
                };

                byte[] response = null;

                try {
                    JSONArray array = new JSONArray(value);

                    for (int i = 0; i < array.length(); i++) {
                        try {
                            response = _buildTLV(ABECS.TYPE.S, "0020", array.getString(i));

                            stream[0].write(response);
                        } finally {
                            ByteUtility.clear(response);
                        }
                    }

                    response = stream[0].toByteArray();
                } finally {
                    ByteUtility.clear(stream[0]);
                }

                return response;

            case ABECS.SPE_TRNCURR:
                return _buildTLV(ABECS.TYPE.N, "0022", value);
            case ABECS.SPE_PANMASK:
                return _buildTLV(ABECS.TYPE.N, "0023", value);
            case ABECS.SPE_IDLIST:
                return _buildTLV(ABECS.TYPE.B, "0001", value);
            case ABECS.SPE_TAGLIST:
                return _buildTLV(ABECS.TYPE.B, "0004", value);
            case ABECS.SPE_EMVDATA:
                return _buildTLV(ABECS.TYPE.B, "0005", value);
            case ABECS.SPE_WKENC:
                return _buildTLV(ABECS.TYPE.B, "000A", value);
            case ABECS.SPE_MSGIDX:
                return _buildTLV(ABECS.TYPE.X, "000B", value);
            case ABECS.SPE_TIMEOUT:
                return _buildTLV(ABECS.TYPE.X, "000C", value);
            case ABECS.SPE_MINDIG:
                return _buildTLV(ABECS.TYPE.X, "000D", value);
            case ABECS.SPE_MAXDIG:
                return _buildTLV(ABECS.TYPE.X, "000E", value);
            case ABECS.SPE_DATAIN:
                return _buildTLV(ABECS.TYPE.B, "000F", value);
            case ABECS.SPE_TRMPAR:
                return _buildTLV(ABECS.TYPE.B, "001A", value);
            case ABECS.SPE_IVCBC:
                return _buildTLV(ABECS.TYPE.B, "001D", value);
            case ABECS.SPE_TRNTYPE:
                return _buildTLV(ABECS.TYPE.B, "0021", value);
            case ABECS.SPE_PBKMOD:
                return _buildTLV(ABECS.TYPE.B, "0024", value);
            case ABECS.SPE_PBKEXP:
                return _buildTLV(ABECS.TYPE.B, "0025", value);

            case ABECS.PP_SERNUM:
                return _buildTLV(ABECS.TYPE.A, "8001", value);
            case ABECS.PP_PARTNBR:
                return _buildTLV(ABECS.TYPE.A, "8002", value);
            case ABECS.PP_MODEL:
                return _buildTLV(ABECS.TYPE.A, "8003", value);
            case ABECS.PP_MNNAME:
                return _buildTLV(ABECS.TYPE.A, "8004", value);
            case ABECS.PP_CAPAB:
                return _buildTLV(ABECS.TYPE.A, "8005", value);
            case ABECS.PP_SOVER:
                return _buildTLV(ABECS.TYPE.A, "8006", value);
            case ABECS.PP_SPECVER:
                return _buildTLV(ABECS.TYPE.A, "8007", value);
            case ABECS.PP_MANVERS:
                return _buildTLV(ABECS.TYPE.A, "8008", value);
            case ABECS.PP_APPVERS:
                return _buildTLV(ABECS.TYPE.A, "8009", value);
            case ABECS.PP_GENVERS:
                return _buildTLV(ABECS.TYPE.A, "800A", value);
            case ABECS.PP_KRNLVER:
                return _buildTLV(ABECS.TYPE.A, "8010", value);
            case ABECS.PP_CTLSVER:
                return _buildTLV(ABECS.TYPE.A, "8011", value);
            case ABECS.PP_MCTLSVER:
                return _buildTLV(ABECS.TYPE.A, "8012", value);
            case ABECS.PP_VCTLSVER:
                return _buildTLV(ABECS.TYPE.A, "8013", value);
            case ABECS.PP_AECTLSVER:
                return _buildTLV(ABECS.TYPE.A, "8014", value);
            case ABECS.PP_DPCTLSVER:
                return _buildTLV(ABECS.TYPE.A, "8015", value);
            case ABECS.PP_PUREVER:
                return _buildTLV(ABECS.TYPE.A, "8016", value);
            case ABECS.PP_MKTDESP:
                return _buildTLV(ABECS.TYPE.A, "8032", value);
            case ABECS.PP_MKTDESD:
                return _buildTLV(ABECS.TYPE.A, "8033", value);
            case ABECS.PP_DKPTTDESP:
                return _buildTLV(ABECS.TYPE.A, "8035", value);
            case ABECS.PP_DKPTTDESD:
                return _buildTLV(ABECS.TYPE.A, "8036", value);
            case ABECS.PP_EVENT:
                return _buildTLV(ABECS.TYPE.A, "8040", value);
            case ABECS.PP_TRK1INC:
                return _buildTLV(ABECS.TYPE.A, "8041", value);
            case ABECS.PP_TRK2INC:
                return _buildTLV(ABECS.TYPE.A, "8042", value);
            case ABECS.PP_TRK3INC:
                return _buildTLV(ABECS.TYPE.A, "8043", value);
            case ABECS.PP_TRACK1:
                return _buildTLV(ABECS.TYPE.B, "8044", value);
            case ABECS.PP_TRACK2:
                return _buildTLV(ABECS.TYPE.B, "8045", value);
            case ABECS.PP_TRACK3:
                return _buildTLV(ABECS.TYPE.B, "8046", value);
            case ABECS.PP_TRK1KSN:
                return _buildTLV(ABECS.TYPE.B, "8047", value);
            case ABECS.PP_TRK2KSN:
                return _buildTLV(ABECS.TYPE.B, "8048", value);
            case ABECS.PP_TRK3KSN:
                return _buildTLV(ABECS.TYPE.B, "8049", value);
            case ABECS.PP_ENCPAN:
                return _buildTLV(ABECS.TYPE.B, "804A", value);
            case ABECS.PP_ENCPANKSN:
                return _buildTLV(ABECS.TYPE.B, "804B", value);
            case ABECS.PP_KSN:
                return _buildTLV(ABECS.TYPE.B, "804C", value);
            case ABECS.PP_VALUE:
                return _buildTLV(ABECS.TYPE.A, "804D", value);
            case ABECS.PP_DATAOUT:
                return _buildTLV(ABECS.TYPE.B, "804E", value);
            case ABECS.PP_CARDTYPE:
                return _buildTLV(ABECS.TYPE.N, "804F", value);
            case ABECS.PP_ICCSTAT:
                return _buildTLV(ABECS.TYPE.N, "8050", value);
            case ABECS.PP_AIDTABINFO:
                return _buildTLV(ABECS.TYPE.A, "8051", value);
            case ABECS.PP_PAN:
                return _buildTLV(ABECS.TYPE.N, "8052", value);
            case ABECS.PP_PANSEQNO:
                return _buildTLV(ABECS.TYPE.N, "8053", value);
            case ABECS.PP_EMVDATA:
                return _buildTLV(ABECS.TYPE.B, "8054", value);
            case ABECS.PP_CHNAME:
                return _buildTLV(ABECS.TYPE.A, "8055", value);
            case ABECS.PP_GOXRES:
                return _buildTLV(ABECS.TYPE.N, "8056", value);
            case ABECS.PP_PINBLK:
                return _buildTLV(ABECS.TYPE.B, "8057", value);
            case ABECS.PP_FCXRES:
                return _buildTLV(ABECS.TYPE.N, "8058", value);
            case ABECS.PP_ISRESULTS:
                return _buildTLV(ABECS.TYPE.B, "8059", value);
            case ABECS.PP_BIGRAND:
                return _buildTLV(ABECS.TYPE.B, "805A", value);
            case ABECS.PP_LABEL:
                return _buildTLV(ABECS.TYPE.S, "805B", value);
            case ABECS.PP_ISSCNTRY:
                return _buildTLV(ABECS.TYPE.N, "805C", value);
            case ABECS.PP_CARDEXP:
                return _buildTLV(ABECS.TYPE.N, "805D", value);
            case ABECS.PP_DEVTYPE:
                return _buildTLV(ABECS.TYPE.N, "8060", value);
            case ABECS.PP_TLRMEM:
                return _buildTLV(ABECS.TYPE.X, "8062", value);
            case ABECS.PP_ENCKRAND:
                return _buildTLV(ABECS.TYPE.B, "8063", value);

            default:
                if (name.startsWith(ABECS.PP_KSNTDESPnn.substring(0, 11))) {
                    name = String.format(US, "%4.4s", (Integer.parseInt(name.substring(11)) + 0x9100)).replace(' ', '0');

                    return _buildTLV(ABECS.TYPE.B, name, value);
                } else if (name.startsWith(ABECS.PP_KSNTDESDnn.substring(0, 11))) {
                    name = String.format(US, "%4.4s", (Integer.parseInt(name.substring(11)) + 0x9200)).replace(' ', '0');

                    return _buildTLV(ABECS.TYPE.B, name, value);
                } else if (name.startsWith(ABECS.PP_TABVERnn.substring(0, 9))) {
                    name = String.format(US, "%4.4s", (Integer.parseInt(name.substring(9))  + 0x9300)).replace(' ', '0');

                    return _buildTLV(ABECS.TYPE.B, name, value);
                }

                throw new RuntimeException("Unknown or unhandled TAG [" + name + "]");
        }
    }
}
