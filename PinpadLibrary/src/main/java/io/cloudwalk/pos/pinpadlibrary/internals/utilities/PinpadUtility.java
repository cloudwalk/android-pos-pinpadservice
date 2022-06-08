package io.cloudwalk.pos.pinpadlibrary.internals.utilities;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            Log.d(TAG, "parseRequestDataPacket");

            byte[] CMD_ID   = new byte[3];
            byte[] CMD_LEN1 = new byte[3];
            byte[] CMD_DATA = null;

            System.arraycopy(input, 0, CMD_ID,   0, 3);
            System.arraycopy(input, 3, CMD_LEN1, 0, 3);

            Bundle response = new Bundle();

            response.putString(ABECS.CMD_ID, new String(CMD_ID));

            if (length < 7) {
                return response;
            }

            CMD_DATA = new byte[PinpadUtility.getIntFromDigitsArray(CMD_LEN1, CMD_LEN1.length)];

            System.arraycopy(input, 6, CMD_DATA, 0, CMD_DATA.length);

            response.putAll(PinpadUtility.parseTLV(CMD_DATA, CMD_DATA.length));

            return response;
        }

        public static Bundle parseResponseDataPacket(byte[] input, int length)
                throws Exception {
            Log.d(TAG, "parseResponseDataPacket");

            byte[] RSP_ID   = new byte[3];
            byte[] RSP_STAT = new byte[3];
            byte[] RSP_LEN1 = new byte[3];
            byte[] RSP_DATA = null;

            System.arraycopy(input, 0, RSP_ID,   0, 3);
            System.arraycopy(input, 3, RSP_STAT, 0, 3);

            Bundle response = new Bundle();

            response.putString(ABECS.RSP_ID, new String(RSP_ID));
            response.putSerializable(ABECS.RSP_STAT, ABECS.STAT.values()[PinpadUtility.getIntFromDigitsArray(RSP_STAT, RSP_STAT.length)]);

            if (length < 10) {
                return response;
            }

            System.arraycopy(input, 6, RSP_LEN1, 0, 3);

            RSP_DATA = new byte[PinpadUtility.getIntFromDigitsArray(RSP_LEN1, RSP_LEN1.length)];

            System.arraycopy(input, 9, RSP_DATA, 0, RSP_DATA.length);

            response.putAll(PinpadUtility.parseTLV(RSP_DATA, RSP_DATA.length));

            return response;
        }

        public static byte[] buildRequestDataPacket(@NotNull Bundle input)
                throws Exception {
            Log.d(TAG, "buildRequestDataPacket");

            ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

            String CMD_ID = null;

            for (String T : input.keySet()) {
                switch (T) {
                    case ABECS.CMD_ID:
                        CMD_ID = input.getString(T);
                        break;

                    default:
                        stream[1].write(PinpadUtility.buildTLV(T, input.getString(T)));
                        break;
                }
            }

            byte[] RSP_DATA = stream[1].toByteArray();

            stream[0].write(CMD_ID.getBytes(UTF_8));
            stream[0].write(String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8));
            stream[0].write(RSP_DATA);

            return stream[0].toByteArray();
        }

        public static byte[] buildRequestDataPacket(@NotNull Bundle input, @NotNull List<String> sort)
                throws Exception {
            ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

            String CMD_ID = input.getString(ABECS.CMD_ID);

            for (String entry : sort) {
                String value = input.getString(entry);

                if (value != null) {
                    stream[1].write(PinpadUtility.buildTLV(entry, value));
                }
            }

            byte[] CMD_DATA = stream[1].toByteArray();

            stream[0].write(CMD_ID.getBytes(UTF_8));
            stream[0].write(String.format(US, "%03d", CMD_DATA.length).getBytes(UTF_8));
            stream[0].write(CMD_DATA);

            return stream[0].toByteArray();
        }

        public static byte[] buildResponseDataPacket(@NotNull Bundle input)
                throws Exception {
            Log.d(TAG, "buildResponseDataPacket");

            ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

            String RSP_ID   = null;
            int    RSP_STAT = ABECS.STAT.ST_INTERR.ordinal();

            for (String T : input.keySet()) {
                switch (T) {
                    case ABECS.RSP_ID:
                        RSP_ID = input.getString(T);
                        break;

                    case ABECS.RSP_STAT:
                        RSP_STAT = ((ABECS.STAT) input.getSerializable(T)).ordinal();
                        break;

                    default:
                        stream[1].write(PinpadUtility.buildTLV(T, input.getString(T)));
                        break;
                }
            }

            byte[] RSP_DATA = stream[1].toByteArray();

            stream[0].write(RSP_ID.getBytes(UTF_8));
            stream[0].write(String.format(US, "%03d", RSP_STAT).getBytes(UTF_8));
            stream[0].write(String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8));
            stream[0].write(RSP_DATA);

            return stream[0].toByteArray();
        }

        public static byte[] buildResponseDataPacket(@NotNull Bundle input, @NotNull List<String> sort)
                throws Exception {
            Log.d(TAG, "buildResponseDataPacket");

            ByteArrayOutputStream[] stream = { new ByteArrayOutputStream(), new ByteArrayOutputStream() };

            String RSP_ID = input.getString(ABECS.RSP_ID);

            for (String entry : sort) {
                String value = input.getString(entry);

                if (value != null) {
                    stream[1].write(PinpadUtility.buildTLV(entry, value));
                }
            }

            int    RSP_STAT = ((ABECS.STAT) input.getSerializable(ABECS.RSP_STAT)).ordinal();
            byte[] RSP_DATA = stream[1].toByteArray();

            stream[0].write(RSP_ID.getBytes(UTF_8));
            stream[0].write(String.format(US, "%03d", RSP_STAT).getBytes(UTF_8));
            stream[0].write(String.format(US, "%03d", RSP_DATA.length).getBytes(UTF_8));
            stream[0].write(RSP_DATA);

            return stream[0].toByteArray();
        }
    }

    private static byte[] _buildTLV(@NotNull ABECS.TYPE type, @NotNull String tag, @NotNull String value)
            throws Exception {
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

    private static byte[] _unwrapDataPacket(byte[] input, int length)
            throws Exception {
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

        byte[] response = new byte[j];

        System.arraycopy(pkt, 0, response, 0, j);

        return response;
    }

    private static byte[] _wrapDataPacket(byte[] input, int length)
            throws Exception {
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

    private PinpadUtility() {
        Log.d(TAG, "PinpadUtility");

        /* Nothing to do */
    }

    public static Bundle parseRequestDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseRequestDataPacket::input.length [" + input.length + "] length [" + length + "]");

        byte[] request = _unwrapDataPacket(input, length);

        String CMD_ID = String.format(US, "%c%c%c", request[0], request[1], request[2]);

        switch (CMD_ID) {
            case ABECS.OPN: return OPN.parseRequestDataPacket(request, request.length);

            case ABECS.CHP: return CHP.parseRequestDataPacket(request, request.length);
            case ABECS.GPN: return GPN.parseRequestDataPacket(request, request.length);
            case ABECS.RMC: return RMC.parseRequestDataPacket(request, request.length);

            case ABECS.TLI: return TLI.parseRequestDataPacket(request, request.length);
            case ABECS.TLR: return TLR.parseRequestDataPacket(request, request.length);
            case ABECS.TLE: return TLE.parseRequestDataPacket(request, request.length);

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
        Log.d(TAG, "parseResponseDataPacket::input.length [" + input.length + "] length [" + length + "]");

        byte[] response = _unwrapDataPacket(input, length);

        String CMD_ID = String.format(US, "%c%c%c", response[0], response[1], response[2]);

        switch (CMD_ID) {
            case ABECS.OPN: return OPN.parseResponseDataPacket(response, response.length);

            case ABECS.CHP: return CHP.parseResponseDataPacket(response, response.length);
            case ABECS.GPN: return GPN.parseResponseDataPacket(response, response.length);
            case ABECS.RMC: return RMC.parseResponseDataPacket(response, response.length);

            case ABECS.TLI: return TLI.parseResponseDataPacket(response, response.length);
            case ABECS.TLR: return TLR.parseResponseDataPacket(response, response.length);
            case ABECS.TLE: return TLE.parseResponseDataPacket(response, response.length);

            case ABECS.GIX: case ABECS.CLX:
            case ABECS.CEX: case ABECS.EBX: case ABECS.GCD: case ABECS.GTK: case ABECS.MNU:
            case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                return CMD.parseResponseDataPacket(response, response.length);

            default:
                /* Nothing to do */
                break;
        }

        throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
    }

    public static Bundle parseTLV(byte[] input, int length) {
        Log.d(TAG, "parseTLV::input.length [" + input.length + "] length [" + length + "]");

        // 2022-06-07: ABECS doesn't strictly follows EMV v4.3 Book 3, Annex B (page 155)

        Bundle response = new Bundle();

        int cursor    = 0;
        int threshold = 0;

        while ((cursor += threshold) < length) {
            byte[] T = new byte[4];
            byte[] L = new byte[4];

            System.arraycopy(input, cursor, T, 2, 2);

            cursor += 2;

            int tag = ByteBuffer.wrap(T).getInt();

            System.arraycopy(input, cursor, L, 2, 2);

            cursor += 2;

            threshold = ByteBuffer.wrap(L).getInt();

            byte[] V = new byte[threshold];

            System.arraycopy(input, cursor, V, 0, threshold);

            switch (tag) {
                case 0x0002: response.putString(ABECS.SPE_MTHDPIN,    new String(V)); break;
                case 0x0003: response.putString(ABECS.SPE_MTHDDAT,    new String(V)); break;
                case 0x0006: response.putString(ABECS.SPE_CEXOPT,     new String(V)); break;
                case 0x0007: response.putString(ABECS.SPE_TRACKS,     new String(V)); break;
                case 0x0008: response.putString(ABECS.SPE_OPNDIG,     new String(V)); break;
                case 0x0009: response.putString(ABECS.SPE_KEYIDX,     new String(V)); break;
                case 0x0010: response.putString(ABECS.SPE_ACQREF,     new String(V)); break;
                case 0x0011: response.putString(ABECS.SPE_APPTYPE,    new String(V)); break;
                case 0x0012: response.putString(ABECS.SPE_AIDLIST,    new String(V)); break;
                case 0x0013: response.putString(ABECS.SPE_AMOUNT,     new String(V)); break;
                case 0x0014: response.putString(ABECS.SPE_CASHBACK,   new String(V)); break;
                case 0x0015: response.putString(ABECS.SPE_TRNDATE,    new String(V)); break;
                case 0x0016: response.putString(ABECS.SPE_TRNTIME,    new String(V)); break;
                case 0x0017: response.putString(ABECS.SPE_GCXOPT,     new String(V)); break;
                case 0x0018: response.putString(ABECS.SPE_GOXOPT,     new String(V)); break;
                case 0x0019: response.putString(ABECS.SPE_FCXOPT,     new String(V)); break;
                case 0x001B: response.putString(ABECS.SPE_DSPMSG,     new String(V)); break;
                case 0x001C: response.putString(ABECS.SPE_ARC,        new String(V)); break;
                case 0x001E: response.putString(ABECS.SPE_MFNAME,     new String(V)); break;

                case 0x0020:
                    ArrayList<String> list = response.getStringArrayList(ABECS.SPE_MNUOPT);

                    if (list == null) {
                        list = new ArrayList<>(0);
                    }

                    list.add(new String(V));

                    response.putStringArrayList(ABECS.SPE_MNUOPT, list);
                    break;

                case 0x0022: response.putString(ABECS.SPE_TRNCURR,    new String(V)); break;
                case 0x0023: response.putString(ABECS.SPE_PANMASK,    new String(V)); break;

                case 0x0001: response.putString(ABECS.SPE_IDLIST,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0004: response.putString(ABECS.SPE_TAGLIST,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0005: response.putString(ABECS.SPE_EMVDATA,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000A: response.putString(ABECS.SPE_WKENC,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000B: response.putString(ABECS.SPE_MSGIDX,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000C: response.putString(ABECS.SPE_TIMEOUT,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000D: response.putString(ABECS.SPE_MINDIG,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000E: response.putString(ABECS.SPE_MAXDIG,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x000F: response.putString(ABECS.SPE_DATAIN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x001A: response.putString(ABECS.SPE_TRMPAR,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x001D: response.putString(ABECS.SPE_IVCBC,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0021: response.putString(ABECS.SPE_TRNTYPE,    DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0024: response.putString(ABECS.SPE_PBKMOD,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x0025: response.putString(ABECS.SPE_PBKEXP,     DataUtility.getHexStringFromByteArray(V)); break;

                case 0x8001: response.putString(ABECS.PP_SERNUM,      new String(V)); break;
                case 0x8002: response.putString(ABECS.PP_PARTNBR,     new String(V)); break;
                case 0x8003: response.putString(ABECS.PP_MODEL,       new String(V)); break;
                case 0x8004: response.putString(ABECS.PP_MNNAME,      new String(V)); break;
                case 0x8005: response.putString(ABECS.PP_CAPAB,       new String(V)); break;
                case 0x8006: response.putString(ABECS.PP_SOVER,       new String(V)); break;
                case 0x8007: response.putString(ABECS.PP_SPECVER,     new String(V)); break;
                case 0x8008: response.putString(ABECS.PP_MANVERS,     new String(V)); break;
                case 0x8009: response.putString(ABECS.PP_APPVERS,     new String(V)); break;
                case 0x800A: response.putString(ABECS.PP_GENVERS,     new String(V)); break;
                case 0x8010: response.putString(ABECS.PP_KRNLVER,     new String(V)); break;
                case 0x8011: response.putString(ABECS.PP_CTLSVER,     new String(V)); break;
                case 0x8012: response.putString(ABECS.PP_MCTLSVER,    new String(V)); break;
                case 0x8013: response.putString(ABECS.PP_VCTLSVER,    new String(V)); break;
                case 0x8014: response.putString(ABECS.PP_AECTLSVER,   new String(V)); break;
                case 0x8015: response.putString(ABECS.PP_DPCTLSVER,   new String(V)); break;
                case 0x8016: response.putString(ABECS.PP_PUREVER,     new String(V)); break;
                case 0x8032: response.putString(ABECS.PP_MKTDESP,     new String(V)); break;
                case 0x8033: response.putString(ABECS.PP_MKTDESD,     new String(V)); break;
                case 0x8035: response.putString(ABECS.PP_DKPTTDESP,   new String(V)); break;
                case 0x8036: response.putString(ABECS.PP_DKPTTDESD,   new String(V)); break;
                case 0x8040: response.putString(ABECS.PP_EVENT,       new String(V)); break;
                case 0x8041: response.putString(ABECS.PP_TRK1INC,     new String(V)); break;
                case 0x8042: response.putString(ABECS.PP_TRK2INC,     new String(V)); break;
                case 0x8043: response.putString(ABECS.PP_TRK3INC,     new String(V)); break;

                case 0x8044:
                    int i; // TODO: lazy detection; must be improved

                    for (i = 0; i < V.length; i++) {
                        if (V[i] < 0x20 || V[i] > 0x7E) {
                            break;
                        }
                    }

                    response.putString(ABECS.PP_TRACK1, (i < V.length) ? DataUtility.getHexStringFromByteArray(V) : new String(V));
                    break;

                case 0x804D: response.putString(ABECS.PP_VALUE,       new String(V)); break;
                case 0x804F: response.putString(ABECS.PP_CARDTYPE,    new String(V)); break;
                case 0x8050: response.putString(ABECS.PP_ICCSTAT,     new String(V)); break;
                case 0x8051: response.putString(ABECS.PP_AIDTABINFO,  new String(V)); break;
                case 0x8052: response.putString(ABECS.PP_PAN,         new String(V)); break;
                case 0x8053: response.putString(ABECS.PP_PANSEQNO,    new String(V)); break;
                case 0x8055: response.putString(ABECS.PP_CHNAME,      new String(V)); break;
                case 0x8056: response.putString(ABECS.PP_GOXRES,      new String(V)); break;
                case 0x8058: response.putString(ABECS.PP_FCXRES,      new String(V)); break;
                case 0x805B: response.putString(ABECS.PP_LABEL,       new String(V)); break;
                case 0x805C: response.putString(ABECS.PP_ISSCNTRY,    new String(V)); break;
                case 0x805D: response.putString(ABECS.PP_CARDEXP,     new String(V)); break;
                case 0x8060: response.putString(ABECS.PP_DEVTYPE,     new String(V)); break;

                case 0x8045: response.putString(ABECS.PP_TRACK2,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8046: response.putString(ABECS.PP_TRACK3,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8047: response.putString(ABECS.PP_TRK1KSN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8048: response.putString(ABECS.PP_TRK2KSN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8049: response.putString(ABECS.PP_TRK3KSN,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804A: response.putString(ABECS.PP_ENCPAN,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804B: response.putString(ABECS.PP_ENCPANKSN,   DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804C: response.putString(ABECS.PP_KSN,         DataUtility.getHexStringFromByteArray(V)); break;
                case 0x804E: response.putString(ABECS.PP_DATAOUT,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8054: response.putString(ABECS.PP_EMVDATA,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8057: response.putString(ABECS.PP_PINBLK,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8059: response.putString(ABECS.PP_ISRESULTS,   DataUtility.getHexStringFromByteArray(V)); break;
                case 0x805A: response.putString(ABECS.PP_BIGRAND,     DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8062: response.putString(ABECS.PP_TLRMEM,      DataUtility.getHexStringFromByteArray(V)); break;
                case 0x8063: response.putString(ABECS.PP_ENCKRAND,    DataUtility.getHexStringFromByteArray(V)); break;

                case 0x001F: /* SPE_MFINFO  */
                case 0x8020: /* PP_DSPTXTSZ */
                case 0x8021: /* PP_DSPGRSZ  */
                case 0x8022: /* PP_MFSUP    */
                case 0x805E: /* PP_MFNAME   */
                    /* 2021-12-14: out-of-scope */
                    break;

                default:
                    String key;

                    if (tag >= 0x9100 && tag <= 0x9163) {
                        key = ABECS.PP_KSNTDESPnn.replace("nn", String.format(US, "%02d", (tag - 0x9100)));

                        response.putString(key, DataUtility.getHexStringFromByteArray(V));
                    } else if (tag >= 0x9200 && tag <= 0x9263) {
                        key = ABECS.PP_KSNTDESDnn.replace("nn", String.format(US, "%02d", (tag - 0x9200)));

                        response.putString(key, DataUtility.getHexStringFromByteArray(V));
                    } else if (tag >= 0x9300 && tag <= 0x9363) {
                        key = ABECS.PP_TABVERnn  .replace("nn", String.format(US, "%02d", (tag - 0x9300)));

                        response.putString(key, new String(V));
                    } else {
                        key = DataUtility.getHexStringFromByteArray(T, T.length, 2);

                        response.putString(key, DataUtility.getHexStringFromByteArray(V));
                    }
                    break;
            }

            Arrays.fill(T, (byte) 0x00);
            Arrays.fill(L, (byte) 0x00);
            Arrays.fill(V, (byte) 0x00);
        }

        return response;
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket::input [" + input + "]");

        byte[] request = null;

        String CMD_ID = input.getString(ABECS.CMD_ID, "UNKNOWN");

        // 2022-06-07: to enhance bug tracking and analysis,
        // `CMD#buildRequestDataPacket(Bundle)` is ditched in favor of each
        // command specific implementation

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
                return _wrapDataPacket(request, request.length);
            }

            throw new RuntimeException("CMD_ID [" + CMD_ID + "] packet exceeds maximum length (2048)");
        } else {
            throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
        }
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket::input [" + input + "]");

        byte[] response = null;

        String RSP_ID = input.getString(ABECS.RSP_ID, "UNKNOWN");

        // 2022-06-07: to enhance bug tracking and analysis,
        // `CMD#buildResponseDataPacket(Bundle)` is ditched in favor of each
        // command specific implementation

        switch (RSP_ID) {
            case ABECS.OPN: response = OPN.buildResponseDataPacket(input); break;
            case ABECS.GIX: response = GIX.buildResponseDataPacket(input); break;
            case ABECS.CLX: response = CLX.buildResponseDataPacket(input); break;

            case ABECS.CEX: response = CEX.buildResponseDataPacket(input); break;
            case ABECS.CHP: response = CHP.buildResponseDataPacket(input); break;
            case ABECS.EBX: response = EBX.buildResponseDataPacket(input); break;
            case ABECS.GCD: response = GCD.buildResponseDataPacket(input); break;
            case ABECS.GPN: response = GPN.buildResponseDataPacket(input); break;
            case ABECS.GTK: response = GTK.buildResponseDataPacket(input); break;
            case ABECS.MNU: response = MNU.buildResponseDataPacket(input); break;
            case ABECS.RMC: response = RMC.buildResponseDataPacket(input); break;

            case ABECS.TLI: response = TLI.buildResponseDataPacket(input); break;
            case ABECS.TLR: response = TLR.buildResponseDataPacket(input); break;
            case ABECS.TLE: response = TLE.buildResponseDataPacket(input); break;

            case ABECS.GCX: response = GCX.buildResponseDataPacket(input); break;
            case ABECS.GED: response = GED.buildResponseDataPacket(input); break;
            case ABECS.GOX: response = GOX.buildResponseDataPacket(input); break;
            case ABECS.FCX: response = FCX.buildResponseDataPacket(input); break;

            default:
                /* Nothing to do */
                break;
        }

        if (response != null) {
            if (response.length <= 2048) {
                return _wrapDataPacket(response, response.length);
            }

            throw new RuntimeException("RSP_ID [" + RSP_ID + "] packet exceeds maximum length (2048)");
        } else {
            throw new RuntimeException("Unknown or unhandled RSP_ID [" + RSP_ID + "]");
        }
    }

    public static byte[] buildTLV(@NotNull String name, @NotNull String value)
            throws Exception {
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
                return _buildTLV(ABECS.TYPE.S, "0020", value);
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

    public static int getIntFromDigitsArray(byte[] input, int length) {
        Log.d(TAG, "getIntFromDigitsArray::input.length [" + input.length + "] length [" + length + "]");

        int response = 0;

        if (input.length >= length) {
            for (int i = length - 1, j = 0; i >= 0; i--, j++) {
                if (input[j] < 0x30 || input[j] > 0x39) {
                    String message = String.format(US, "getIntFromDigitsArray::input[%d] [%02X]", j, input[j]);

                    throw new IllegalArgumentException(message);
                }

                response += (input[j] - 0x30) * ((i > 0) ? (Math.pow(10, i)) : 1);
            }
        } else {
            String message = String.format(US, "getIntFromDigitsArray::input.length [%d] length [%d]", input.length, length);

            throw new IllegalArgumentException(message);
        }

        return response;
    }
}
