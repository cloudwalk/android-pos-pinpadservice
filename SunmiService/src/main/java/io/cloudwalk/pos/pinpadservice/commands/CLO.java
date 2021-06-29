package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;

import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;

public class CLO {
    private static final String TAG_LOGCAT = CLO.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    public static Bundle clo(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        final Bundle output = new Bundle();

        /* 2021-06-29: BCPP 1.20 from Sunmi is crashing due to a faulty address when CLO is
         * triggered */

        output.putString(ABECS.RSP_ID,   ABECS.CLO);
        output.putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

        Log.d(TAG_LOGCAT, ABECS.CLO + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output;
    }
}
