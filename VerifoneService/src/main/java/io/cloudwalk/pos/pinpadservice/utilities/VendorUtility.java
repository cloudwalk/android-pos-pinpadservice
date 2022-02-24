package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

public class VendorUtility {
    private static final String
            TAG = VendorUtility.class.getSimpleName();

    private static AcessoDiretoPinpad
            sAcessoDiretoPinpad = null;

    private static final Semaphore[]
            sSemaphore = {
                    new Semaphore(1, true),
                    new Semaphore(1, true)
            };

    private static final String[]
            sService = {
                    "com.vfi.smartpos.deviceservice",
                    "com.verifone.smartpos.service.VerifoneDeviceService",
                    "com.vfi.smartpos.system_service",
                    "com.vfi.smartpos.system_service.SystemService"
            };

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    private VendorUtility() {
        Log.d(TAG, "VendorUtility");

        /* Nothing to do */
    }

    private static AcessoDiretoPinpad getPinpad() {
        // Log.d(TAG, "getPinpad");

        AcessoDiretoPinpad pinpad;

        Semaphore semaphore = new Semaphore(0, true);

        if (sAcessoDiretoPinpad == null) {
            ServiceUtility.register(sService[0], sService[1], new ServiceUtility.Callback() {
                @Override
                public void onSuccess() {
                    try {
                        sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(CallbackUtility.getCallback());
                    } catch (Exception exception) {
                        Log.e(TAG, Log.getStackTraceString(exception));
                    }

                    semaphore.release();
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "onFailure");

                    this.onSuccess();
                }
            });

            semaphore.acquireUninterruptibly();
        }

        pinpad = sAcessoDiretoPinpad;

        return pinpad;
    }

    private static void recv(Bundle bundle) {
        Log.d(TAG, "recv");

        try {
            sSemaphore[1].acquire();

            String applicationId = bundle.getString   ("application_id");
            byte[] request       = bundle.getByteArray("request");
            Bundle requestBundle = bundle.getBundle   ("request_bundle");

            while (true) {
                if (sResponseQueue.poll() == null) { break; }
            }

            byte[] buffer = new byte[2048];
            int    count  = 0;

            do {
                int timeout = (count != 0) ? 0 : 2000;

                if (!sSemaphore[0].tryAcquire(0, SECONDS)) {
                    break;
                }

                try {
                    count = getPinpad().recebeResposta(buffer, timeout);

                    if (count > 0) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        stream.write(buffer, 0, count);

                        Bundle response = new Bundle();

                        response.putString   ("application_id", applicationId);
                        response.putByteArray("response",       stream.toByteArray());

                        if (timeout != 0 || count != 1) {
                            sResponseQueue.add(response);
                        }

                        if (buffer[0] != 0x06) { return; }
                    }
                } finally {
                    sSemaphore[0].release();
                }
            } while (count++ <= 1);

            if (request.length > 1) {
                switch (requestBundle.getString(ABECS.CMD_ID)) {
                    case ABECS.GPN:
                    case ABECS.GOX:
                        PinCaptureActivity.finishActivity();
                        break;

                    default:
                        /* Nothing to do */
                        break;
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            sSemaphore[1].release();
        }
    }

    public static int abort(Bundle bundle) {
        Log.d(TAG, "abort");

        sSemaphore[0].acquireUninterruptibly();
        sSemaphore[1].acquireUninterruptibly();
        sSemaphore[1].release();
        sSemaphore[0].release();

        return send(bundle);
    }

    public static int send(Bundle bundle) {
        Log.d(TAG, "send");

        String applicationId = bundle.getString   ("application_id");
        byte[] request       = bundle.getByteArray("request");
        Bundle requestBundle = bundle.getBundle   ("request_bundle");

        if (request.length > 1) {
            switch (requestBundle.getString(ABECS.CMD_ID)) {
                case ABECS.GPN:
                case ABECS.GOX:
                    PinCaptureActivity.startActivity(applicationId);
                    break;

                // TODO: intercept MNU and GCD

                default:
                    /* Nothing to do */
                    break;
            }
        }

        int status = getPinpad().enviaComando(request, request.length);

        if (status >= 0) {
            new Thread() {
                @Override
                public void run() { super.run(); recv(bundle); }
            }.start();
        }

        return status;
    }
}
