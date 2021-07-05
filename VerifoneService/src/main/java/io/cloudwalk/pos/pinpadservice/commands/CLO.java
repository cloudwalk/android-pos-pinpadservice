package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;

import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;

public class CLO {
    private static final String TAG_LOGCAT = CLO.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadManager.getInstance().getPinpad();
    }

    public static Bundle clo(Bundle input)
            throws Exception {
        final long overhead = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        long[] timestamp = { SystemClock.elapsedRealtime() };

        getPinpad().close(() -> {
            timestamp[0] = SystemClock.elapsedRealtime() - timestamp[0];

            output[0].putString(ABECS.RSP_ID,   ABECS.CLO);
            output[0].putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG_LOGCAT, ABECS.CLO + "::timestamp [" + timestamp[0] + "ms] [" + ((SystemClock.elapsedRealtime() - overhead) - timestamp[0]) + "ms]");

        return output[0];
    }
}
