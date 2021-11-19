package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

    public static final BlockingQueue<byte[]>
            sResponseQueue = new LinkedBlockingQueue<>();

    private PinpadManager() {
        Log.d(TAG, "PinpadManager");
    }

    private static byte[] intercept(Bundle bundle)
            throws Exception {
        Log.d(TAG, "interceptRequest");

        String applicationId = bundle.getString   ("application_id");
        byte[] dataPacket    = bundle.getByteArray("request");

        Log.h(TAG, dataPacket, dataPacket.length);

        if (dataPacket.length != 1) {
            Bundle request = PinpadUtility.parseRequestDataPacket(dataPacket, dataPacket.length);
                   request = (request != null) ? request : new Bundle();

            switch (request.getString(ABECS.CMD_ID, "UNKNOWN")) {
                case ABECS.OPN: case ABECS.GIX: case ABECS.CLX:
                case ABECS.CEX: case ABECS.CHP: case ABECS.EBX: case ABECS.GCD:
                case ABECS.GTK: case ABECS.MNU: case ABECS.GPN: case ABECS.RMC:
                case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                    return dataPacket;

                default:
                    return new byte[] { 0x15 };
            }
        } else {
            switch (dataPacket[0]) {
                case 0x04: /* EOT */
                case 0x15: /* NAK */
                    return dataPacket;

                default:
                    return new byte[] { 0x15 };
            }
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

        sRecvSemaphore.acquireUninterruptibly();

        byte[] response = sResponseQueue.poll();

        try {
            if (response != null) {
                result = response.length;
            } else {
                // TODO: retrieve response
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            if (result > 0) {
                byte[] courrier = new byte[result];

                System.arraycopy(response, 0, courrier, 0, result);

                bundle.putByteArray("response", courrier);
            }

            Log.h(TAG, response, result);
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
            byte[] request = intercept(bundle);

            if (request.length != 1) {
                CallbackUtility.setServiceCallback(callback);
            }

            if (request[0] != 0x15) {
                // TODO: process request
            } else {
                result = (sResponseQueue.offer(request)) ? 0 : -1;
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        sSendSemaphore.release();

        return result;
    }
}
