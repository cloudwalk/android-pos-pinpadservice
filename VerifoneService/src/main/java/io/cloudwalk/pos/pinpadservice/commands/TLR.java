package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class TLR {
    private static final String TAG = TLE.class.getSimpleName();

    private static StringBuilder TLR_DATA = new StringBuilder();

    private static int TLR_NREC = 0;

    public static Bundle tlr(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        final Bundle output = new Bundle();

        int     TLR_NREC = input.getInt   (ABECS.TLR_NREC, -1);
        String  TLR_DATA = input.getString(ABECS.TLR_DATA);

        ABECS.STAT status = ((TLR_NREC > 0) && (TLR_DATA != null)) ? ABECS.STAT.ST_OK : ABECS.STAT.ST_INVPARM;

        switch (status) {
            case ST_OK:
                TLR.TLR_DATA.append(TLR_DATA);

                TLR_NREC += getTLR_NREC();

                setTLR_NREC(TLR_NREC);
                break;


            default:
                /* Nothing to do */
                break;
        }

        output.putString(ABECS.RSP_ID,   ABECS.TLR);
        output.putInt   (ABECS.RSP_STAT, status.ordinal());

        Log.d(TAG, ABECS.TLR + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");

        return output;
    }

    public static StringBuilder getTLR_DATA() {
        return TLR_DATA;
    }

    public static int getTLR_NREC() {
        return TLR_NREC;
    }

    public static void setTLR_DATA(StringBuilder input) {
        TLR_DATA = input;
    }

    public static void setTLR_NREC(int input) {
        TLR_NREC = input;
    }
}
