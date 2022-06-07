package io.cloudwalk.pos.pinpadlibrary.internals.commands;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class GIX {
    private static final String
            TAG = GIX.class.getSimpleName();

    private GIX() {
        Log.d(TAG, "GIX");

        /* Nothing to do */
    }

    public static byte[] buildRequestDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildRequestDataPacket");

        List<String> list = new ArrayList<>(0);

        list.add(ABECS.SPE_IDLIST);

        return PinpadUtility.CMD.buildRequestDataPacket(input, list);
    }

    public static byte[] buildResponseDataPacket(@NotNull Bundle input)
            throws Exception {
        Log.d(TAG, "buildResponseDataPacket");

        List<List<String>> list = new ArrayList<>(0);

        for (int i = 0; i < 4; i++) {
            list.add(new ArrayList<>(0));
        }

        list.get(0).add(ABECS.PP_SERNUM);
        list.get(0).add(ABECS.PP_PARTNBR);
        list.get(0).add(ABECS.PP_MODEL);
        list.get(0).add(ABECS.PP_MNNAME);
        list.get(0).add(ABECS.PP_CAPAB);
        list.get(0).add(ABECS.PP_SOVER);
        list.get(0).add(ABECS.PP_SPECVER);
        list.get(0).add(ABECS.PP_MANVERS);
        list.get(0).add(ABECS.PP_APPVERS);
        list.get(0).add(ABECS.PP_GENVERS);
        list.get(0).add(ABECS.PP_KRNLVER);
        list.get(0).add(ABECS.PP_CTLSVER);
        list.get(0).add(ABECS.PP_MCTLSVER);
        list.get(0).add(ABECS.PP_VCTLSVER);
        list.get(0).add(ABECS.PP_AECTLSVER);
        list.get(0).add(ABECS.PP_DPCTLSVER);
        list.get(0).add(ABECS.PP_PUREVER);
        list.get(0).add(ABECS.PP_MKTDESP);
        list.get(0).add(ABECS.PP_MKTDESD);
        list.get(0).add(ABECS.PP_DKPTTDESP);
        list.get(0).add(ABECS.PP_DKPTTDESD);

        for (String entry : input.keySet()) {
            if (entry.equals(ABECS.RSP_STAT)) {
                continue;
            }

            if (input.getString(entry)       .startsWith("PP_KSNTDESP")) {
                list.get(1).add(entry);
            } else if (input.getString(entry).startsWith("PP_KSNTDESD")) {
                list.get(2).add(entry);
            } else if (input.getString(entry).startsWith("PP_TABVER")) {
                list.get(3).add(entry);
            }
        }

        for (int i = 1; i < 4; i++) {
            Collections.sort(list.get(i), String::compareTo);

            list.get(0).addAll(list.get(i));
        }

        list.get(0).add(ABECS.PP_BIGRAND);

        return PinpadUtility.CMD.buildResponseDataPacket(input, list.get(0));
    }
}
