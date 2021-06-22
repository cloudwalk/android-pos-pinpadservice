package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadlibrary.exceptions.MissingArgumentException;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;
import com.example.poc2104301453.pinpadservice.utilities.ManufacturerUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoGetCard;

public class GCR {
    private static final String TAG_LOGCAT = GCR.class.getSimpleName();

    public static Bundle gcr(Bundle input)
            throws Exception {
        AcessoFuncoesPinpad pinpad = PinpadAbstractionLayer.getInstance().getPinpad();
        String CMD_ID = input.getString(ABECS.CMD_ID);

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        int     GCR_ACQIDXREQ   = input.getInt   (ABECS.GCR_ACQIDXREQ, -1);
        int     GCR_APPTYPREQ   = input.getInt   (ABECS.GCR_APPTYPREQ, -1);
        long    GCR_AMOUNT      = input.getLong  (ABECS.GCR_AMOUNT);
        String  GCR_DATE        = input.getString(ABECS.GCR_DATE);
        String  GCR_TIME        = input.getString(ABECS.GCR_TIME);
        String  GCR_TABVER      = input.getString(ABECS.GCR_TABVER);
        int     GCR_QTDAPP      = input.getInt   (ABECS.GCR_QTDAPP, -1);
        boolean GCR_CTLSON      = input.getInt   (ABECS.GCR_CTLSON, 1) != 0;

        if ((GCR_ACQIDXREQ == -1) || (GCR_APPTYPREQ == -1) || (GCR_TABVER == null)
                || (GCR_QTDAPP == -1)) {
            throw new MissingArgumentException();
        }

        List<String> list = new ArrayList<>(GCR_QTDAPP);

        if ((GCR_QTDAPP = (GCR_APPTYPREQ != 0) ? 0 : GCR_QTDAPP) != 0) {
            int i = 1;

            while (i++ != GCR_QTDAPP) {
                String GCR_IDAPPn = ABECS.GCR_IDAPPn.replace("n", Integer.toString(i));

                String item = input.getString(GCR_IDAPPn);

                if (item != null) {
                    list.add(item);
                } else {
                    throw new MissingArgumentException();
                }
            }
        }

        Date date = new Date();

        try {
            date = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
                    .parse(GCR_DATE + GCR_TIME);
        } catch (Exception exception) {
            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
        }

        EntradaComandoGetCard.Builder builder = new EntradaComandoGetCard.Builder(date);

        List<Integer> typeList = new ArrayList<>(1);

        typeList.add(GCR_APPTYPREQ);

        builder.informaIndiceAdquirente         (GCR_ACQIDXREQ);

        if (GCR_APPTYPREQ != 99) { /* 2021-06-22: workaround for a manufacturer's bug. */
            builder.informaTipoAplicacao        (typeList);
        }

        builder.informaValorTotal               (GCR_AMOUNT);
        builder.informaTimestamp                (GCR_TABVER);
        builder.informaPermiteCtls              (GCR_CTLSON);

        if (!list.isEmpty()) {
            EntradaComandoGetCard.ListaRegistrosAID candidateList =
                    new EntradaComandoGetCard.ListaRegistrosAID();

            for (String item : list) {
                candidateList.adicionaEntrada(Integer.parseInt(item.substring(0, 2)), Integer.parseInt(item.substring(2)));
            }

            builder.informaListaRegistrosAID    (candidateList);
        }

        pinpad.getCard(builder.build(), saidaComandoGetCard -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(saidaComandoGetCard.obtemResultadoOperacao());

            output[0].putString(ABECS.RSP_ID, CMD_ID);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            try {
                if (status != ABECS.STAT.ST_OK) {
                    return;
                }

                /* TODO: code */
            } catch (Exception exception) {
                Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
            } finally {
                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();

        return output[0];
    }
}
