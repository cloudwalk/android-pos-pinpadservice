package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;

public class CLO {
    private static final String TAG_LOGCAT = CLO.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    public static Bundle clo(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        /* 2021-06-29: BCPP 1.20 from Sunmi is crashing due to a faulty address when CLO is
         * triggered: S/N PB0419AL60448 */

        // final Bundle[] output = { new Bundle() };
        // final Semaphore[] semaphore = { new Semaphore(0, true) };

        // getPinpad().close(() -> {
        //     output[0].putString(ABECS.RSP_ID,   ABECS.CLO);
        //     output[0].putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

        //     semaphore[0].release();
        // });

        // semaphore[0].acquireUninterruptibly();

        // return output[0];

        final Bundle output = new Bundle();

        output.putString(ABECS.RSP_ID,   ABECS.CLO);
        output.putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

        Log.d(TAG_LOGCAT, ABECS.CLO + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output;
    }
}
