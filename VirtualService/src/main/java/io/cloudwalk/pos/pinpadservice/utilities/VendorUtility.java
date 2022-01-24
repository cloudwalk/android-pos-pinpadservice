package io.cloudwalk.pos.pinpadservice.utilities;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;

public class VendorUtility {
    private static final String
            TAG = VendorUtility.class.getSimpleName();

    private static Socket
            sClient = null;

    public static final BlockingQueue<Bundle>
            sResponseQueue = new LinkedBlockingQueue<>();

    public static final Semaphore
            sVendorSemaphore = new Semaphore(1, true);

    private VendorUtility() {
        Log.d(TAG, "VendorUtility");
    }

    public static int abort(Bundle bundle) { return send(bundle); }

    public static int send(Bundle bundle) {
        Log.d(TAG, "send");

        try {
            String address  = SharedPreferencesUtility.readIPv4();
            int    delim    = address.indexOf(":");

            String host     = address.substring(0, delim);
            int    port     = Integer.parseInt(address.substring(delim + 1));

            byte[] request  = bundle.getByteArray("request");

            if (sClient != null && sClient.isConnected() && !sClient.isClosed()) {
                sClient.close();

                Log.d(TAG, "send::" + sClient + " (close) (overlapping)");
            }

            sClient = new Socket();

            sClient.setPerformancePreferences(2, 1, 0);

            sClient.connect(new InetSocketAddress(host, port), 2000);

            OutputStream output = sClient.getOutputStream();

            output.write(request);
            output.flush();

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    try {
                        sVendorSemaphore.acquireUninterruptibly();

                        while (true) {
                            if (sResponseQueue.poll() == null) { break; }
                        }

                        InputStream input = sClient.getInputStream();

                        byte[] buffer = new byte[2048];
                        int    count  = 0;

                        do {
                            sClient.setSoTimeout((count != 0) ? 0 : 2000);

                            count = input.read(buffer, 0, buffer.length);

                            if (count >= 0) {
                                stream.write(buffer, 0, count);

                                Bundle response = new Bundle();

                                String applicationId = bundle.getString("application_id");

                                response.putString   ("application_id", applicationId);
                                response.putByteArray("response",       stream.toByteArray());

                                sResponseQueue.add(response);

                                if (buffer[0] == 0x04 || buffer[0] == 0x15) { return; }
                            }
                        } while (count <= 1);
                    } catch (Exception exception) {
                        Log.e(TAG, Log.getStackTraceString(exception));
                    } finally {
                        try { sClient.close(); } catch (Exception exception) { Log.e(TAG, Log.getStackTraceString(exception)); }

                        sVendorSemaphore.release();
                    }
                }
            }.start();

            return request.length;
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            return -1;
        }
    }
}
