package io.cloudwalk.pos.pinpadlibrary.utilities;

import android.os.Bundle;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PinpadUtility {
    private static final String TAG = PinpadManager.class.getSimpleName();

    private PinpadUtility() {
        Log.d(TAG, "PinpadUtility");

        /* Nothing to do */
    }

    public static byte[] build(Bundle input) {
        Log.d(TAG, "build");

        byte[] cmd = "OPN".getBytes(UTF_8);

        byte[] pkt = new byte[2044 + 4];

        pkt[0] = 0x16; /* PKTSTART */

        int length = Math.min(cmd.length, 2044 + 4);

        int j = 1;

        for (int i = 0; i < length; i++) {
            switch (cmd[i]) {
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
                    pkt[j++] = cmd[i];
                    break;
            }
        }

        pkt[j] = 0x17; /* PKTSTOP */

        byte[] crc = new byte[cmd.length + 1];

        System.arraycopy(cmd, 0, crc, 0, cmd.length);

        crc[cmd.length] = pkt[j];

        crc = DataUtility.CRC16_XMODEM(crc);

        System.arraycopy(crc, 0, pkt, j + 1, crc.length);

        pkt = DataUtility.trimByteArray(pkt);

        return pkt;
    }
}
