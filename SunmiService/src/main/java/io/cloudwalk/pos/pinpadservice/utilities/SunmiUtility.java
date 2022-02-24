package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.setis.sunmi.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;

public class SunmiUtility {
    private static final String
            TAG = SunmiUtility.class.getSimpleName();

    private static AcessoDiretoPinpad
            sAcessoDiretoPinpad = null;

    private static final Semaphore
            sAbortSemaphore = new Semaphore(1, true);

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    public static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private SunmiUtility() {
        Log.d(TAG, "SunmiUtility");

        /* Nothing to do */
    }

    private static AcessoDiretoPinpad getPinpad() {
        // Log.d(TAG, "getPinpad");

        AcessoDiretoPinpad pinpad;

        if (sAcessoDiretoPinpad == null) {
            try {
                sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(CallbackUtility.getCallback());
            } catch (Exception exception) {
                Log.e(TAG, Log.getStackTraceString(exception));
            }
        }

        pinpad = sAcessoDiretoPinpad;

        return pinpad;
    }

    private static void recv(Bundle bundle) {
        Log.d(TAG, "recv");

        try {
            sRecvSemaphore.acquireUninterruptibly();

            String applicationId = bundle.getString   ("application_id");
            byte[] request       = bundle.getByteArray("request");
            Bundle requestBundle = bundle.getBundle   ("request_bundle");

            while (sResponseQueue.poll() != null);

            byte[] buffer = new byte[2048];
            int    count  = 0;

            do {
                if (!sAbortSemaphore.tryAcquire(0, SECONDS)) {
                    break;
                }

                try {
                    int timeout = (count != 0) ? 0 : 2000;

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

                        if (buffer[0] != 0x06) { break; }
                    }
                } finally {
                    sAbortSemaphore.release();
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
            sRecvSemaphore.release();
        }
    }

    public static int abort(Bundle bundle) {
        Log.d(TAG, "abort");

        sAbortSemaphore.acquireUninterruptibly();
        sRecvSemaphore .acquireUninterruptibly();
        sAbortSemaphore.release();
        sRecvSemaphore .release();

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
