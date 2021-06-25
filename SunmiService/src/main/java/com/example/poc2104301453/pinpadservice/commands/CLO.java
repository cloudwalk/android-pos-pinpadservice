package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;

import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;

public class CLO {
    private static final String TAG_LOGCAT = CLO.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    public static Bundle clo(Bundle input)
            throws Exception {
        final Bundle output = new Bundle();

        output.putString(ABECS.RSP_ID,   ABECS.CLO);
        output.putInt   (ABECS.RSP_STAT, ABECS.STAT.ST_OK.ordinal());

        return output;
    }
}
