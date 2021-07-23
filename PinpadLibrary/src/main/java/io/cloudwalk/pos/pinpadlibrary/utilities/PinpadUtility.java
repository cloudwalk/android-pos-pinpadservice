package io.cloudwalk.pos.pinpadlibrary.utilities;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.cloudwalk.pos.loglibrary.Log;
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
}
