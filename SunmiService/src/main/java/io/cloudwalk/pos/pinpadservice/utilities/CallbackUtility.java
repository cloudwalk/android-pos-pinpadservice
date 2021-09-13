package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.Locale.US;

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
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import sunmi.paylib.SunmiPayKernel;

public class CallbackUtility {
    private static final String
            TAG = CallbackUtility.class.getSimpleName();

    private static final Semaphore
            sClbkSemaphore = new Semaphore(1, true);

    private CallbackUtility() {
        /* Nothing to do */
    }

    private static void mensagemNotificacao(String mensagem, TipoNotificacao tipoNotificacao) {
        Log.d(TAG, "mensagemNotificacao::mensagem [" + ((mensagem != null) ? mensagem.replace("\n", "\\n") : null) + "], tipoNotificacao [" + tipoNotificacao + "]");

        boolean sync = false;

        switch (tipoNotificacao) {
            case DSP_INICIA_PIN:    /* 16 */
            case DSP_ENCERRA_PIN:   /* 17 */
                PinCaptureActivity.onNotificationThrow(mensagem, -1, tipoNotificacao.ordinal());
                /* no break */

            case DSP_SENHA_INVALIDA:
            case DSP_SENHA_ULTIMA_TENTATIVA:
            case DSP_SENHA_BLOQUEADA:
            case DSP_SENHA_VERIFICADA:
            case DSP_CARTAO_BLOQUEADO:
            case DSP_REAPRESENTE_CARTAO:
                sync = true;
                break;

            default:
                /* Nothing to do */
                break;
        }

        Semaphore semaphore = new Semaphore((sync) ? 0 : 1, true);

        new Thread() {
            @Override
            public void run() {
                super.run();

                sClbkSemaphore.acquireUninterruptibly();

                IServiceCallback callback = PinpadManager.getServiceCallback();

                if (callback != null) {
                    Bundle bundle = new Bundle();

                    bundle.putString("NTF_MSG", (mensagem != null) ? mensagem : "");

                    try {
                        callback.onNotificationThrow(bundle, tipoNotificacao.ordinal());
                    } catch (Exception exception) {
                        Log.e(TAG, Log.getStackTraceString(exception));
                    }
                }

                sClbkSemaphore.release();

                semaphore.release();
            }
        }.start();

        semaphore.acquireUninterruptibly();
    }

    private static void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
        Log.d(TAG, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");

        String msg   = notificacaoCapturaPin.obtemMensagemCapturaPin();
           int count = notificacaoCapturaPin.obtemQuantidadeDigitosPin();

        // TODO: expose to the one who made the request
        // TODO: reuse code from `mensagemNotificacao`!?
    }

    private static void menu(Menu menu) {
        Log.d(TAG, "menu::menu [" + menu + "]");

        IServiceCallback callback = PinpadManager.getServiceCallback();

        if (callback != null) {
            Bundle bundle = new Bundle();

            bundle.putString("NTF_TTL", menu.obtemTituloMenu());
            bundle.putStringArrayList("NTF_OPT", (ArrayList<String>) menu.obtemOpcoesMenu());
            bundle.putString("NTF_TOT", String.format(US, "%03d", menu.obtemTimeout()));

            try {
                menu.obtemMenuCallback().informaOpcaoSelecionada(callback.onSelectionRequired(bundle));
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

            try {
                switch (i) {
                    case 0: led = AidlConstants.LedLight.BLUE_LIGHT;   break;
                    case 1: led = AidlConstants.LedLight.YELLOW_LIGHT; break;
                    case 2: led = AidlConstants.LedLight.GREEN_LIGHT;  break;
                    case 3: led = AidlConstants.LedLight.RED_LIGHT;    break;

                    default:
                        continue;
                }

                SunmiPayKernel.getInstance().mBasicOptV2.ledStatusOnDevice(led, (status[i] != 0) ? 0 : 1);

                SystemClock.sleep(50); /* 2021-09-10: user experience */
            } catch (RemoteException exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }
    }

    public static InterfaceUsuarioPinpad getCallback() {
        Log.d(TAG, "getCallback");

        return new InterfaceUsuarioPinpad() {
            @Override
            public void mensagemNotificacao(String s, TipoNotificacao tipoNotificacao) {
                CallbackUtility.mensagemNotificacao(s, tipoNotificacao);
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
