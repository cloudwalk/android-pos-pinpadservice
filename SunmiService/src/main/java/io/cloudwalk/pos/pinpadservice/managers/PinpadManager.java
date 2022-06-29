package io.cloudwalk.pos.pinpadservice.managers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static io.cloudwalk.pos.pinpadservice.utilities.PlatformUtility.sResponseQueue;

import android.os.Bundle;
import android.os.SystemClock;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.IPinpadManager;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.pinpadservice.utilities.CallbackUtility;
import io.cloudwalk.pos.pinpadservice.utilities.PlatformUtility;

public class PinpadManager extends IPinpadManager.Stub {
    private static final String
            TAG = PinpadManager.class.getSimpleName();

    private static final PinpadManager
            sPinpadManager = new PinpadManager();

    private static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private static final Semaphore
            sSendSemaphore = new Semaphore(1, true);

    private PinpadManager() {
        Log.d(TAG, "PinpadManager");
    }

    public static PinpadManager getInstance() {
        // Log.d(TAG, "getInstance");

        return sPinpadManager;
    }

    @Override
    public int recv(@NotNull Bundle bundle) {
        // Log.d(TAG, "recv");

        long timeout   = bundle.getLong("timeout", 0);
             timeout   = (timeout < 0) ? 0 : timeout;

        long timestamp = SystemClock.elapsedRealtime() + timeout;
        int  result    = 0;

        try {
            if (!sRecvSemaphore.tryAcquire(timeout, MILLISECONDS)) {
                return result;
            }

            try {
                timeout = (timestamp - SystemClock.elapsedRealtime());
                timeout = (timeout < 0) ? 0 : timeout;

                Bundle response = sResponseQueue.poll(timeout, MILLISECONDS);

                if (response != null) {
                    bundle.putString   ("application_id", response.getString   ("application_id"));
                    bundle.putByteArray("response",       response.getByteArray("response"));

                    return bundle.getByteArray("response").length;
                }
            } finally {
                sRecvSemaphore.release();
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            result = -1;
        }

        return result;
    }

    @Override
    public int send(@NotNull Bundle bundle, IServiceCallback callback) {
        // Log.d(TAG, "send");

        sSendSemaphore.acquireUninterruptibly();

        int result = -1;

        String applicationId = bundle.getString   ("application_id");
        byte[] stream        = bundle.getByteArray("request");

        try {
            switch (stream[0]) {
                case 0x18:
                    if (stream.length == 1) {
                        result = PlatformUtility.interrupt(bundle);
                        break;
                    }
                    /* no break; */

                default:
                    JSONObject request;

                    try {
                        request = new JSONObject(PinpadUtility.parseRequestDataPacket(stream, stream.length));

                        bundle.putString("request_json", request.toString());
                    } catch (Exception exception) {
                        request = new JSONObject();

                        Log.e(TAG, Log.getStackTraceString(exception));
                    }

                    CallbackUtility.setServiceCallback(callback);

                    String CMD_ID = (request.has(ABECS.CMD_ID) ? request.getString(ABECS.CMD_ID) : "UNKNOWN");

                    switch (CMD_ID) {
                        case ABECS.OPN: case ABECS.GIX: case ABECS.CLX:
                        case ABECS.CEX: case ABECS.CHP: case ABECS.EBX: case ABECS.GCD:
                        case ABECS.GPN: case ABECS.GTK: case ABECS.MNU: case ABECS.RMC:
                        case ABECS.TLI: case ABECS.TLR: case ABECS.TLE:
                        case ABECS.GCX: case ABECS.GED: case ABECS.GOX: case ABECS.FCX:
                            result = PlatformUtility.send(bundle);
                            break;

                        default:
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();

                                    PlatformUtility.sRecvSemaphore.acquireUninterruptibly();

                                    // TODO: review test case A003

                                    Bundle response = new Bundle();

                                    response.putString   ("application_id", applicationId);
                                    response.putByteArray("response",       new byte[] { 0x15 });

                                    while (sResponseQueue.poll() != null);

                                    sResponseQueue.add(response);

                                    PlatformUtility.sRecvSemaphore.release();
                                }
                            }.start();

                            result = 0;
                            break;
                    }
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        } finally {
            sSendSemaphore.release();
        }

        return result;
    }
}
