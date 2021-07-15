package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoCheckEvent;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoGetCard;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;
import io.cloudwalk.pos.pinpadservice.utilities.ManufacturerUtility;

import static br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent.Eventos.VERIFICA_APROXIMACAO_CTLS;
import static br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent.Eventos.VERIFICA_INSERCAO_ICC;
import static br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent.Eventos.VERIFICA_PASSAGEM_CARTAO_MAGNETICO;
import static br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent.Eventos.VERIFICA_PRESSIONAMENTO_TECLAS;

public class CEX {
    private static final String TAG = CEX.class.getSimpleName();

    private static String CMD_ID = ABECS.GCX;

    private static AcessoFuncoesPinpad getPinpad() {
        return PinpadManager.getInstance().getPinpad();
    }

    private static Bundle parseRSP(Bundle input, SaidaComandoCheckEvent response) {
        Bundle output = new Bundle();

        String PP_TRK1INC  = null;
        String PP_TRK2INC  = null;
        String PP_TRK3INC  = null;

        String SPE_PANMASK = input.getString(ABECS.SPE_PANMASK);

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
                    String pattern = (SPE_PANMASK != null) ? SPE_PANMASK : "9999";

                    boolean truncate = CMD_ID.equals(ABECS.CEX);

                    PP_TRK1INC = ManufacturerUtility.getPP_TRKx(pattern, card, truncate, 1);
                    PP_TRK2INC = ManufacturerUtility.getPP_TRKx(pattern, card, truncate, 2);
                    PP_TRK3INC = ManufacturerUtility.getPP_TRKx(pattern, card, truncate, 3);
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
                Log.e(TAG, "response.obtemEventoOcorrido() [" + response.obtemEventoOcorrido() + "]");

                return output;
        }

        if (PP_TRK1INC != null && !PP_TRK1INC.isEmpty()) {
            output.putString(ABECS.PP_TRK1INC, PP_TRK1INC);
        }

        if (PP_TRK2INC != null && !PP_TRK2INC.isEmpty()) {
            output.putString(ABECS.PP_TRK2INC, PP_TRK2INC);
        }

        if (PP_TRK3INC != null && !PP_TRK3INC.isEmpty()) {
            output.putString(ABECS.PP_TRK3INC, PP_TRK3INC);
        }

        return output;
    }

    public static Bundle cex(Bundle input)
            throws Exception {
        final long overhead = SystemClock.elapsedRealtime();

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        CMD_ID = input.getString(ABECS.CMD_ID, ABECS.CEX);

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

        int timeout = input.getInt(ABECS.SPE_TIMEOUT, -1);

        request.informaTimeout((timeout > 255) ? -1 : timeout);

        long[] timestamp = { SystemClock.elapsedRealtime() };

        getPinpad().checkEvent(request, response -> {
            timestamp[0] = SystemClock.elapsedRealtime() - timestamp[0];

            PinpadManager.getInstance().setCallbackStatus(false);

            ABECS.STAT status = ManufacturerUtility.toSTAT(response.obtemResultadoOperacao());

            output[0].putString (ABECS.RSP_ID,   ABECS.CEX);
            output[0].putInt    (ABECS.RSP_STAT, status.ordinal());

            try {
                if (status != ABECS.STAT.ST_OK) {
                    return;
                }

                output[0].putAll(parseRSP(input, response));
            } finally {
                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG, ABECS.CEX + "::timestamp [" + timestamp[0] + "ms] [" + ((SystemClock.elapsedRealtime() - overhead) - timestamp[0]) + "ms]");

        return output[0];
    }
}
