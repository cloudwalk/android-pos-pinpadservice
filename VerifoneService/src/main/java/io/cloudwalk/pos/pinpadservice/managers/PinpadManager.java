package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;
import android.os.IBinder;

import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.ILed;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.AcessoDiretoPinpad;
import br.com.verifone.bibliotecapinpad.GestaoBibliotecaPinpad;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.R;
// import io.cloudwalk.pos.pinpadservice.presentation.PinCaptureActivity;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

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

                sMngrSemaphore.acquireUninterruptibly();

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

    private static byte[] intercept(String application, boolean send, byte[] data, int length) {
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
                        case ABECS.CEX: case ABECS.CHP: case ABECS.EBX: case ABECS.GCD:
                        case ABECS.GTK: case ABECS.MNU: case ABECS.RMC:
                        case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                        case ABECS.GCX: case ABECS.GED: case ABECS.FCX:
                            // TODO: (GIX) rewrite requests that may include 0x8020 and 0x8021!?
                            break;

                        case ABECS.GPN:
                        case ABECS.GOX:
                            Bundle bundle = new Bundle();

                            if (!application.equals    ("io.cloudwalk.pos.poc2104301453.demo")
                                    && application.startsWith("io.cloudwalk.")) {
                                Log.d(TAG, "intercept::infinitepay [" + application + "]");

                                bundle.putInt("activity_pin_capture", R.layout.infinitepay_activity_pin_capture);
                                bundle.putInt("rl_pin_capture", R.id.infinitepay_rl_pin_capture);
                                // bundle.putInt("keyboard_custom_pos00", R.id.infinitepay_keyboard_custom_pos00);
                            }

                            if (bundle.isEmpty()) {
                                Log.d(TAG, "intercept::default [" + application + "]");

                                bundle.putInt("activity_pin_capture", R.layout.default_activity_pin_capture);
                                bundle.putInt("rl_pin_capture", R.id.default_rl_pin_capture);
                                // bundle.putInt("keyboard_custom_pos00", R.id.default_keyboard_custom_pos00);
                            }

                            // TODO: PinCaptureActivity.startActivity(bundle);
                            break;

                        default:
                            Log.w(TAG, "intercept::NAK registered");

                            return new byte[] { 0x15 }; // TODO: NAK if CRC fails, .ERR010......... otherwise!?
                    }
                } else {
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

                output = intercept(null, false, output, result);
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
            byte[] request = intercept(application, true, input, length);

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
