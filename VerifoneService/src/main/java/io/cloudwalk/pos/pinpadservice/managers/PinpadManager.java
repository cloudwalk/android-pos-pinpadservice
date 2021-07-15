package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import br.com.verifone.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.LedsContactless;
import br.com.verifone.bibliotecapinpad.definicoes.Menu;
import br.com.verifone.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.verifone.bibliotecapinpad.definicoes.TipoNotificacao;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadservice.commands.OPN;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String TAG = PinpadManager.class.getSimpleName();

    private static final List<Pair<String, Runnable>> sCommandList = new ArrayList<>(0);

    private static final PinpadManager sPinpadManager = new PinpadManager();

    private static final Semaphore[] sSemaphore = {
            new Semaphore(1, true),
            new Semaphore(1, true),
            new Semaphore(1, true)
    };

    private static AcessoDiretoPinpad sAcessoDiretoPinpad = null;

    /**
     * Runnable interface.
     */
    public static interface Runnable {
        /**
         * @return {@link Bundle}
         * @throws Exception self-describing
         */
        Bundle run(Bundle input)
                throws Exception;
    }

    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        sCommandList.add(new Pair<>(ABECS.OPN, OPN::opn));
    }

    public static PinpadManager getInstance() {
        Log.d(TAG, "getInstance");

        return sPinpadManager;
    }

    public AcessoDiretoPinpad getPinpad() {
        Log.d(TAG, "getPinpad");

        sSemaphore[1].acquireUninterruptibly();

        if (sAcessoDiretoPinpad == null) {
            InterfaceUsuarioPinpad callback = new InterfaceUsuarioPinpad() {
                @Override
                public void mensagemNotificacao(String s, TipoNotificacao tipoNotificacao) {
                    Log.d(TAG, "mensagemNotificacao::s [" + s + "], tipoNotificacao [" + tipoNotificacao + "]");
                }

                @Override
                public void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
                    Log.d(TAG, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");
                }

                @Override
                public void menu(Menu menu) {
                    Log.d(TAG, "menu::menu [" + menu + "]");
                }

                @Override
                public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                    Log.d(TAG, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");
                }
            };

            try {
                sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(callback);
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }

        sSemaphore[1].release();

        return sAcessoDiretoPinpad;
    }
}
