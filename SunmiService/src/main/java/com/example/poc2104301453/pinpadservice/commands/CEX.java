package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadlibrary.utilities.DataUtility;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;
import com.example.poc2104301453.pinpadservice.utilities.ManufacturerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.setis.sunmi.bibliotecapinpad.entradas.EntradaComandoCheckEvent;
import br.com.setis.sunmi.bibliotecapinpad.saidas.SaidaComandoCheckEvent;
import br.com.setis.sunmi.bibliotecapinpad.saidas.SaidaComandoGetCard;

import static br.com.setis.sunmi.bibliotecapinpad.entradas.EntradaComandoCheckEvent.Eventos.*;

public class CEX {
    private static final String TAG_LOGCAT = CEX.class.getSimpleName();

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadAbstractionLayer.getInstance().getPinpad();
    }

    private static Bundle parseRSP(Bundle input, SaidaComandoCheckEvent response) {
        Bundle output = new Bundle();

        String PP_TRK1INC = null;
        String PP_TRK2INC = null;
        String PP_TRK3INC = null;

        switch (response.obtemEventoOcorrido()) {
            case TECLA_OK_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "00");
                break;

            case TECLA_SETA_CIMA_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "02");
                break;

            case TECLA_SETA_BAIX0_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "03");
                break;

            case TECLA_F1_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "04");
                break;

            case TECLA_F2_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "05");
                break;

            case TECLA_F3_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "06");
                break;

            case TECLA_F4_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "07");
                break;

            case TECLA_LIMPA_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "08");
                break;

            case TECLA_CANCELA_PRESSIONADA:
                output.putString(ABECS.PP_EVENT, "13");
                break;

            case CARTAO_MAG_LIDO:
                output.putString(ABECS.PP_EVENT, "90");

                SaidaComandoGetCard.DadosCartao card = response.obtemDadosCartao();

                if (card != null) {
                    PP_TRK1INC = card.obtemTrilha1(); /* TODO: review formatting */
                    PP_TRK2INC = card.obtemTrilha2();
                    PP_TRK3INC = card.obtemTrilha3();
                }
                break;

            case CARTAO_ICC_REMOVIDO:
                output.putString(ABECS.PP_EVENT, "91");
                break;

            case CARTAO_ICC_INSERIDO:
                output.putString(ABECS.PP_EVENT, "92");
                break;

            case CARTAO_CTLS_NAO_DETECTADO:
                output.putString(ABECS.PP_EVENT, "93");
                break;

            case CARTAO_CTLS_DETECTADO:
                output.putString(ABECS.PP_EVENT, "94");
                break;

            default:
                Log.e(TAG_LOGCAT, "response.obtemEventoOcorrido() [" + response.obtemEventoOcorrido() + "]");

                return output;
        }

        String SPE_PANMASK = input.getString(ABECS.SPE_PANMASK);

        int ll = -1;
        int rr = -1;

        if (SPE_PANMASK != null && SPE_PANMASK.length() >= 4) {
            try {
                ll = Integer.parseInt(SPE_PANMASK.substring(0, 2));
                rr = Integer.parseInt(SPE_PANMASK.substring(2, 4));
            } catch (Exception exception) {
                Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
            }
        }

        if (PP_TRK1INC != null) {
            output.putString(ABECS.PP_TRK1INC, DataUtility.mask(PP_TRK1INC, ll, rr));
        }

        if (PP_TRK2INC != null) {
            output.putString(ABECS.PP_TRK2INC, DataUtility.mask(PP_TRK2INC, ll, rr));
        }

        if (PP_TRK3INC != null) {
            output.putString(ABECS.PP_TRK2INC, DataUtility.mask(PP_TRK3INC, ll, rr));
        }

        return output;
    }

    public static Bundle cex(Bundle input)
            throws Exception {
        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        List<EntradaComandoCheckEvent.Eventos> eventList = new ArrayList<>(0);

        String SPE_CEXOPT = input.getString(ABECS.SPE_CEXOPT, "111100");

        if (SPE_CEXOPT.charAt(0) != '0') {
            eventList.add(VERIFICA_PRESSIONAMENTO_TECLAS);
        }

        if (SPE_CEXOPT.charAt(1) != '0') {
            eventList.add(VERIFICA_PASSAGEM_CARTAO_MAGNETICO);
        }

        if (SPE_CEXOPT.charAt(2) != '0') {
            eventList.add(VERIFICA_INSERCAO_ICC);
        }

        if (SPE_CEXOPT.charAt(3) != '0') {
            eventList.add(VERIFICA_APROXIMACAO_CTLS);
        }

        EntradaComandoCheckEvent request = new EntradaComandoCheckEvent(eventList);

        request.informaTimeout(input.getInt(ABECS.SPE_TIMEOUT, -1));

        getPinpad().checkEvent(request, response -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(response.obtemResultadoOperacao());

            output[0].putString (ABECS.RSP_ID,   ABECS.GIX);
            output[0].putInt    (ABECS.RSP_STAT, status.ordinal());

            try {
                output[0].putAll(parseRSP(input, response));
            } finally {
                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();

        return output[0];
    }
}
