package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_MSG;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.NTF_TYPE;
import static io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager.Callback.Type.NOTIFICATION;

import android.os.Bundle;
import android.os.NetworkOnMainThreadException;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;

public class PlatformUtility {
    private static final String
            TAG = PlatformUtility.class.getSimpleName();

    private static Socket
            sPinpadSocket = null;

    private static final Semaphore
            sInterruptSemaphore = new Semaphore(1, true);

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    public static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private PlatformUtility() {
        Log.d(TAG, "PlatformUtility");
    }

    private static byte[] _intercept(String action, byte[] array) {
        // Log.d(TAG, "_intercept");

        try {
            if (array.length < (1 + 6 + 1 + 2)) {
                return array;
            }

            if (action.equals("recv")) {
                JSONObject response = new JSONObject(PinpadUtility.parseResponseDataPacket(array, array.length));

                switch (response.getString(ABECS.RSP_ID)) {
                    case ABECS.GIX:
                        if (!response.has(ABECS.PP_MODEL)) {
                            break;
                        }

                        String PP_MODEL = response.getString(ABECS.PP_MODEL);

                        if (!PP_MODEL.contains("VIRTUAL//")) {
                            response.put(ABECS.PP_MODEL, String.format(US, "%.20s", "VIRTUAL//" + PP_MODEL));
                        }
                        /* no break */

                    default:
                        return PinpadUtility.buildResponseDataPacket(response.toString());
                }
            } else {
                JSONObject request = new JSONObject(PinpadUtility.parseRequestDataPacket(array, array.length));

                return PinpadUtility.buildRequestDataPacket(request.toString());
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        return array;
    }

    private static int _route(Bundle bundle)
            throws Exception {
        // Log.d(TAG, "_route");

        String address  = SharedPreferencesUtility.readIPv4();
        int    delim    = address.indexOf(":");

        String host     = address.substring(0, delim);
        int    port     = Integer.parseInt(address.substring(delim + 1));

        byte[] request  = bundle.getByteArray("request");

        if (address.contains("127.0.0.1") || address.contains("0.0.0.0")) {
            // TODO: return MockUtility.send(bundle);
        }

        if (sPinpadSocket != null
                && sPinpadSocket.isConnected() && !sPinpadSocket.isClosed()) {
            sPinpadSocket.close();

            Log.d(TAG, "_route::" + sPinpadSocket + " (close) (overlapping)");
        }

        sPinpadSocket = new Socket();

        sPinpadSocket.setPerformancePreferences(2, 1, 0);
        sPinpadSocket.setKeepAlive(true);

        sPinpadSocket.connect(new InetSocketAddress(host, port), 2000);

        sPinpadSocket.getOutputStream().write(request);
        sPinpadSocket.getOutputStream().flush();

        new Thread() {
            @Override
            public void run() {
                super.run();

                Socket pinpadSocket = sPinpadSocket;

                try {
                    sRecvSemaphore.acquireUninterruptibly();

                    while (sResponseQueue.poll() != null);

                    byte[] array = new byte[2048];
                    int    count = 0;

                    do {
                        if (!sInterruptSemaphore.tryAcquire(0, SECONDS)) {
                            break;
                        }

                        try {
                            String applicationId = bundle.getString("application_id");

                            int timeout = (count != 0) ? 200 : 2000; // 2022-02-24: Java socket implementations take
                                                                     // `0` as no timeout whatsoever

                            pinpadSocket.setSoTimeout(timeout);

                            try {
                                count = pinpadSocket.getInputStream().read(array, 0, array.length);
                            } catch (SocketTimeoutException exception) {
                                count = 0;

                                if (timeout != 200) { throw exception; }
                            }

                            if (count > 0) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                                stream.write(array, 0, count);

                                Bundle response = new Bundle();

                                response.putString   ("application_id", applicationId);
                                response.putByteArray("response",       _intercept("recv", stream.toByteArray()));

                                if (timeout != 200 || count != 1) {
                                    sResponseQueue.add(response);
                                }

                                if (array[0] != 0x06) { break; }

                                try {
                                    String CMD_ID = (new JSONObject(bundle.getString("request_json"))).getString(ABECS.CMD_ID);

                                    Bundle callback = new Bundle();

                                    String message = String.format(US, "\nPROCESSING %s\n/%s", CMD_ID, pinpadSocket.getInetAddress().getHostAddress());

                                    Log.d(TAG, "message:: [" + message + "]");

                                    callback.putString(NTF_MSG,  message);
                                    callback.putInt   (NTF_TYPE, NOTIFICATION.ordinal());

                                    CallbackUtility.getServiceCallback().onServiceCallback(callback);
                                } catch (NullPointerException ignored) { }
                            }
                        } finally {
                            sInterruptSemaphore.release();
                        }
                    } while (count++ <= 1);
                } catch (Exception exception) {
                    Log.e(TAG, Log.getStackTraceString(exception));
                } finally {
                    try { pinpadSocket.close(); } catch (Exception exception) { Log.e(TAG, Log.getStackTraceString(exception)); }

                    sRecvSemaphore.release();
                }
            }
        }.start();

        return request.length;
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

        int[] status = { -1 };

        try {
            return _route(bundle);  // 2021-01-25: in theory, none should consume the service in
                                    // the main thread
        } catch (NetworkOnMainThreadException exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            Semaphore semaphore = new Semaphore(0, true);

            new Thread() {          // 2021-01-25: bypass `NetworkOnMainThreadException` for those
                @Override           // consuming the service in the main thread
                public void run() {
                    super.run();

                    try {
                        status[0] = _route(bundle);
                    } catch (Exception exception) {
                        Log.e(TAG, Log.getStackTraceString(exception));
                    } finally {
                        semaphore.release();
                    }
                }
            }.start();

            semaphore.acquireUninterruptibly();

            return status[0];
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        return -1;
    }
}
