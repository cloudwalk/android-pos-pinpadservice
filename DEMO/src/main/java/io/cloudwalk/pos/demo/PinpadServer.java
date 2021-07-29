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

import io.cloudwalk.pos.demo.presentation.MainActivity;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.utilitieslibrary.Application;

import static android.content.Context.WIFI_SERVICE;

public class PinpadServer {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ServerSocket sServerSocket = null;

    private WifiManager.WifiLock sWifiLock = null;

    public static interface Callback {
        void onSuccess(String address, String backlog);

        void onFailure(Exception exception);
    }

    private InetAddress getInetAddress(WifiManager wifiManager)
            throws Exception {
        Log.d(TAG, "getInetAddress");

        int ip = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        return InetAddress.getByAddress(BigInteger.valueOf(ip).toByteArray());
    }

    public PinpadServer(@NotNull PinpadServer.Callback callback) {
        Log.d(TAG, "PinpadServer");

        try {
            WifiManager wifiManager = (WifiManager) Application.getPackageContext().getSystemService(WIFI_SERVICE);

            InetAddress inetAddress = getInetAddress(wifiManager);

            sServerSocket = new ServerSocket(8080, 1, inetAddress);

            sServerSocket.setSoTimeout(2000);

            if (sWifiLock == null) {
                sWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);

                sWifiLock.acquire();
            }

            callback.onSuccess(sServerSocket.getLocalSocketAddress().toString(), "1");

            // TODO: create new thread?

            while (true) {
                Log.d(TAG, "Waiting for a client...");

                Socket client = null;

                try {
                    client = sServerSocket.accept();
                } catch (SocketTimeoutException warning) {
                    String[] hostAddress = { inetAddress.getHostAddress(), getInetAddress(wifiManager).getHostAddress() };

                    // TODO: handle host address change?

                    continue;
                }

                Log.d(TAG, "client.getHostAddress() [" + client.getInetAddress().getHostAddress() + "]");

                DataInputStream  input  = new DataInputStream (new BufferedInputStream (client.getInputStream ()));
                DataOutputStream output = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));

                byte[] buffer = new byte[2048];

                int count = 0;

                // TODO: check connection while communicating?

                while ((count = input.read(buffer)) > 0) {
                    Log.h(TAG, buffer, count);

                    output.write(buffer, 0, count); /* 2021-07-29: only echoing for now */
                    output.flush();
                }

                // TODO: PinpadManager.send(...)
                // TODO: PinpadManager.receive(...)
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));

            if (sServerSocket != null) {
                try {
                    sServerSocket.close();
                } catch (Exception warning) {
                    Log.w(TAG, Log.getStackTraceString(warning));
                }

                sServerSocket = null;
            }

            if (sWifiLock != null) {
                sWifiLock.release();

                sWifiLock = null;
            }

            callback.onFailure(exception);
        }
    }

    public void close() {
        Log.d(TAG, "close");

        try {
            if (sServerSocket != null) { // TODO: make it thread-safe?
                sServerSocket.close();
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
    }
}
