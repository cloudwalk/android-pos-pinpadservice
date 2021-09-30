package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.Locale.US;

import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.*;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;

import com.sunmi.pay.hardware.aidl.AidlConstants;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.LedsContactless;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.Menu;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.setis.sunmi.bibliotecapinpad.definicoes.TipoNotificacao;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import sunmi.paylib.SunmiPayKernel;

public class CallbackUtility {
    private static final String
            TAG = CallbackUtility.class.getSimpleName();

    private static final Semaphore
            sClbkSemaphore = new Semaphore(1, true);

    private static IServiceCallback
            sServiceCallback = null;

    private CallbackUtility() {
        /* Nothing to do */
    }

    private static void mensagemNotificacao(String mensagem, int count, int tipoNotificacao) {
        Log.d(TAG, "mensagemNotificacao::mensagem [" + ((mensagem != null) ? mensagem.replace("\n", "\\n") : null) + "] count [" + count + "] tipoNotificacao [" + tipoNotificacao + "]");

        int visibility = 0;

        switch (tipoNotificacao) {
            case NTF_PIN_START:
            case NTF_PIN_ENTRY:
                visibility = 1;
                /* no break */

            case NTF_PIN_FINISH:
                visibility = (visibility != 0) ? visibility : 2;

                PinCaptureActivity.setVisibility(visibility != 2);
                break;

            default:
                /* Nothing to do */
                break;
        }

        IServiceCallback callback = getServiceCallback();

        if (callback != null) {
            Bundle bundle     = new Bundle();
            StringBuilder pin = new StringBuilder();

            for (int i = 0; i < count; i++) {
                pin.append("*");
            }

            bundle.putString(ABECS.NTF_MSG, (mensagem != null) ? mensagem : "");

            if (count >= 0) {
                bundle.putString(ABECS.NTF_PIN, pin.toString());
            }

            try {
                callback.onNotificationThrow(bundle, tipoNotificacao);
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }
    }

    private static void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
        Log.d(TAG, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");

        String msg   = notificacaoCapturaPin.obtemMensagemCapturaPin();
           int count = notificacaoCapturaPin.obtemQuantidadeDigitosPin();

           mensagemNotificacao(msg, count, NTF_PIN_ENTRY);
    }

    private static void menu(Menu menu) {
        Log.d(TAG, "menu::menu [" + menu + "]");

        IServiceCallback callback = getServiceCallback();

        if (callback != null) {
            Bundle bundle = new Bundle();

            bundle.putString(ABECS.NTF_TITLE,                               menu.obtemTituloMenu());
            bundle.putStringArrayList(ABECS.NTF_OPTLST, (ArrayList<String>) menu.obtemOpcoesMenu());
            bundle.putString(ABECS.NTF_TIMEOUT,         String.format(US, "%03d", menu.obtemTimeout()));

            try {
                int option = callback.onSelectionRequired(bundle);

                Log.d(TAG, "menu::option [" + option + "]");

                menu.obtemMenuCallback().informaOpcaoSelecionada(option);
            } catch (RemoteException exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }
    }

    private static void ledsProcessamentoContactless(LedsContactless ledsContactless) {
        Log.d(TAG, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");

        int[] status = ledsContactless.checaLedsAcesos();

        for (int i = 0; i < status.length; i++) {
            Log.d(TAG, "ledsProcessamentoContactless::status[" + i + "] [" + status[i] + "]");

            int led = -1;

            // try {
                switch (i) {
                    case 0: led = AidlConstants.LedLight.BLUE_LIGHT;   break;
                    case 1: led = AidlConstants.LedLight.YELLOW_LIGHT; break;
                    case 2: led = AidlConstants.LedLight.GREEN_LIGHT;  break;
                    case 3: led = AidlConstants.LedLight.RED_LIGHT;    break;

                    default:
                        continue;
                }

                /* 2021-09-17: disabled due to bugged BC v1.27 */
                // SunmiPayKernel.getInstance().mBasicOptV2.ledStatusOnDevice(led, (status[i] != 0) ? 0 : 1);

            //     SystemClock.sleep(50); /* 2021-09-10: user experience */
            // } catch (RemoteException exception) {
            //     Log.e(TAG, Log.getStackTraceString(exception));
            // }
        }
    }

    public static IServiceCallback getServiceCallback() {
        Log.d(TAG, "getServiceCallback");

        IServiceCallback output;

        sClbkSemaphore.acquireUninterruptibly();

        if (sServiceCallback == null) {
            Log.d(TAG, "getServiceCallback::sServiceCallback [null]");
        }

        output = sServiceCallback;

        sClbkSemaphore.release();

        return output;
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
                int type = -1;

                switch (tipoNotificacao) {
                    case DSP_LIVRE:                         type = NTF;                         break;
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

                CallbackUtility.mensagemNotificacao(mensagem, -1, type);
            }

            @Override
            public void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
                CallbackUtility.notificacaoCapturaPin(notificacaoCapturaPin);
            }

            @Override
            public void menu(Menu menu) {
                CallbackUtility.menu(menu);
            }

            @Override
            public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                CallbackUtility.ledsProcessamentoContactless(ledsContactless);
            }
        };
    }
}
