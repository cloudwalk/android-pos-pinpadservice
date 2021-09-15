package io.cloudwalk.pos.pinpadservice.managers;

import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.NTF_PIN_FINISH;
import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.NTF_PIN_START;

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

                sMngrSemaphore.acquireUninterruptibly();

                try {
                    sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(CallbackUtility.getCallback());

                    Context context = Application.getPackageContext();

                    context.startActivity(new Intent(context, PinCaptureActivity.class));

                    PinCaptureActivity.acquire();
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                }

                sMngrSemaphore.release();
            }
        }.start();
    }

    private static AcessoDiretoPinpad getPinpad() {
        Log.d(TAG, "getPinpad");

        AcessoDiretoPinpad pinpad;

        sMngrSemaphore.acquireUninterruptibly();

        pinpad = sAcessoDiretoPinpad;

        sMngrSemaphore.release();

        return pinpad;
    }

    private static byte[] intercept(boolean send, byte[] data, int length) {
        Log.d(TAG, "intercept");

        try {
            sMngrSemaphore.acquireUninterruptibly();

            if (length > 4) {
                byte[] slice = new byte[3];

                System.arraycopy(data, 1, slice, 0, 3);

                String CMD_ID = new String(slice);

                Log.d(TAG, "intercept::CMD_ID [" + CMD_ID + "]");

                if (send) {
                    switch (CMD_ID) {
                        case ABECS.OPN: case ABECS.GIX: case ABECS.CLX:
                        case ABECS.CEX: case ABECS.EBX: case ABECS.GTK: case ABECS.RMC:
                        case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                        case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                            // TODO: (GIX) rewrite requests that may include 0x8020 and 0x8021!?
                            break;

                        case ABECS.GPN:
                            PinCaptureActivity.onNotificationThrow(NTF_PIN_START);
                            break;

                        default:
                            Log.w(TAG, "intercept::NAK registered");

                            return new byte[] { 0x15 }; // TODO: NAK if CRC fails, .ERR010......... otherwise!?
                    }
                } else {
                    switch (CMD_ID) {
                        case ABECS.GPN:
                            PinCaptureActivity.onNotificationThrow(NTF_PIN_FINISH);
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
            sMngrSemaphore.release();

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

                Log.h(TAG, output, result);
            } else {
                result = getPinpad().recebeResposta(output, timeout);

                output = intercept(false, output, result);
            }
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
