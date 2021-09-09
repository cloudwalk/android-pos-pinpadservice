package io.cloudwalk.pos.pinpadservice.managers;

import static java.util.Locale.US;

import android.os.Bundle;
import android.os.RemoteException;

import java.util.ArrayList;
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
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager
            sPinpadManager = new PinpadManager();

    private static final Queue<byte[]>
            sQueue = new LinkedList<>();

    private static final Semaphore[]
            sClbkSemaphore = {
                    new Semaphore(1, true),
                    new Semaphore(1, true)
            };

    private static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sSendSemaphore = new Semaphore(1, true);

    private static AcessoDiretoPinpad
            sAcessoDiretoPinpad = null;

    private static IServiceCallback
            sServiceCallback = null;

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
                    Log.d(TAG, "mensagemNotificacao::s [" + ((s != null) ? s.replace("\n", "\\n") : "null") + "], tipoNotificacao [" + tipoNotificacao + "]");

                    Semaphore semaphore = new Semaphore(1, true);

                    switch (tipoNotificacao) {
                        case DSP_INICIA_PIN:    /* 16 */
                        case DSP_ENCERRA_PIN:   /* 17 */
                            semaphore.acquireUninterruptibly();
                            break;

                        default:
                            /* Nothing to do */
                            break;
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            sClbkSemaphore[1].acquireUninterruptibly();

                            IServiceCallback callback = getServiceCallback();

                            if (callback != null) {
                                Bundle bundle = new Bundle();

                                bundle.putString("NTF_MSG", (s != null) ? s : "");

                                try {
                                    callback.onNotificationThrow(bundle, tipoNotificacao.ordinal());
                                } catch (Exception exception) {
                                    Log.e(TAG, Log.getStackTraceString(exception));
                                }
                            }

                            semaphore.release();

                            sClbkSemaphore[1].release();
                        }
                    }.start();

                    semaphore.acquireUninterruptibly();
                }

                @Override
                public void notificacaoCapturaPin(NotificacaoCapturaPin notificacaoCapturaPin) {
                    Log.d(TAG, "notificacaoCapturaPin::notificacaoCapturaPin [" + notificacaoCapturaPin + "]");

                    // TODO: to be used internally (GOX) (GPN)
                }

                @Override
                public void menu(Menu menu) {
                    Log.d(TAG, "menu::menu [" + menu + "]");

                    IServiceCallback callback = getServiceCallback();

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

                @Override
                public void ledsProcessamentoContactless(LedsContactless ledsContactless) {
                    Log.d(TAG, "ledsProcessamentoContactless::ledsContactless [" + ledsContactless + "]");

                    // TODO: VFSystemService!? VFService!?
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

    private static IServiceCallback getServiceCallback() {
        Log.d(TAG, "getServiceCallback");

        IServiceCallback output;

        sClbkSemaphore[0].acquireUninterruptibly();

        if (sServiceCallback == null) {
            Log.d(TAG, "getServiceCallback::sServiceCallback [null]");
        }

        output = sServiceCallback;

        sClbkSemaphore[0].release();

        return output;
    }

    private static void setServiceCallback(IServiceCallback callback) {
        Log.d(TAG, "setServiceCallback");

        sClbkSemaphore[0].acquireUninterruptibly();

        sServiceCallback = callback;

        sClbkSemaphore[0].release();
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
    public int send(String application, IServiceCallback callback, byte[] input, int length) {
        Log.d(TAG, "send");

        sSendSemaphore.acquireUninterruptibly();

        Log.d(TAG, "send::application [" + application + "]");

        if (length > 1) { /* 2021-08-11: e.g. not a control byte - e.g. <<CAN>> */
            setServiceCallback(callback);
        }

        int result = -1;

        try {
            byte[] request = PinpadUtility.intercept(input, length);

            if (request[0] != 0x15) {
                result = getPinpad().enviaComando(request, length);
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
