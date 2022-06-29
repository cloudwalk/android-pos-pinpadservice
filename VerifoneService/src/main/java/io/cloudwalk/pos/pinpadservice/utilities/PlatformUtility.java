package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Bundle;

import org.json.JSONObject;

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

public class PlatformUtility {
    private static final String
            TAG = PlatformUtility.class.getSimpleName();

    private static final Semaphore
            sInterruptSemaphore = new Semaphore(1, true);

    private static final String[]
            sVerifoneService = {
                    "com.vfi.smartpos.deviceservice",
                    "com.verifone.smartpos.service.VerifoneDeviceService",
                    "com.vfi.smartpos.system_service",
                    "com.vfi.smartpos.system_service.SystemService"
            };

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    public static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    public static AcessoDiretoPinpad
            sAcessoDiretoPinpad = null;

    private PlatformUtility() {
        Log.d(TAG, "PlatformUtility");

        /* Nothing to do */
    }

    private static AcessoDiretoPinpad _getPinpad() {
        // Log.d(TAG, "_getPinpad");

        Semaphore semaphore = new Semaphore(0, true);

        if (sAcessoDiretoPinpad == null) {
            ServiceUtility.register(sVerifoneService[0], sVerifoneService[1], new ServiceUtility.Callback() {
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

        return sAcessoDiretoPinpad;
    }

    private static void _process(Bundle bundle) {
        Log.d(TAG, "_process");

        try {
            sRecvSemaphore.acquireUninterruptibly();

            while (sResponseQueue.poll() != null);

            byte[] buffer = new byte[2048];
            int    count  = 0;

            do {
                if (!sInterruptSemaphore.tryAcquire(0, SECONDS)) {
                    break;
                }

                try {
                    int timeout = (count != 0) ? 0 : 2000;

                    count = _getPinpad().recebeResposta(buffer, timeout);

                    if (count > 0) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        stream.write(buffer, 0, count);

                        Bundle response = new Bundle();

                        response.putString   ("application_id", bundle.getString("application_id"));
                        response.putByteArray("response",       stream.toByteArray());

                        if (timeout != 0 || count != 1) {
                            sResponseQueue.add(response);
                        }

                        if (buffer[0] != 0x06) { break; }
                    }
                } finally {
                    sInterruptSemaphore.release();
                }
            } while (count++ <= 1);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            PinCaptureActivity.finishActivity();

            sRecvSemaphore.release();
        }
    }

    public static int interrupt(Bundle bundle) {
        Log.d(TAG, "interrupt");

        sInterruptSemaphore.acquireUninterruptibly();
        sRecvSemaphore     .acquireUninterruptibly();
        sInterruptSemaphore.release();
        sRecvSemaphore     .release();

        return send(bundle);
    }

    public static int send(Bundle bundle) {
        Log.d(TAG, "send");

        String applicationId = bundle.getString   ("application_id");
        byte[] stream        = bundle.getByteArray("request");

        try {
            if (stream.length > 1) {
                switch ((new JSONObject(bundle.getString("request_json", ""))).optString(ABECS.CMD_ID)) {
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
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        int status = _getPinpad().enviaComando(stream, stream.length);

        if (status >= 0) {
            new Thread() {
                @Override
                public void run() { super.run(); _process(bundle); }
            }.start();
        }

        return status;
    }
}
