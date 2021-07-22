package io.cloudwalk.pos.pinpadlibrary.utilities;

import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    private static byte[] wrap(byte[] input) {
        Log.d(TAG, "wrap");

        byte[] pkt = new byte[2044 + 4];

        pkt[0] = 0x16; /* PKTSTART */

        int length = Math.min(input.length, 2044 + 4);

        int j = 1;

        for (int i = 0; i < length; i++) {
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

        byte[] crc = new byte[input.length + 1];

        System.arraycopy(input, 0, crc, 0, input.length);

        crc[input.length] = pkt[j];

        crc = DataUtility.CRC16_XMODEM(crc);

        System.arraycopy(crc, 0, pkt, j + 1, crc.length);

        pkt = Arrays.copyOf(pkt, j + 1 + crc.length);

        return pkt;
    }

    /**
     *
     * @param input
     * @return
     */
    public static byte[] build(@NotNull Bundle input) {
        Log.d(TAG, "build");

        // TODO: build(Bundle); // enforcing <<CAN>>

        byte[] cmd = "OPN".getBytes(UTF_8);

        return wrap(cmd);
    }

    /**
     *
     * @param input
     * @return
     */
    public static Bundle parse(byte[] input) {
        Log.d(TAG, "parse");

        // TODO: parse(byte[])

        return new Bundle();
    }

    /**
     *
     * @param input
     * @param length
     */
    public static void trace(byte[] input, int length) {
        StringBuilder[] msg = { new StringBuilder(), new StringBuilder() };

        for (int i = 0; i < length; i++) {
            byte b = input[i];

            msg[0].append(String.format("%02X ", b));

            msg[1].append((b > 0x20 && b < 0x7F) ? (char) b : '.');

            if ((msg[1].length() % 16) != 0 && (i + 1) >= length) {
                int ceil;

                ceil = 48 - (msg[0].length() % 48);

                for (int j = 0; j < ceil; j += 3) {
                    msg[0].append(".. ");
                }

                ceil = 16 - (msg[1].length() % 16);

                for (int j = 0; j < ceil; j++) {
                    msg[1].append(".");
                }
            }

            if ((i > 0 && msg[1].length() % 16 == 0) || (i + 1) >= length) {
                msg[0].append(msg[1]);

                Log.d(TAG, "\r\n" + msg[0].toString());

                msg[0].delete(0, msg[0].length());

                msg[1].delete(0, msg[1].length());
            }
        }
    }
}
