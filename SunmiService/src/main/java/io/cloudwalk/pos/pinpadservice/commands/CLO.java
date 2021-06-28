package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;

import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.PinpadAbstractionLayer;

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
