package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import br.com.verifone.bibliotecapinpad.InterfaceUsuarioPinpad;
import br.com.verifone.bibliotecapinpad.definicoes.LedsContactless;
import br.com.verifone.bibliotecapinpad.definicoes.Menu;
import br.com.verifone.bibliotecapinpad.definicoes.NotificacaoCapturaPin;
import br.com.verifone.bibliotecapinpad.definicoes.TipoNotificacao;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager sPinpadManager = new PinpadManager();

    private static final Queue<byte[]> sQueue = new LinkedList<>();

    private static final Semaphore sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore sSendSemaphore = new Semaphore(1, true);

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
    }

    private AcessoDiretoPinpad getPinpad() {
        Log.d(TAG, "getPinpad");

        if (sAcessoDiretoPinpad == null) {
            InterfaceUsuarioPinpad callback = new InterfaceUsuarioPinpad() {
                @Override
                public void mensagemNotificacao(String s, TipoNotificacao tipoNotificacao) {
                    Log.d(TAG, "mensagemNotificacao::s [" + ((s != null) ? s.replace("\n", "\\n") : s) + "], tipoNotificacao [" + tipoNotificacao + "]");
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

    private static byte[] intercept(byte[] data, int length) {
        Log.d(TAG, "intercept");

        try {
            if (length > 4) {
                byte[] CMD_ID = new byte[3];

                System.arraycopy(data, 1, CMD_ID, 0, 3);

                switch (new String(CMD_ID)) {
                    case ABECS.CLX:
                    case ABECS.GIX:
                    case ABECS.OPN:

                    case ABECS.TLI:
                    case ABECS.TLR:
                    case ABECS.TLE:
                        /* Nothing to do */
                        break;

                    default:
                        Log.w(TAG, "intercept::NAK registered");

                        return new byte[]{0x15}; // TODO: NAK if CRC fails, .ERR010......... otherwise?
                }
            }
        } finally {
            Log.h(TAG, data, length);
        }

        return data;
    }

    public static PinpadManager getInstance() {
        Log.d(TAG, "getInstance");

        return sPinpadManager;
    }

    @Override
    public int recv(byte[] output, long timeout) {
        Log.d(TAG, "recv");

        sRecvSemaphore.acquireUninterruptibly();

        int result = -1;

        try {
            byte[] response = sQueue.poll();

            if (response != null) {
                System.arraycopy(response, 0, output, 0, response.length);

                result = response.length;
            } else {
                result = getPinpad().recebeResposta(output, timeout);
            }

            Log.h(TAG, output, result);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sRecvSemaphore.release();

        return result;
    }

    @Override
    public int send(String application, byte[] input, int length) {
        Log.d(TAG, "send");

        sSendSemaphore.acquireUninterruptibly();

        Log.d(TAG, "send::application [" + application + "]");

        int result = -1;

        try {
            byte[] request = intercept(input, length);

            if (request[0] != 0x15) {
                result = getPinpad().enviaComando(request, request.length);
            } else {
                result = (sQueue.add(request)) ? 0 : -1;
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sSendSemaphore.release();

        return result;
    }
}
