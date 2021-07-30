package io.cloudwalk.pos.demo;

import android.net.wifi.WifiManager;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.demo.presentation.MainActivity;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.Application;

import static android.content.Context.WIFI_SERVICE;

public class PinpadServer {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PinpadServer.Callback sCallback = null;

    private Semaphore sSemaphore = new Semaphore(1, true);

    private ServerSocket sServerSocket = null;

    private WifiManager sWifiManager = null;

    private WifiManager.WifiLock sWifiLock = null;

    public static interface Callback {
        void onFailure(Exception exception);

        void onRecv(byte[] trace, int length);

        void onSend(byte[] trace, int length);

        void onSuccess(String localSocket);
    }

    private InetAddress getInetAddress()
            throws Exception {
        Log.d(TAG, "getInetAddress");

        int ip = sWifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        return InetAddress.getByAddress(BigInteger.valueOf(ip).toByteArray());
    }

    private void accept(InetAddress currentAddress) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    String localSocketAddress = sServerSocket.getLocalSocketAddress().toString();

                    sSemaphore.release();

                    sCallback.onSuccess(localSocketAddress);

                    while (true) {
                        Log.d(TAG, "Waiting for a client...");

                        Socket client = null;

                        try {
                            sSemaphore.acquireUninterruptibly();

                            client = sServerSocket.accept();
                        } catch (SocketTimeoutException warning) {
                            String[] hostAddress = { currentAddress.getHostAddress(), getInetAddress().getHostAddress() };

                            // TODO: handle host address change?

                            continue;
                        } finally {
                            sSemaphore.release();
                        }

                        Log.d(TAG, "client.getHostAddress() [" + client.getInetAddress().getHostAddress() + "]");

                        DataInputStream  input  = new DataInputStream (new BufferedInputStream (client.getInputStream ()));
                        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));

                        byte[] buffer = new byte[2048];

                        int count = 0;

                        while ((count = input.read(buffer)) > 0) {
                            sCallback.onRecv(buffer, count);

                            output.write(buffer, 0, count);
                            output.flush();

                            sCallback.onSend(buffer, count);

                            /* TODO: replicate PinpadManager.request(Bundle) logic

                            sCallback.onRecv(buffer, count);

                            PinpadManager.send(buffer, count);

                            byte[] response = new byte[2048];

                            count = PinpadManager.receive(response, 60000);

                            output.write(response, 0, count);
                            output.flush();

                            sCallback.onSend(response, count);

                             */
                        }

                        // TODO: PinpadManager.send(...)
                        // TODO: PinpadManager.receive(...)
                    }
                } catch (Exception exception) {
                    close(exception);
                }
            }
        }.start();
    }

    private void close(Exception exception) {
        Log.d(TAG, "close");

        sSemaphore.acquireUninterruptibly();

        if (sServerSocket != null) {
            try {
                sServerSocket.close();
            } catch (Exception warning) {
                Log.w(TAG, Log.getStackTraceString(warning));
            }
        }

        if (sWifiLock != null) {
            if (sWifiLock.isHeld()) {
                sWifiLock.release();
            }
        }

        sSemaphore.release();

        if (exception != null) {
            sCallback.onFailure(exception);
        }
    }

    public PinpadServer(@NotNull PinpadServer.Callback callback) {
        Log.d(TAG, "PinpadServer");

        sSemaphore.acquireUninterruptibly();

        sCallback = callback;

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    sWifiManager = (WifiManager) Application.getPackageContext()
                            .getApplicationContext().getSystemService(WIFI_SERVICE);

                    InetAddress inetAddress = getInetAddress();

                    sServerSocket = new ServerSocket(8080, 1, inetAddress);

                    sServerSocket.setSoTimeout(200);

                    if (sWifiLock == null) {
                        sWifiLock = sWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);
                    }

                    sWifiLock.acquire();

                    accept(inetAddress);
                } catch (Exception exception) {
                    sSemaphore.release();

                    close(exception);
                }
            }
        }.start();
    }

    public void close() {
        Log.d(TAG, "close");

        close(null);
    }
}
