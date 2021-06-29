package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class TLI {
    private static final String TAG_LOGCAT = TLI.class.getSimpleName();

    private static String TLI_TABVER = "0000000000";

    private static int TLI_ACQIDX = 0;

    private static void setTLI_ACQIDX(int input) {
        TLI_ACQIDX = input;
    }

    private static void setTLI_TABVER(String input) {
        TLI_TABVER = input;
    }

    public static Bundle tli(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        final Bundle output = new Bundle();

        setTLI_ACQIDX(input.getInt   (ABECS.TLI_ACQIDX, 0));
        setTLI_TABVER(input.getString(ABECS.TLI_TABVER, "0000000000"));

        output.putString(ABECS.RSP_ID,   ABECS.TLI);
        output.putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

        Log.d(TAG_LOGCAT, ABECS.TLI + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output;
    }

    public static int getTLI_ACQIDX() {
        return TLI_ACQIDX;
    }

    public static String getTLI_TABVER() {
        return TLI_TABVER;
    }
}
