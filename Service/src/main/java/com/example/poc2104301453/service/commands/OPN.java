package com.example.poc2104301453.service.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.library.ABECS;
import com.example.poc2104301453.library.exceptions.*;
import com.example.poc2104301453.service.utilities.ServiceUtility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import br.com.verifone.bibliotecapinpad.AcessoDiretoPinpad;

public class OPN {
    private static final String TAG_LOGCAT = OPN.class.getSimpleName();

    private static AcessoDiretoPinpad sPinpad = ServiceUtility.getPinpad();

    public static Bundle opn(Bundle input)
            throws Exception {
        String CMD_ID  = input.getString(ABECS.KEY_ENUM.REQUEST.getValue());
        String OPN_MOD = input.getString("OPN_MOD");
        String OPN_EXP = input.getString("OPN_EXP");

        Log.d(TAG_LOGCAT, "opn::CMD_ID [" + CMD_ID + "]");

        if (OPN_MOD != null || OPN_EXP != null) {
            if (OPN_MOD == null || OPN_EXP == null) {
                throw new Exception("Mandatory key(s) \"" + "OPN_MOD and/or OPN_EXP" + "\" not found");
            } else {
                throw new PendingDevelopmentException("OPN ABECS pending development");
            }
        }

        byte    PKTSTART = 0x16;
        byte[]  PKTDATA  = new byte[2048];
        byte    PKTSTOP  = 0x17;
        byte[]  PKTCRC   = new byte[1024];

        PKTDATA[0] = 'O';
        PKTDATA[1] = 'P';
        PKTDATA[2] = 'N';
        PKTDATA[3] = 0x00;
        PKTDATA[4] = 0x00;
        PKTDATA[5] = 0x00;

        PKTCRC = ServiceUtility.generateCRC(PKTDATA);

        byte[] CMD = new byte[4096];

        int i = 0;
        int j = 0;

        CMD[i] = PKTSTART;

        for (i = 1, j = 0; j < PKTDATA.length; i++, j++) {
            if (PKTDATA[j] == 0x13) {
                CMD[i++] = 0x13; CMD[i] = 0x33;
                continue;
            }

            if (PKTDATA[j] == 0x16) {
                CMD[i++] = 0x13; CMD[i] = 0x36;
                continue;
            }

            if (PKTDATA[j] == 0x17) {
                CMD[i++] = 0x13; CMD[i] = 0x37;
                continue;
            }

            CMD[i] = PKTDATA[j];
        }

        CMD[i] = PKTSTOP;

        for (j = 0; j < PKTCRC.length; j++) {
            CMD[i++] = PKTCRC[j];
        }

        byte[] RSP = { 0x00 };

        sPinpad.enviaComando(CMD, CMD.length);

        StringBuilder msg = new StringBuilder();

        sPinpad.recebeResposta(RSP, 2000);

        msg.append("abort::recebeResposta(RSP, 2000)\r\n\t - RSP [").append(ServiceUtility.bytesToHex(RSP)).append("]");

        Log.d(TAG_LOGCAT, msg.toString());

        Exception exception = new Exception("Invalid response: "+ ServiceUtility.bytesToHex(RSP));

        if (RSP.length < 6) {
            throw exception;
        }

        for (i = 0; i < 3; i++) {
            if (RSP[i] != CMD[i]) {
                throw exception;
            }
        }

        int status = RSP[3] << 16 & 0x00FF0000 |
                     RSP[4] <<  8 & 0x0000FF00 |
                     RSP[5] <<  0 & 0x000000FF;

        Bundle output = new Bundle();

        output.putInt(ABECS.KEY_ENUM.STATUS.getValue(), status);

        return output;
    }
}
