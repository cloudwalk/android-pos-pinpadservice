package io.cloudwalk.pos.pinpadservice.managers;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager
            sPinpadManager = new PinpadManager();

    private static final Queue<byte[]>
            sQueue = new LinkedList<>();

    private static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sSendSemaphore = new Semaphore(1, true);

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

        byte[] response = sQueue.poll();

        try {
            if (response != null) {
                Log.h(TAG, response, result);

                result = response.length;
            } else {
                response = new byte[2048];

                // TODO: handle external response

                response[0] = 0x15;
                result      = 1;
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            if (result > 0) {
                byte[] courrier = new byte[result];

                System.arraycopy(response, 0, courrier, 0, result);

                bundle.putByteArray("response", courrier);
            }
        }

        sRecvSemaphore.release();

        return result;
    }

    @Override
    public int send(@NotNull Bundle bundle, IServiceCallback callback) {
        Log.d(TAG, "send");

        sSendSemaphore.acquireUninterruptibly();

        String applicationId = bundle.getString   ("application_id");
        byte[] request       = bundle.getByteArray("request");

        Log.d(TAG, "send::applicationId [" + applicationId + "]");

        if (request.length > 1) {
            CallbackUtility.setServiceCallback(callback);
        }

        int result = -1;

        try {
            // TODO: parse request

            request = new byte[] { 0x15 };

            if (request[0] != 0x15) {
                // TODO: send request for external processing
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
