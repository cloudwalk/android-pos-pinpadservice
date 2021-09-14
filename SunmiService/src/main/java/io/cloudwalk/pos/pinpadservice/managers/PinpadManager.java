package io.cloudwalk.pos.pinpadservice.managers;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.setis.sunmi.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;
import io.cloudwalk.pos.utilitieslibrary.Application;
import sunmi.paylib.SunmiPayKernel;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager
            sPinpadManager = new PinpadManager();

    private static final Queue<byte[]>
            sQueue = new LinkedList<>();

    private static final Semaphore
            sLoadSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sSendSemaphore = new Semaphore(1, true);

    private static AcessoDiretoPinpad
            sAcessoDiretoPinpad = null;

    private PinpadManager() {
        Log.d(TAG, "PinpadManager");

        /* Nothing to do */
    }

    private static AcessoDiretoPinpad getPinpad() {
        Log.d(TAG, "getPinpad");

        AcessoDiretoPinpad pinpad;

        sLoadSemaphore.acquireUninterruptibly();

        if (sAcessoDiretoPinpad == null) {
            try {
                sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(CallbackUtility.getCallback());
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }

        pinpad = sAcessoDiretoPinpad;

        sLoadSemaphore.release();

        return pinpad;
    }

    private static byte[] intercept(boolean send, byte[] data, int length) {
        Log.d(TAG, "intercept");

        try {
            if (length > 4) {
                byte[] CMD_ID = new byte[3];

                System.arraycopy(data, 1, CMD_ID, 0, 3);

                if (send) {
                    switch (new String(CMD_ID)) {
                        case ABECS.OPN: case ABECS.GIX: case ABECS.CLX:
                        case ABECS.CEX: case ABECS.EBX: case ABECS.GTK: case ABECS.RMC:
                        case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                        case ABECS.GCX: case ABECS.GED: case ABECS.FCX:
                            /* Nothing to do */

                            // TODO: (GIX) rewrite requests that may include 0x8020 and 0x8021!?
                            break;

                        case ABECS.GPN:
                        case ABECS.GOX:
                            Context context = Application.getPackageContext();

                            context.startActivity(new Intent(context, PinCaptureActivity.class));

                            PinCaptureActivity.acquire();
                            break;

                        default:
                            Log.w(TAG, "intercept::NAK registered");

                            return new byte[] { 0x15 }; // TODO: NAK if CRC fails, .ERR010......... otherwise!?
                    }
                } else {
                    switch (new String(CMD_ID)) {
                        case ABECS.GPN:
                        case ABECS.GOX:
                            PinCaptureActivity.onNotificationThrow("", -1, -1);
                            /* no break */

                        default:
                            for (int i = 0; i < 4; i++) {
                                try {
                                    SunmiPayKernel.getInstance().mBasicOptV2.ledStatusOnDevice(i + 1, 1);
                                } catch (RemoteException exception) {
                                    Log.e(TAG, Log.getStackTraceString(exception));
                                }
                            }
                            break;
                    }
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

                output = intercept(false, output, result);
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

        if (length > 1) { /* 2021-08-11: not a control byte */
            CallbackUtility.setServiceCallback(callback);
        }

        int result = -1;

        try {
            byte[] request = intercept(true, input, length);

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
