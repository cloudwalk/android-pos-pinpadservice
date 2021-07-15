package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.Locale;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class GCR {
    private static final String TAG = GCR.class.getSimpleName();

    public static Bundle gcr(Bundle input)
            throws Exception {
        final long timestamp = SystemClock.elapsedRealtime();

        Bundle request = new Bundle();

        request.putString(ABECS.CMD_ID,      ABECS.GCR);

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

        response.putString(ABECS.RSP_ID, ABECS.GCR);

        Bundle output = new Bundle();

        output.putString  (ABECS.RSP_ID,   response.getString(ABECS.RSP_ID));
        output.putInt     (ABECS.RSP_STAT, response.getInt   (ABECS.RSP_STAT, ABECS.STAT.ST_INTERR.ordinal()));

        try {
            if (output.getInt(ABECS.RSP_STAT) != ABECS.STAT.ST_OK.ordinal()) {
                return response;
            }

            output.putInt(ABECS.GCR_CARDTYPE, response.getInt(ABECS.PP_CARDTYPE));
            output.putInt(ABECS.GCR_STATCHIP, response.getInt(ABECS.PP_ICCSTAT, 0));

            String GCR_ACQIDX  = response.getString(ABECS.PP_AIDTABINFO, "");
            String GCR_RECIDX  = GCR_ACQIDX;
            String GCR_APPTYPE = GCR_RECIDX;

            if (!GCR_APPTYPE.isEmpty()) {
                GCR_ACQIDX  = GCR_ACQIDX .substring(0, 2);
                GCR_RECIDX  = GCR_RECIDX .substring(2, 4);
                GCR_APPTYPE = GCR_APPTYPE.substring(4, 6);
            }

            output.putString(ABECS.GCR_ACQIDX,  GCR_ACQIDX);
            output.putString(ABECS.GCR_RECIDX,  GCR_RECIDX);
            output.putString(ABECS.GCR_APPTYPE, GCR_APPTYPE);

            output.putString(ABECS.GCR_TRK1,     response.getString(ABECS.PP_TRK1INC,   ""));
            output.putString(ABECS.GCR_TRK2,     response.getString(ABECS.PP_TRK2INC,   ""));
            output.putString(ABECS.GCR_TRK3,     response.getString(ABECS.PP_TRK3INC,   ""));
            output.putString(ABECS.GCR_PAN,      response.getString(ABECS.PP_PAN,       ""));
            output.putInt   (ABECS.GCR_PANSEQNO, response.getInt(ABECS.PP_PANSEQNO,     -1));
            output.putString(ABECS.GCR_APPLABEL, response.getString(ABECS.PP_LABEL,     ""));
            output.putString(ABECS.GCR_CHNAME,   response.getString(ABECS.PP_CHNAME,    ""));
            output.putInt   (ABECS.GCR_CARDEXP,  response.getInt(ABECS.PP_CARDEXP));
            output.putInt   (ABECS.GCR_ISSCNTRY, response.getInt(ABECS.PP_ISSCNTRY));

            // TODO: ABECS.GCR_SRVCODE and ABECS.GCR_ACQRD
        } finally {
            Log.d(TAG, ABECS.GCR + "::timestamp [" + (SystemClock.elapsedRealtime() - timestamp) + "ms]");
        }

        return output;
    }
}
