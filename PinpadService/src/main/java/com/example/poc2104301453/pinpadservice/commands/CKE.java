package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;

public class CKE {
    private static final String TAG_LOGCAT = CKE.class.getSimpleName();

    public static Bundle cke(Bundle input)
            throws Exception {
        Bundle request = new Bundle();

        request.putString(ABECS.CMD_ID, ABECS.CEX);

        String SPE_CEXOPT = "";

        SPE_CEXOPT += (input.getInt(ABECS.CKE_KEY ) != 0) ? "1" : "0";
        SPE_CEXOPT += (input.getInt(ABECS.CKE_MAG ) != 0) ? "1" : "0";
        SPE_CEXOPT += (input.getInt(ABECS.CKE_ICC ) != 0) ? "1" : "0";
        SPE_CEXOPT += (input.getInt(ABECS.CKE_CTLS) != 0) ? "1" : "0";

        SPE_CEXOPT += "00";

        request.putString (ABECS.SPE_CEXOPT, SPE_CEXOPT);

        Bundle response = CEX.cex(request);

        response.putString(ABECS.RSP_ID, ABECS.CKE);

        Bundle output = new Bundle();

        output.putString  (ABECS.RSP_ID,   response.getString(ABECS.RSP_ID));
        output.putInt     (ABECS.RSP_STAT, response.getInt   (ABECS.RSP_STAT, ABECS.STAT.ST_INTERR.ordinal()));

        if (output.getInt(ABECS.RSP_STAT) != ABECS.STAT.ST_OK.ordinal()) {
            return response;
        }

        switch (response.getString(ABECS.PP_EVENT)) {
            case "00":
                output.putInt(ABECS.CKE_KEYCODE,  0);
                break;

            case "02":
            case "03":
                output.putInt(ABECS.CKE_KEYCODE, -2);
                break;

            case "04":
                output.putInt(ABECS.CKE_KEYCODE,  4);
                break;

            case "05":
                output.putInt(ABECS.CKE_KEYCODE,  5);
                break;

            case "06":
                output.putInt(ABECS.CKE_KEYCODE,  6);
                break;

            case "07":
                output.putInt(ABECS.CKE_KEYCODE,  7);
                break;

            case "08":
                output.putInt(ABECS.CKE_KEYCODE,  8);
                break;

            case "13":
                output.putInt(ABECS.CKE_KEYCODE, 13);
                break;

            case "90":
                String CKE_TRK1 = response.getString(ABECS.PP_TRK1INC);
                String CKE_TRK2 = response.getString(ABECS.PP_TRK2INC);
                String CKE_TRK3 = response.getString(ABECS.PP_TRK3INC);

                CKE_TRK1 = (CKE_TRK1 != null) ? CKE_TRK1 : "";
                CKE_TRK2 = (CKE_TRK2 != null) ? CKE_TRK2 : "";
                CKE_TRK3 = (CKE_TRK3 != null) ? CKE_TRK3 : "";

                if (CKE_TRK1.isEmpty() && CKE_TRK2.isEmpty() && CKE_TRK3.isEmpty()) {
                    output.putInt(ABECS.RSP_STAT, ABECS.STAT.ST_MCDATAERR.ordinal());

                    return output;
                }

                output.putInt   (ABECS.CKE_EVENT, 1);
                output.putString(ABECS.CKE_TRK1,  CKE_TRK1);
                output.putString(ABECS.CKE_TRK2,  CKE_TRK2);
                output.putString(ABECS.CKE_TRK3,  CKE_TRK3);
                break;

            case "91":
                output.putInt(ABECS.CKE_EVENT,    2);
                output.putInt(ABECS.CKE_ICCSTAT,  0);
                break;

            case "92":
                output.putInt(ABECS.CKE_EVENT,    2);
                output.putInt(ABECS.CKE_ICCSTAT,  1);
                break;

            case "93":
                output.putInt(ABECS.CKE_EVENT,    3);
                output.putInt(ABECS.CKE_CTLSSTAT, 0);
                break;

            case "94":
                output.putInt(ABECS.CKE_EVENT,    3);
                output.putInt(ABECS.CKE_CTLSSTAT, 1);
                break;

            default:
                Log.e(TAG_LOGCAT, ABECS.PP_EVENT + " [" + response.getString(ABECS.PP_EVENT) + "]");

                /* Nothing to do */
                break;
        }

        switch (output.getInt(ABECS.CKE_KEYCODE, -1)) {
            case -1:
                break;

            case -2:
                output.remove(ABECS.CKE_KEYCODE);
                /* no break */

            default:
                output.putInt(ABECS.CKE_EVENT,    0);
                break;
        }

        return output;
    }
}
