package io.cloudwalk.pos.demo;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;

public class DEMO_aline {

    private static final String
            TAG = DEMO_aline.class.getSimpleName();

    private DEMO_aline() {
        Log.d(TAG, "DEMO_aline");

        /* Nothing to do */
    }

    public static Bundle GCX() { //done by aline
        Log.d(TAG, "GCX");

        Bundle request = new Bundle();

        request.putString(ABECS.CMD_ID, ABECS.GCX);
        request.putString(ABECS.SPE_TRNTYPE, "00");
        request.putString(ABECS.SPE_ACQREF, "04");
        request.putString(ABECS.SPE_AMOUNT, "000000004400");
        request.putString(ABECS.SPE_TRNCURR, "986");
        request.putString(ABECS.SPE_TRNDATE, "220324");
        request.putString(ABECS.SPE_TRNTIME, "161044");
        request.putString(ABECS.SPE_GCXOPT, "10000");
        request.putString(ABECS.SPE_PANMASK, "0004");
        request.putString(ABECS.SPE_TIMEOUT, "1E");
        request.putString(ABECS.SPE_DSPMSG, "INSIRA OU APROXIME SEU CARTAO");

        return request;
    }

    public static Bundle GOX() { //done by aline
        Log.d(TAG, "GOX");

        Bundle request = new Bundle();

        request.putString(ABECS.CMD_ID, ABECS.GOX);
        request.putString(ABECS.SPE_ACQREF, "04");
        request.putString(ABECS.SPE_TRNTYPE, "00");
        request.putString(ABECS.SPE_AMOUNT, "000000004400");
        request.putString(ABECS.SPE_TRNCURR, "986");
        request.putString(ABECS.SPE_GOXOPT, "01100");
        request.putString(ABECS.SPE_MTHDPIN, "3");
        request.putString(ABECS.SPE_KEYIDX, "10");
        request.putString(ABECS.SPE_DSPMSG, "INSIRA A SENHA");
        request.putString(ABECS.SPE_TIMEOUT, "3C");

        return request;
    }

    public static Bundle FCX() { //done by aline
        Log.d(TAG, "FCX");

        Bundle request = new Bundle();

        request.putString(ABECS.CMD_ID, ABECS.FCX);
        request.putString(ABECS.SPE_FCXOPT, "0000");
        request.putString(ABECS.SPE_ARC, "00");

        return request;
    }
}
