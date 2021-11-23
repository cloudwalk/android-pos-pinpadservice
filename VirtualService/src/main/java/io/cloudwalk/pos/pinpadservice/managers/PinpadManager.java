package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager
            sPinpadManager = new PinpadManager();

    private static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sSendSemaphore = new Semaphore(1, true);

    // public static final BlockingQueue<byte[]>
    //         sResponseQueue = new LinkedBlockingQueue<>();

    private PinpadManager() {
        Log.d(TAG, "PinpadManager");
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

        sRecvSemaphore.acquireUninterruptibly();

        try {
            // TODO: CMD

            // TODO: bundle.putByteArray("response", response);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sRecvSemaphore.release();

        return result;
    }

    @Override
    public int send(@NotNull Bundle bundle, IServiceCallback callback) {
        Log.d(TAG, "send");

        sSendSemaphore.acquireUninterruptibly();

        int result = -1;

        try {
            String applicationId = bundle.getString   ("application_id");
            byte[] request       = bundle.getByteArray("request");
            Bundle requestBundle = bundle.getBundle   ("request_bundle");

            switch (request[0]) {
                case 0x18: /* CAN */
                    // TODO: CAN
                    break;

                default:
                    if (requestBundle == null) {
                        requestBundle = PinpadUtility.parseRequestDataPacket(request, request.length);
                    }

                    CallbackUtility.setServiceCallback(callback);

                    switch (requestBundle.getString(ABECS.CMD_ID, "UNKNOWN")) {
                        case ABECS.OPN: case ABECS.GIX: case ABECS.CLX:
                        case ABECS.CEX: case ABECS.CHP: case ABECS.EBX: case ABECS.GCD:
                        case ABECS.GTK: case ABECS.MNU: case ABECS.GPN: case ABECS.RMC:
                        case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                        case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                            // TODO: CMD
                            break;

                        default:
                            throw new IllegalArgumentException();
                    }
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            // TODO: NAK
        }

        sSendSemaphore.release();

        return result;
    }
}
