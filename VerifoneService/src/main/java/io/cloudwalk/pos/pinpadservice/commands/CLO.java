package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;

import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;

public class CLO {
    private static final String TAG_LOGCAT = CLO.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    public static Bundle clo(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        getPinpad().close(() -> {
            output[0].putString(ABECS.RSP_ID,   ABECS.CLO);
            output[0].putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG_LOGCAT, ABECS.CLO + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output[0];
    }
}
