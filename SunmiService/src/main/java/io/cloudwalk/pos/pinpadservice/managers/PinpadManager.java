package io.cloudwalk.pos.pinpadservice.managers;

import static java.util.Locale.US;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.setis.sunmi.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager
            sPinpadManager = new PinpadManager();

    private static final Queue<byte[]>
            sQueue = new LinkedList<>();

    private static final Semaphore
            sMngrSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sSendSemaphore = new Semaphore(1, true);

    private static AcessoDiretoPinpad
            sAcessoDiretoPinpad = null;

    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        new Thread() {
            @Override
            public void run() {
                super.run();

                acquire(sMngrSemaphore);

                try {
                    sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(CallbackUtility.getCallback());
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                }

                release(sMngrSemaphore);
            }
        }.start();
    }

    private static AcessoDiretoPinpad getPinpad() {
        // Log.d(TAG, "getPinpad");

        AcessoDiretoPinpad pinpad;

        acquire(sMngrSemaphore);

        pinpad = sAcessoDiretoPinpad;

        release(sMngrSemaphore);

        return pinpad;
    }

    private static byte[] intercept(String applicationId, boolean send, byte[] data, int length) {
        // Log.d(TAG, "intercept::length [" + length + "] (" + ((send) ? "send" : "recv") + ")");

        try {
            acquire(sMngrSemaphore);

            String CMD_ID = "UNKNOWN";

            switch (length) {
                case 0:
                    return data;

                case 1:
                    switch (data[0]) {
                        case 0x04: CMD_ID = "EOT"; break;
                        case 0x15: CMD_ID = "NAK"; break;

                        default:
                            return data;
                    }
                    break;

                default:
                    if (length >= 4) {
                        byte[] slice = new byte[3];

                        System.arraycopy(data, 1, slice, 0, 3);

                        CMD_ID = new String(slice);
                    }
            }

            if (send) {
                switch (CMD_ID) {
                    case ABECS.OPN: case ABECS.GIX: case ABECS.CLX:
                    case ABECS.CEX: case ABECS.CHP: case ABECS.EBX: case ABECS.GCD:
                    case ABECS.GTK: case ABECS.MNU: case ABECS.RMC:
                    case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                    case ABECS.GCX: case ABECS.GED: case ABECS.FCX:
                        /* Nothing to do */
                        break;

                    case ABECS.GPN:
                    case ABECS.GOX:
                        PinCaptureActivity.startActivity(applicationId);
                        break;

                    default:
                        // Log.d(TAG, "intercept::NAK registered");

                        return new byte[] { 0x15 }; // TODO: NAK if CRC fails, .ERR010......... otherwise!?
                }
            } else {
                switch (CMD_ID) {
                    case "EOT":
                    case "NAK":
                        /* no break */

                    case ABECS.GPN:
                    case ABECS.GOX:
                        PinCaptureActivity.finishActivity();
                        break;

                    default:
                        /* Nothing to do */
                        break;
                }
            }
        } finally {
            release(sMngrSemaphore);

            Log.h(TAG, data, length);
        }

        return data;
    }

    private static void acquire(Semaphore semaphore) {
        // Log.d(TAG, "acquire::semaphore [" + semaphore + "]");

        semaphore.acquireUninterruptibly();
    }

    private static void release(Semaphore semaphore) {
        // Log.d(TAG, "release");

        if (semaphore.availablePermits() <= 0) {
            semaphore.release();
        }
    }

    public static PinpadManager getInstance() {
        Log.d(TAG, "getInstance");

        return sPinpadManager;
    }

    @Override
    public int recv(@NotNull Bundle bundle) {
        Log.d(TAG, "recv");

        long   timeout  = bundle.getLong("timeout", 0);
        int    result   = -1;

        acquire(sRecvSemaphore);

        byte[] response = sQueue.poll();

        try {
            if (response != null) {
                Log.h(TAG, response, result);

                result = response.length;
            } else {
                response = new byte[2048];

                result = getPinpad().recebeResposta(response, timeout);

                response = intercept(null, false, response, result);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            if (result > 0) {
                byte[] courier = new byte[result];

                System.arraycopy(response, 0, courier, 0, result);

                bundle.putByteArray("response", courier);
            }
        }

        release(sRecvSemaphore);

        return result;
    }

    @Override
    public int send(@NotNull Bundle bundle, IServiceCallback callback) {
        Log.d(TAG, "send");

        acquire(sSendSemaphore);

        String applicationId = bundle.getString   ("application_id");
        byte[] request       = bundle.getByteArray("request");

        Log.d(TAG, "send::applicationId [" + applicationId + "]");

        if (request.length > 1) {
            CallbackUtility.setServiceCallback(callback);
        }

        int result = -1;

        try {
            request = intercept(applicationId, true, request, request.length);

            if (request[0] != 0x15) {
                result = getPinpad().enviaComando(request, request.length);
            } else {
                result = (sQueue.add(request)) ? 0 : -1;
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        release(sSendSemaphore);

        return result;
    }
}
