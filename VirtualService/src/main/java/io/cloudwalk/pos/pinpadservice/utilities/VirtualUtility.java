package io.cloudwalk.pos.pinpadservice.utilities;

import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;
import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.NTF;
import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.NTF_MSG;
import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.NTF_TYPE;

import android.os.Bundle;
import android.os.NetworkOnMainThreadException;

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

public class VirtualUtility {
    private static final String
            TAG = VirtualUtility.class.getSimpleName();

    private static Socket
            sPinpadSocket = null;

    private static final Semaphore
            sAbortSemaphore = new Semaphore(1, true);

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    public static final Semaphore
            sRecvSemaphore = new Semaphore(1, true);

    private VirtualUtility() {
        Log.d(TAG, "VirtualUtility");
    }

    private static byte[] _intercept(String action, byte[] stream) {
        // Log.d(TAG, "_intercept");

        try {
            if (stream.length < 3) {
                return stream;
            }

            switch (action) {
                case "recv":
                    Bundle response = PinpadUtility.parseResponseDataPacket(stream, stream.length);

                    switch (response.getString(ABECS.RSP_ID, "UNKNOWN")) {
                        case ABECS.GIX:
                            if (!response.containsKey(ABECS.PP_MODEL)) {
                                break;
                            }

                            response.putString(ABECS.PP_MODEL, String.format(US, "%.20s", "VIRTUAL/" + response.getString(ABECS.PP_MODEL)));

                            Log.d(TAG, ABECS.PP_MODEL + "[" + response.getString(ABECS.PP_MODEL) + "]");

                            // TODO: return PinpadUtility.buildResponseDataPacket(response);

                        default:
                            /* Nothing to do */
                            break;
                    }

                default:
                    /* Nothing to do */
                    break;
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        return stream;
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
                            int timeout = (count != 0) ? 200 : 2000; // 2022-02-24: Java socket implementations take
                                                                     // `0` as no timeout whatsoever

                            pinpadSocket.setSoTimeout(timeout);

                            try {
                                count = pinpadSocket.getInputStream().read(buffer, 0, buffer.length);
                            } catch (SocketTimeoutException exception) {
                                count = 0;

                                if (timeout != 200) { throw exception; }
                            }

                            if (count > 0) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                                stream.write(buffer, 0, count);

                                Bundle response = new Bundle();

                                response.putString   ("application_id", applicationId);
                                response.putByteArray("response",       _intercept("recv", stream.toByteArray()));

                                if (timeout != 200 || count != 1) {
                                    sResponseQueue.add(response);
                                }

                                if (buffer[0] != 0x06) { break; }

                                try {
                                    String CMD_ID = requestBundle.getString(ABECS.CMD_ID);

                                    Bundle callback = new Bundle();

                                    callback.putString(NTF_MSG,  String.format(US, "\nPROCESSING %s\n/%s", CMD_ID, pinpadSocket.getInetAddress().getHostAddress()));
                                    callback.putInt   (NTF_TYPE, NTF);

                                    CallbackUtility.getServiceCallback().onServiceCallback(callback);
                                } catch (NullPointerException exception) {
                                    /* Nothing to do */
                                }
                            }
                        } finally {
                            sAbortSemaphore.release();
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
