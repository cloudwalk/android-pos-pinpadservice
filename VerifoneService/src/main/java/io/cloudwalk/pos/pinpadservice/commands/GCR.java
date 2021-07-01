package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.Locale;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class GCR {
    private static final String TAG_LOGCAT = GCR.class.getSimpleName();

    public static Bundle gcr(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        Bundle request = new Bundle();

        request.putString(ABECS.CMD_ID,      ABECS.GCX);
        request.putInt   (ABECS.SPE_ACQREF,  input.getInt   (ABECS.GCR_ACQIDXREQ));
        request.putString(ABECS.SPE_APPTYPE, String.format(Locale.getDefault(), "%02d", input.getInt(ABECS.GCR_APPTYPREQ, 99)));
        request.putLong  (ABECS.SPE_AMOUNT,  input.getLong  (ABECS.GCR_AMOUNT));
        request.putString(ABECS.SPE_TRNDATE, input.getString(ABECS.GCR_DATE));
        request.putString(ABECS.SPE_TRNTIME, input.getString(ABECS.GCR_TIME));
        request.putString(ABECS.GCR_TABVER,  input.getString(ABECS.GCR_TABVER));

        StringBuilder SPE_AIDLIST = new StringBuilder();
        int           GCR_QTDAPP  = input.getInt(ABECS.GCR_QTDAPP, 0);

        int i = 0;

        while (i < GCR_QTDAPP) {
            String GCR_IDAPPnn = ABECS.GCR_IDAPPnn.replace("nn", "" + ++i);

            SPE_AIDLIST.append(input.getString(GCR_IDAPPnn, ""));
        }

        if (SPE_AIDLIST.length() > 0) {
            request.putString(ABECS.SPE_AIDLIST, SPE_AIDLIST.toString());
        }

        String SPE_GCXOPT = (request.getInt(ABECS.GCR_CTLSON, 1) != 0) ? "10000" : "00000";

        request.putString(ABECS.SPE_GCXOPT, SPE_GCXOPT);

        Bundle response = GCX.gcx(request);

        response.putString(ABECS.RSP_ID, ABECS.GCX);

        Bundle output = new Bundle();

        output.putString  (ABECS.RSP_ID,   response.getString(ABECS.RSP_ID));
        output.putInt     (ABECS.RSP_STAT, response.getInt   (ABECS.RSP_STAT, ABECS.STAT.ST_INTERR.ordinal()));

        try {
            if (output.getInt(ABECS.RSP_STAT) != ABECS.STAT.ST_OK.ordinal()) {
                return response;
            }

            /* TODO: translate output from GIX to GCR */
        } finally {
            Log.d(TAG_LOGCAT, ABECS.GCX + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");
        }

        return output;
    }
}
