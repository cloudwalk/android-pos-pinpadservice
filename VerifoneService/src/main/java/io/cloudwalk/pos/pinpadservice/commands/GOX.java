package io.cloudwalk.pos.pinpadservice.commands;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.ModoCriptografia;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoGoOnChip;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoGoOnChip;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.utilities.DataUtility;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;
import io.cloudwalk.pos.pinpadservice.presentation.MainActivity;
import io.cloudwalk.pos.pinpadservice.utilities.ManufacturerUtility;

public class GOX {
    private static final String TAG_LOGCAT = GOX.class.getSimpleName();

    private static String CMD_ID = ABECS.GOX;

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadManager.getInstance().getPinpad();
    }

    private static Bundle parseRSP(SaidaComandoGoOnChip response) {
        Bundle output = new Bundle();



        return output;
    }

    public static Bundle gox(Bundle input)
            throws Exception {
        final long overhead = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        CMD_ID = input.getString(ABECS.CMD_ID, ABECS.GOX);

        long    SPE_CASHBACK    = input.getLong  (ABECS.SPE_CASHBACK, -1);
        int     SPE_TRNTYPE     = input.getInt   (ABECS.SPE_TRNTYPE, (SPE_CASHBACK != -1) ? ((int) 0x09) : ((int) 0x00));
        int     SPE_ACQREF      = input.getInt   (ABECS.SPE_ACQREF, 0);

        long    SPE_AMOUNT      = input.getLong  (ABECS.SPE_AMOUNT, -1);
        int     SPE_TRNCURR     = input.getInt   (ABECS.SPE_TRNCURR, -1);

        String  SPE_GOXOPT      = input.getString(ABECS.SPE_GOXOPT);
                SPE_GOXOPT      = (SPE_GOXOPT != null) ? SPE_GOXOPT   : "00000";

        int     SPE_KEYIDX      = input.getInt   (ABECS.SPE_KEYIDX, -1);

        String  SPE_DSPMSG      = input.getString(ABECS.SPE_DSPMSG);

        String  SPE_EMVDATA     = input.getString(ABECS.SPE_EMVDATA);
        String  SPE_TAGLIST     = input.getString(ABECS.SPE_TAGLIST);
        int     SPE_TIMEOUT     = input.getInt   (ABECS.SPE_TIMEOUT, -1);

        // TODO: SPE_MTHDPIN

        EntradaComandoGoOnChip.Builder builder =
                new EntradaComandoGoOnChip.Builder(SPE_ACQREF, ModoCriptografia.DUKPT_3DES, SPE_KEYIDX);

        builder.informaTipoTransacao            ((byte) SPE_TRNTYPE);
        builder.informaValorTotal               (SPE_AMOUNT);


        if (SPE_CASHBACK != -1) {
            builder.informaValorTroco           (SPE_CASHBACK);
        }

        if (SPE_TRNCURR != -1) {
            builder.informaCodigoMoeda          (SPE_TRNCURR);
        }

        builder.informaPanNaListaExcecao        ((SPE_GOXOPT.charAt(0) != '0'));
        builder.informaForcaTransacaoOnline     ((SPE_GOXOPT.charAt(1) != '0'));
        builder.informaPermiteBypass            ((SPE_GOXOPT.charAt(2) != '0'));

        // TODO: SPE_WKENC

        if (SPE_DSPMSG != null) {
            builder.informaMensagemPin          (SPE_DSPMSG);
        }

        // TODO: SPE_TRMPAR

        EntradaComandoGoOnChip.ParametrosTerminalRiskManagement
                SPE_TRMPAR = new EntradaComandoGoOnChip.ParametrosTerminalRiskManagement();

        SPE_TRMPAR.informaTargetPercentage      (Byte.parseByte("25"));
        SPE_TRMPAR.informaThresholdValue        ("00000000".getBytes());
        SPE_TRMPAR.informaTerminalFloorLimit    ("00000000".getBytes());
        SPE_TRMPAR.informaMaxTargetPercentage   (Byte.parseByte("25"));

        builder.informaParametrosTerminalRiskManagement (SPE_TRMPAR);
        builder.informaTimeoutInatividade               (SPE_TIMEOUT);

        if (SPE_EMVDATA != null) {
            byte[] data = new byte[SPE_EMVDATA.length()];
            int    i    = 0;

            while (SPE_EMVDATA.length() > 1) {
                data[i++] = (byte) Integer.parseInt(SPE_EMVDATA.substring(0, 2), 16);

                SPE_EMVDATA = SPE_EMVDATA.substring(2);
            }

            builder.informaDadosEMV(data);
        }

        if (SPE_TAGLIST != null) {
            byte[] data = new byte[SPE_TAGLIST.length()];
            int    i    = 0;

            while (SPE_TAGLIST.length() > 1) {
                data[i++] = (byte) Integer.parseInt(SPE_TAGLIST.substring(0, 2), 16);

                SPE_TAGLIST = SPE_TAGLIST.substring(2);
            }

            builder.informaListaTagsEMV(data);
        }

        DataUtility.getApplicationContext().startActivity(new Intent(DataUtility.getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        MainActivity.acquireUninterruptibly();

        long[] timestamp = { SystemClock.elapsedRealtime() };

        getPinpad().goOnChip(builder.build(), response -> {
            timestamp[0] = SystemClock.elapsedRealtime() - timestamp[0];

            PinpadManager.getInstance().setCallbackStatus(false);

            ABECS.STAT status = ManufacturerUtility.toSTAT(response.obtemResultadoOperacao());

            output[0].putString(ABECS.RSP_ID,   ABECS.GCX);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            try {
                if (status != ABECS.STAT.ST_OK) {
                    return;
                }

                output[0].putAll(parseRSP(response));
            } finally {
                semaphore[0].release();

                MainActivity.release();
            }
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG_LOGCAT, ABECS.GCX + "::timestamp [" + timestamp[0] + "ms] [" + ((SystemClock.elapsedRealtime() - overhead) - timestamp[0]) + "ms]");

        return output[0];
    }
}
