package com.example.poc2104301453.pinpadservice.commands;

import android.os.Bundle;
import android.util.Log;

import com.example.poc2104301453.pinpadlibrary.ABECS;
import com.example.poc2104301453.pinpadlibrary.exceptions.MissingArgumentException;
import com.example.poc2104301453.pinpadservice.PinpadAbstractionLayer;
import com.example.poc2104301453.pinpadservice.utilities.ManufacturerUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoFuncoesPinpad;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoCheckEvent;
import br.com.verifone.bibliotecapinpad.saidas.SaidaComandoGetCard;

import static br.com.verifone.bibliotecapinpad.entradas.EntradaComandoCheckEvent.Eventos.*;
import static br.com.verifone.bibliotecapinpad.saidas.SaidaComandoCheckEvent.EventoOcorrido.*;

public class CKE {
    private static final String TAG_LOGCAT = CKE.class.getSimpleName();

    public static Bundle cke(Bundle input)
            throws Exception {
        AcessoFuncoesPinpad pinpad = PinpadAbstractionLayer.getInstance().getPinpad();
        String CMD_ID = input.getString(ABECS.CMD_ID);

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        List<EntradaComandoCheckEvent.Eventos> eventList =
                new ArrayList<>(0);

        if (input.getInt(ABECS.CKE_KEY) != 0) {
            eventList.add(VERIFICA_PRESSIONAMENTO_TECLAS);
        }

        if (input.getInt(ABECS.CKE_MAG) != 0) {
            eventList.add(VERIFICA_PASSAGEM_CARTAO_MAGNETICO);
        }

        if (input.getInt(ABECS.CKE_ICC) != 0) {
            eventList.add(VERIFICA_INSERCAO_ICC);
        }

        if (input.getInt(ABECS.CKE_CTLS) != 0) {
            eventList.add(VERIFICA_APROXIMACAO_CTLS);
        }

        if (eventList.isEmpty()) {
            throw new MissingArgumentException();
        }

        EntradaComandoCheckEvent entradaComandoCheckEvent =
                new EntradaComandoCheckEvent(eventList);

        entradaComandoCheckEvent.informaTimeout(-1);

        pinpad.checkEvent(entradaComandoCheckEvent, saidaComandoCheckEvent -> {
            ABECS.STAT status = ManufacturerUtility.toSTAT(saidaComandoCheckEvent.obtemResultadoOperacao());

            output[0].putString(ABECS.RSP_ID, CMD_ID);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            try {
                if (status != ABECS.STAT.ST_OK) {
                    return;
                }

                SaidaComandoCheckEvent.EventoOcorrido event =
                        saidaComandoCheckEvent.obtemEventoOcorrido();

                switch (event) {
                    case CARTAO_MAG_LIDO:
                        output[0].putInt(ABECS.CKE_EVENT, 1);

                        SaidaComandoGetCard.DadosCartao card =
                                saidaComandoCheckEvent.obtemDadosCartao();

                        if (card == null) {
                            break;
                        }

                        output[0].putString(ABECS.CKE_TRK1, card.obtemTrilha1()); /* TODO: review formatting */
                        output[0].putString(ABECS.CKE_TRK2, card.obtemTrilha2());
                        output[0].putString(ABECS.CKE_TRK3, card.obtemTrilha3());
                        break;

                    case CARTAO_ICC_REMOVIDO:
                    case CARTAO_ICC_INSERIDO:
                        output[0].putInt(ABECS.CKE_EVENT, 2);
                        output[0].putInt(ABECS.CKE_ICCSTAT,  (event != CARTAO_ICC_REMOVIDO) ? 1 : 0);
                        break;

                    case CARTAO_CTLS_DETECTADO:
                    case CARTAO_CTLS_NAO_DETECTADO:
                        output[0].putInt(ABECS.CKE_EVENT, 3);
                        output[0].putInt(ABECS.CKE_CTLSSTAT, (event != CARTAO_CTLS_NAO_DETECTADO) ? 1 : 0);
                        break;

                    default:
                        output[0].putInt(ABECS.CKE_EVENT, 0);

                        switch (event) {
                            case TECLA_OK_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 0);
                                break;

                            case TECLA_F1_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 4);
                                break;

                            case TECLA_F2_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 5);
                                break;

                            case TECLA_F3_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 6);
                                break;

                            case TECLA_F4_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 7);
                                break;

                            case TECLA_LIMPA_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 8);
                                break;

                            case TECLA_CANCELA_PRESSIONADA:
                                output[0].putInt(ABECS.CKE_KEYCODE, 13);
                                break;

                            default:
                                /* Nothing to do */
                                break;
                        }
                }
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
