package io.cloudwalk.pos.pinpadservice.managers;

import static java.util.Locale.US;

import android.os.IBinder;

import com.vfi.smartpos.deviceservice.aidl.IDeviceService;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    public static final String
            ACTION_VFSERVICE = "com.verifone.smartpos.service.VerifoneDeviceService";

    public static final String
            PACKAGE_VFSERVICE = "com.vfi.smartpos.deviceservice";

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
                    ServiceUtility.register(PACKAGE_VFSERVICE, ACTION_VFSERVICE, new ServiceUtility.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess");
                        }

                        @Override
                        public void onFailure() {
                            Log.d(TAG, "onFailure");

                            this.onSuccess();
                        }
                    });

                    sAcessoDiretoPinpad = GestaoBibliotecaPinpad.obtemInstanciaAcessoDiretoPinpad(CallbackUtility.getCallback());
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                }

                release(sMngrSemaphore);
            }
        }.start();
    }

    private static AcessoDiretoPinpad getPinpad() {
        Log.d(TAG, "getPinpad");

        AcessoDiretoPinpad pinpad;

        acquire(sMngrSemaphore);

        pinpad = sAcessoDiretoPinpad;

        release(sMngrSemaphore);

        return pinpad;
    }

    private static byte[] intercept(String application, boolean send, byte[] data, int length) {
        Log.d(TAG, "intercept::length [" + length + "] (" + ((send) ? "send" : "recv") + ")");

        try {
            acquire(sMngrSemaphore);

            String CMD_ID = "UNKNOWN";

            switch (length) {
                case 0:
                    return data;

                case 1:
                    Log.d(TAG, "intercept::data[0] [" + String.format(US, "%02X", data[0]) + "]");

                    if (data[0] != 0x04) {
                        return data;
                    }

                    CMD_ID = "EOT";
                    break;

                default:
                    if (length >= 3) {
                        byte[] slice = new byte[3];

                        System.arraycopy(data, 1, slice, 0, 3);

                        CMD_ID = new String(slice);
                    }
            }

            Log.d(TAG, "intercept::CMD_ID [" + CMD_ID + "]");

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
                        PinCaptureActivity.startActivity(application);
                        break;

                    default:
                        Log.w(TAG, "intercept::NAK registered");

                        return new byte[] { 0x15 }; // TODO: NAK if CRC fails, .ERR010......... otherwise!?
                }
            } else {
                switch (CMD_ID) {
                    case "EOT":
                    case ABECS.GPN:
                    case ABECS.GOX:
                        PinCaptureActivity.finishActivity();
                        break;

                    default:
                        /* Nothing to do */
                        break;
                }

                try {
                    IBinder        service = ServiceUtility.retrieve(PACKAGE_VFSERVICE, ACTION_VFSERVICE);
                    IDeviceService  device = IDeviceService.Stub.asInterface(service);

                    for (int i = 0; i < 4; i++) {
                        device.getLed().turnOff(i + 1);
                    }
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                }
            }
        } finally {
            release(sMngrSemaphore);

            Log.h(TAG, data, length);
        }

        return data;
    }

    private static void acquire(Semaphore semaphore) {
        Log.d(TAG, "acquire::semaphore [" + semaphore + "]");

        semaphore.acquireUninterruptibly();
    }

    private static void release(Semaphore semaphore) {
        Log.d(TAG, "release");

        if (semaphore.availablePermits() <= 0) {
            semaphore.release();
        }

        Log.d(TAG, "release::semaphore.availablePermits() [" + semaphore.availablePermits() + "]");
    }

    public static PinpadManager getInstance() {
        Log.d(TAG, "getInstance");

        return sPinpadManager;
    }

    @Override
    public int recv(byte[] output, long timeout) {
        Log.d(TAG, "recv::timeout [" + timeout + "]");

        acquire(sRecvSemaphore);

        int result = -1;

        try {
            byte[] response = sQueue.poll();

            if (response != null) {
                System.arraycopy(response, 0, output, 0, response.length);

                result = response.length;

                Log.h(TAG, output, result);
            } else {
                result = getPinpad().recebeResposta(output, timeout);

                output = intercept(null, false, output, result);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        release(sRecvSemaphore);

        return result;
    }

    @Override
    public int send(String application, IServiceCallback callback, byte[] input, int length) {
        Log.d(TAG, "send");

        acquire(sSendSemaphore);

        Log.d(TAG, "send::application [" + application + "]");

        if (length > 1) { /* 2021-08-11: not a control byte */
            CallbackUtility.setServiceCallback(callback);
        }

        int result = -1;

        try {
            byte[] request = intercept(application, true, input, length);

            if (request[0] != 0x15) {
                result = getPinpad().enviaComando(request, length);
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
