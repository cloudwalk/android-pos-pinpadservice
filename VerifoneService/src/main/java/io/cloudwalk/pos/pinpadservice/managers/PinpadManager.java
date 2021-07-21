package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

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
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String TAG = PinpadManager.class.getSimpleName();

    private static final List<Pair<String, Runnable>> sCommandList = new ArrayList<>(0);

    private static final PinpadManager sPinpadManager = new PinpadManager();

    private static final Semaphore sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore sSendSemaphore = new Semaphore(1, true);

    private static final byte ACK = 0x06;

    private static final byte NAK = 0x15;

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

    private AcessoDiretoPinpad getPinpad() {
        Log.d(TAG, "getPinpad");

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

        return sAcessoDiretoPinpad;
    }

    public static PinpadManager getInstance() {
        Log.d(TAG, "getInstance");

        return sPinpadManager;
    }

    @Override
    public byte[] request(byte[] input) {
        Log.d(TAG, "request");

        sSendSemaphore.acquireUninterruptibly();

        int status = 0;

        try {
            status = getPinpad().enviaComando(input, input.length);

            Log.d(TAG, "request::enviaComando(byte[], int) [" + status + "]");

            if (status < 0) {
                throw new RuntimeException();
            }
        } catch (Exception exception) {
            /* 2021-07-21: unexpected */

            Log.e(TAG, Log.getStackTraceString(exception));

            return new byte[] { NAK };
        } finally {
            sSendSemaphore.release();
        }

        sRecvSemaphore.acquireUninterruptibly();

        byte[] output = new byte[2048 + 4];

        try {
            int i = 0;

            do {
                output[0] = NAK;

                status = getPinpad().recebeResposta(output, (i != 0) ? 10000 : 2000);

                Log.d(TAG, "request::recebeResposta(byte[], long) [" + status + "]");

                if (status < 0) {
                    throw new RuntimeException();
                }

                if (status == 0) {
                    throw new TimeoutException();
                }
            } while (output[0] != NAK && ++i < 2);
        } catch (Exception exception) {
            /* 2021-07-21: unexpected */

            Log.e(TAG, Log.getStackTraceString(exception));

            output = new byte[] { NAK };
        } finally {
            sRecvSemaphore.release();
        }

        return DataUtility.trimByteArray(output);
    }
}
