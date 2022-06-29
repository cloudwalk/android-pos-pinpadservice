package io.cloudwalk.pos.pinpadservice.utilities;

import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_MSG;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_OPTLST;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_PIN;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_TITLE;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_TYPE;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NOTIFICATION;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_2x16;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_AID_INVALID;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_CARD_BLOCKED;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_INSERT_SWIPE_CARD;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_BLOCKED;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_ENTRY;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_FINISH;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_INVALID;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_LAST_TRY;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_START;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PIN_VERIFIED;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_PROCESSING;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_REMOVE_CARD;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_SECOND_TAP;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_SELECT;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_SELECTED;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_TAP_INSERT_SWIPE_CARD;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NTF_UPDATING;

import android.os.Bundle;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.LedsContactless;
import br.com.verifone.bibliotecapinpad.definicoes.Menu;
import br.com.verifone.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.verifone.bibliotecapinpad.definicoes.TipoNotificacao;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;

public class CallbackUtility {
    private static final String
            TAG = CallbackUtility.class.getSimpleName();

    private static final Semaphore
            sClbkSemaphore = new Semaphore(1, true);

    private static IServiceCallback
            sServiceCallback = null;

    private CallbackUtility() {
        Log.d(TAG, "CallbackUtility");

        /* Nothing to do */
    }

    private static void mensagemNotificacao(String mensagem, int count, int tipoNotificacao) {
        // Log.d(TAG, "mensagemNotificacao::mensagem [" + ((mensagem != null) ? mensagem.replace("\n", "\\n") : null) + "] count [" + count + "] tipoNotificacao [" + tipoNotificacao + "]");

        switch (PinpadManager.Callback.Type.values()[tipoNotificacao]) {
            case NTF_PIN_START:
            case NTF_PIN_ENTRY:
                PinCaptureActivity.resumeActivity();
                /* no break */

            case NTF_PIN_FINISH:
                PinCaptureActivity.moveActivity(tipoNotificacao != NTF_PIN_FINISH.ordinal());
                break;

            default:
                PinCaptureActivity.moveActivity(false);
                break;
        }

        IServiceCallback callback = getServiceCallback();

        if (callback != null) {
            Bundle bundle     = new Bundle();
            StringBuilder pin = new StringBuilder();

            bundle.putString(NTF_MSG, (mensagem != null) ? mensagem : "");
            bundle.putInt   (NTF_TYPE, tipoNotificacao);

            for (int i = 0; i < count; i++) {
                pin.append("*");
            }

            if (count >= 0) {
                bundle.putString(NTF_PIN, pin.toString());
            }

            try {
                callback.onServiceCallback(bundle);
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }
    }

    private static void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
        // Log.d(TAG, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");

        String msg   = notificacaoCapturaPin.obtemMensagemCapturaPin();
           int count = notificacaoCapturaPin.obtemQuantidadeDigitosPin();

           mensagemNotificacao(msg, count, NTF_PIN_ENTRY.ordinal());
    }

    private static void menu(Menu menu) {
        // Log.d(TAG, "menu::menu [" + menu + "]");

        IServiceCallback callback = getServiceCallback();

        if (callback != null) {
            Bundle bundle = new Bundle();

            bundle.putInt            (NTF_TYPE,   NTF_SELECT.ordinal());
            bundle.putStringArrayList(NTF_OPTLST, (ArrayList<String>) menu.obtemOpcoesMenu());
            bundle.putString         (NTF_TITLE,                      menu.obtemTituloMenu());

            try {
                menu.obtemMenuCallback().informaOpcaoSelecionada(callback.onServiceCallback(bundle));
            } catch (RemoteException exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }
    }

    private static void ledsProcessamentoContactless(LedsContactless ledsContactless) {
        // Log.d(TAG, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");

        /* Nothing to do */
    }

    public static IServiceCallback getServiceCallback() {
        Log.d(TAG, "getServiceCallback");

        IServiceCallback response;

        sClbkSemaphore.acquireUninterruptibly();

        response = sServiceCallback;

        sClbkSemaphore.release();

        return response;
    }

    public static void setServiceCallback(IServiceCallback callback) {
        Log.d(TAG, "setServiceCallback");

        sClbkSemaphore.acquireUninterruptibly();

        sServiceCallback = callback;

        sClbkSemaphore.release();
    }

    public static InterfaceUsuarioPinpad getCallback() {
        Log.d(TAG, "getCallback");

        return new InterfaceUsuarioPinpad() {
            @Override
            public void mensagemNotificacao(String mensagem, TipoNotificacao tipoNotificacao) {
                Log.d(TAG, "mensagemNotificacao::mensagem [" + ((mensagem != null) ? mensagem.replace("\n", "\\n") : null) + "] tipoNotificacao [" + tipoNotificacao + "]");

                PinpadManager.Callback.Type type = null;

                switch (tipoNotificacao) {
                    case DSP_LIVRE:                         type = NOTIFICATION;                break;
                    case DSP_2X16:                          type = NTF_2x16;                    break;
                    case DSP_PROCESSANDO:                   type = NTF_PROCESSING;              break;
                    case DSP_INSIRA_PASSE_CARTAO:           type = NTF_INSERT_SWIPE_CARD;       break;
                    case DSP_APROXIME_INSIRA_PASSE_CARTAO:  type = NTF_TAP_INSERT_SWIPE_CARD;   break;
                    case DSP_SELECIONE:                     type = NTF_SELECT;                  break;
                    case DSP_SELECIONADO:                   type = NTF_SELECTED;                break;
                    case DSP_APP_INVALIDA:                  type = NTF_AID_INVALID;             break;
                    case DSP_SENHA_INVALIDA:                type = NTF_PIN_INVALID;             break;
                    case DSP_SENHA_ULTIMA_TENTATIVA:        type = NTF_PIN_LAST_TRY;            break;
                    case DSP_SENHA_BLOQUEADA:               type = NTF_PIN_BLOCKED;             break;
                    case DSP_SENHA_VERIFICADA:              type = NTF_PIN_VERIFIED;            break;
                    case DSP_CARTAO_BLOQUEADO:              type = NTF_CARD_BLOCKED;            break;
                    case DSP_RETIRE_CARTAO:                 type = NTF_REMOVE_CARD;             break;
                    case DSP_ATUALIZANDO_TABELAS:           type = NTF_UPDATING;                break;
                    case DSP_REAPRESENTE_CARTAO:            type = NTF_SECOND_TAP;              break;
                    case DSP_INICIA_PIN:                    type = NTF_PIN_START;               break;
                    case DSP_ENCERRA_PIN:                   type = NTF_PIN_FINISH;              break;
                }

                CallbackUtility.mensagemNotificacao(mensagem, -1, (type != null) ? type.ordinal() : -1);
            }

            @Override
            public void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
                Log.d(TAG, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");

                CallbackUtility.notificacaoCapturaPin(notificacaoCapturaPin);
            }

            @Override
            public void menu(Menu menu) {
                Log.d(TAG, "menu::menu [" + menu + "]");

                CallbackUtility.menu(menu);
            }

            @Override
            public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                Log.d(TAG, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");

                CallbackUtility.ledsProcessamentoContactless(ledsContactless);
            }
        };
    }
}
