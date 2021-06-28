package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;

import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class GIN {
    private static final String TAG_LOGCAT = GIN.class.getSimpleName();

    public static Bundle gin(Bundle input)
            throws Exception {
        Bundle response = GIX.gix(input);

        response.putString(ABECS.RSP_ID, ABECS.GIN);

        Bundle output = new Bundle();

        output.putString  (ABECS.RSP_ID,   response.getString(ABECS.RSP_ID));
        output.putInt     (ABECS.RSP_STAT, response.getInt   (ABECS.RSP_STAT, ABECS.STAT.ST_INTERR.ordinal()));

        if (output.getInt(ABECS.RSP_STAT) != ABECS.STAT.ST_OK.ordinal()) {
            return response;
        }

        String GIN_MNAME    = response.getString(ABECS.PP_MNNAME);
        String GIN_MODEL    = response.getString(ABECS.PP_MODEL);
        String GIN_CTLSSUP  = response.getString(ABECS.PP_CAPAB).charAt(0) != '1' ? " " : "C";
        String GIN_SOVER    = response.getString(ABECS.PP_SOVER);
        String GIN_SPECVER  = response.getString(ABECS.PP_SPECVER);
        String GIN_MANVER   = response.getString(ABECS.PP_MANVERS);
        String GIN_SERNUM   = response.getString(ABECS.PP_SERNUM);

        String GIN_ACQNAM   = "ABECS";

        String GIN_KRNLVER  = response.getString(ABECS.PP_KRNLVER);
        String GIN_APPVERS  = response.getString(ABECS.PP_APPVERS);
        String GIN_CTLSVER  = response.getString(ABECS.PP_CTLSVER);
        String GIN_MCTLSVER = response.getString(ABECS.PP_MCTLSVER);
        String GIN_VCTLSVER = response.getString(ABECS.PP_VCTLSVER);

        int    GIN_ACQIDX   = input.getInt(ABECS.GIN_ACQIDX);

        switch (GIN_ACQIDX) {
            case 0:
                output.putString(ABECS.GIN_MNAME,       GIN_MNAME);
                output.putString(ABECS.GIN_MODEL,       GIN_MODEL);
                output.putString(ABECS.GIN_CTLSSUP,     GIN_CTLSSUP);
                output.putString(ABECS.GIN_SOVER,       GIN_SOVER);
                output.putString(ABECS.GIN_SPECVER,     GIN_SPECVER);
                output.putString(ABECS.GIN_MANVER,      GIN_MANVER);
                output.putString(ABECS.GIN_SERNUM,      GIN_SERNUM);
                break;

            case 2:
                output.putString(ABECS.GIN_ACQNAM,      GIN_ACQNAM);
                output.putString(ABECS.GIN_KRNLVER,     GIN_KRNLVER);
                output.putString(ABECS.GIN_APPVERS,     GIN_APPVERS);
                output.putString(ABECS.GIN_SPECVER,     GIN_SPECVER);
                break;

            case 3:
                output.putString(ABECS.GIN_ACQNAM,      GIN_ACQNAM);
                output.putString(ABECS.GIN_KRNLVER,     GIN_KRNLVER);
                output.putString(ABECS.GIN_CTLSVER,     GIN_CTLSVER);
                output.putString(ABECS.GIN_MCTLSVER,    GIN_MCTLSVER);
                output.putString(ABECS.GIN_VCTLSVER,    GIN_VCTLSVER);
                output.putString(ABECS.GIN_APPVERS,     GIN_APPVERS);
                output.putString(ABECS.GIN_SPECVER,     GIN_SPECVER);

                String GIN_DUKPT = response.getString(ABECS.PP_DKPTTDESP, "00000...").charAt(1) != '1' ? " " : "T";

                output.putString(ABECS.GIN_DUKPT,       GIN_DUKPT);
                break;

            default:
                output.putString(ABECS.GIN_ACQNAM,      GIN_ACQNAM);
                output.putString(ABECS.GIN_APPVERS,     GIN_APPVERS);
                break;
        }

        return output;
    }
}
