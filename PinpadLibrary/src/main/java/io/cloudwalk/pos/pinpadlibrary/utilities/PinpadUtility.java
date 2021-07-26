package io.cloudwalk.pos.pinpadlibrary.utilities;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.commands.CLO;
import io.cloudwalk.pos.pinpadlibrary.commands.CLX;
import io.cloudwalk.pos.pinpadlibrary.commands.OPN;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.util.Locale.US;

public class PinpadUtility {
    private static final String TAG = PinpadUtility.class.getSimpleName();

    private PinpadUtility() {
        Log.d(TAG, "PinpadUtility");

        /* Nothing to do */
    }

    /**
     *
     * @param input
     * @return
     */
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

        // TODO: validate CRC (exception?)

        byte[] output = new byte[j];

        System.arraycopy(pkt, 0, output, 0, j);

        return output;
    }

    /**
     *
     * @param input
     * @return
     */
    public static byte[] buildDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildDataPacket");

        byte[] request = null;

        String CMD_ID = input.getString(ABECS.CMD_ID, "UNKNOWN");

        switch (CMD_ID) {
            case ABECS.OPN: request = OPN.buildDataPacket(input); break;
            case ABECS.GIN: break;
            case ABECS.GIX: break;

            case "DWK": // TODO: case ABECS.DWK:
                break;

            case ABECS.CLO: request = CLO.buildDataPacket(input); break;
            case ABECS.CLX: request = CLX.buildDataPacket(input); break;

            default:
                /* Nothing to do */
                break;
        }

        if (request != null) {
            return wrapDataPacket(request, request.length);
        }

        throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
    }

    /**
     *
     * @param input
     * @return
     */
    public static Bundle parseDataPacket(byte[] input, int length)
            throws Exception {
        Log.d(TAG, "parseDataPacket");

        byte[] response = unwrapDataPacket(input, length);

        String CMD_ID = String.format(US, "%c%c%c", response[0], response[1], response[2]);

        switch (CMD_ID) {
            case ABECS.OPN: return OPN.parseDataPacket(response, response.length);
            case ABECS.GIN: break;
            case ABECS.GIX: break;

            case "DWK": // TODO: case ABECS.DWK:
                break;

            case ABECS.CLO: return CLO.parseDataPacket(response, response.length);
            case ABECS.CLX: return CLX.parseDataPacket(response, response.length);

            default:
                /* Nothing to do */
                break;
        }

        throw new RuntimeException("Unknown or unhandled CMD_ID [" + CMD_ID + "]");
    }
}
