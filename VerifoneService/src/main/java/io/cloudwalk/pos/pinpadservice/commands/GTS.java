package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.Locale;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class GTS {
    private static final String TAG = GTS.class.getSimpleName();

    public static Bundle gts(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        int GTS_ACQIDX  = input.getInt(ABECS.GTS_ACQIDX, -1);

        Bundle response = new Bundle();

        if (GTS_ACQIDX >= 0 && GTS_ACQIDX < 100) {
            response = GIX.gix(input);
        }

        response.putString(ABECS.RSP_ID, ABECS.GTS);

        Bundle output = new Bundle();

        output.putString  (ABECS.RSP_ID,   response.getString(ABECS.RSP_ID));
        output.putInt     (ABECS.RSP_STAT, response.getInt   (ABECS.RSP_STAT, ABECS.STAT.ST_INVPARM.ordinal()));

        try {
            if (output.getInt(ABECS.RSP_STAT) != ABECS.STAT.ST_OK.ordinal()) {
                return response;
            }

            String key = ABECS.PP_TABVERnn
                    .replace("nn", String.format(Locale.getDefault(), "%02d", GTS_ACQIDX));

            output.putString(ABECS.GTS_TABVER, response.getString(key));
        } finally {
            Log.d(TAG, ABECS.GTS + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");
        }

        return output;
    }
}
