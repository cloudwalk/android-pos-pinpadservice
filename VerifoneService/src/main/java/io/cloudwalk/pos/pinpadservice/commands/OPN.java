package io.cloudwalk.pos.pinpadservice.commands;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.LedsContactless;
import br.com.verifone.bibliotecapinpad.definicoes.Menu;
import br.com.verifone.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.verifone.bibliotecapinpad.definicoes.TipoNotificacao;
import br.com.verifone.bibliotecapinpad.entradas.EntradaComandoOpen;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;
import io.cloudwalk.pos.pinpadservice.presentation.PinActivity;
import io.cloudwalk.pos.pinpadservice.utilities.ManufacturerUtility;

public class OPN {
    private static final String TAG_LOGCAT = OPN.class.getSimpleName();

    public static Bundle opn(Bundle input)
            throws Exception {
        final long overhead = SystemClock.elapsedRealtime();

        InterfaceUsuarioPinpad callback = new InterfaceUsuarioPinpad() {
            @Override
            public void mensagemNotificacao(String s, TipoNotificacao tipoNotificacao) {
                Log.d(TAG_LOGCAT, "mensagemNotificacao::s [" + s + "], tipoNotificacao [" + tipoNotificacao + "]");

                switch (tipoNotificacao) {
                    case DSP_SENHA_BLOQUEADA:
                    case DSP_SENHA_INVALIDA:
                    case DSP_SENHA_ULTIMA_TENTATIVA:
                    case DSP_SENHA_VERIFICADA:
                        PinActivity.update(s, tipoNotificacao.ordinal() * -1);
                        break;

                    default:
                        try {
                            Bundle input = new Bundle();

                            input.putString("message", s);

                            PinpadManager.getInstance().getCallback().onNotificationThrow(input, tipoNotificacao.ordinal());
                        } catch (RemoteException exception) {
                            Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
                        }
                        break;
                }
            }

            @Override
            public void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
                Log.d(TAG_LOGCAT, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");

                PinActivity.update(notificacaoCapturaPin.obtemMensagemCapturaPin(), notificacaoCapturaPin.obtemQuantidadeDigitosPin());
            }

            @Override
            public void menu(Menu menu) {
                Log.d(TAG_LOGCAT, "menu::menu [" + menu + "]");

                try {
                    Bundle input = new Bundle();

                    ArrayList<String> options = new ArrayList<>(menu.obtemOpcoesMenu());

                    input.putString("title",   menu.obtemTituloMenu());
                    input.putStringArrayList   ("options", options);
                    input.putInt   ("timeout", menu.obtemTimeout());

                    PinpadManager.getInstance().getCallback().onSelectionRequired(input);
                } catch (RemoteException exception) {
                    Log.e(TAG_LOGCAT, Log.getStackTraceString(exception));
                }
            }

            @Override
            public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                Log.d(TAG_LOGCAT, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");
            }
        };

        EntradaComandoOpen entradaComandoOpen = new EntradaComandoOpen(callback);

        final Bundle[] output = { new Bundle() };
        final Semaphore[] semaphore = { new Semaphore(0, true) };

        long[] timestamp = { SystemClock.elapsedRealtime() };

        PinpadManager.getInstance().getPinpad().open(entradaComandoOpen, response -> {
            timestamp[0] = SystemClock.elapsedRealtime() - timestamp[0];

            PinpadManager.getInstance().setCallbackStatus(false);

            ABECS.STAT status = ManufacturerUtility.toSTAT(response);

            output[0].putString(ABECS.RSP_ID,   ABECS.OPN);
            output[0].putInt   (ABECS.RSP_STAT, status.ordinal());

            semaphore[0].release();
        });

        semaphore[0].acquireUninterruptibly();

        Log.d(TAG_LOGCAT, ABECS.OPN + "::timestamp [" + timestamp[0] + "ms] [" + ((SystemClock.elapsedRealtime() - overhead) - timestamp[0]) + "ms]");

        return output[0];
    }
}
