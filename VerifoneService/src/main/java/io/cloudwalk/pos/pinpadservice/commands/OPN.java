package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class OPN {
    private static final String TAG = OPN.class.getSimpleName();

    public static Bundle opn(Bundle input)
            throws Exception {
        final long overhead = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        long[] timestamp = { SystemClock.elapsedRealtime() };

        // TODO: ...

        timestamp[0] = SystemClock.elapsedRealtime() - timestamp[0];

        // TODO: ...

        output[0].putString         (ABECS.RSP_ID,   ABECS.OPN);
        output[0].putSerializable   (ABECS.RSP_STAT, ABECS.STAT.ST_INTERR);

        semaphore[0].release();

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG, ABECS.OPN + "::timestamp [" + timestamp[0] + "ms] [" + ((SystemClock.elapsedRealtime() - overhead) - timestamp[0]) + "ms]");

        return output[0];
    }
}
