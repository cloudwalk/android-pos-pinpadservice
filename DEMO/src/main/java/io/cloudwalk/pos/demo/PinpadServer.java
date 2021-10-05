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

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.utilitieslibrary.Application;

import static android.content.Context.WIFI_SERVICE;

public class PinpadServer { // TODO: improve and enable at MainActivity!?
    private static final String
            TAG = PinpadServer.class.getSimpleName();

    private static final byte
            ACK = 0x06;

    private static final byte
            EOT = 0x04;

    private IServiceCallback
            mServiceCallback = null;

    private PinpadServer.Callback
            mServerCallback = null;

    private Semaphore[]
            mSemaphore = {
                    new Semaphore(1, true),
                    new Semaphore(1, true)
            };

    private ServerSocket
            mServerSocket = null;

    private Socket
            mClientSocket = null;

    private WifiManager
            mWifiManager = null;

    private WifiManager.WifiLock
            mWifiLock = null;

    public static interface Callback {
        void onFailure(Exception exception);

        void onRecv(byte[] trace, int length);

        void onSend(byte[] trace, int length);

        void onSuccess(String localSocket);
    }

    private InetAddress getInetAddress()
            throws Exception {
        // Log.d(TAG, "getInetAddress");

        int ip = mWifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        return InetAddress.getByAddress(BigInteger.valueOf(ip).toByteArray());
    }

    private void accept(InetAddress currentAddress) {
        Log.d(TAG, "accept");

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    String localSocketAddress = mServerSocket.getLocalSocketAddress().toString();

                    mSemaphore[0].release();

                    mServerCallback.onSuccess(localSocketAddress);

                    while (true) {
                        try {
                            mSemaphore[0].acquireUninterruptibly();

                            mClientSocket = mServerSocket.accept();
                        } catch (SocketTimeoutException warning) {
                            String[] hostAddress = { currentAddress.getHostAddress(), getInetAddress().getHostAddress() };

                            continue; // TODO: handle host address change!?
                        } finally {
                            mSemaphore[0].release();
                        }

                        Log.d(TAG, "client.getHostAddress() [" + mClientSocket.getInetAddress().getHostAddress() + "]");

                        DataInputStream  input  = new DataInputStream (new BufferedInputStream (mClientSocket.getInputStream ()));
                        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(mClientSocket.getOutputStream()));

                        byte[] request = new byte[2048];

                        int count = 0;

                        while ((count = input.read(request)) > 0) {
                            onRecv(request, count);

                            count = PinpadManager.send(request, count, mServiceCallback);

                            if (count < 0) {
                                Log.e(TAG, "accept::count [" + count + "]");
                                continue;
                            }

                            byte[] response = new byte[2048];

                            count = PinpadManager.receive(response, 1900);

                            if (count != 1) {
                                Log.e(TAG, "accept::count [" + count + "]");
                                continue;
                            }

                            onSend(response, count);

                            output.write(response, 0, count);
                            output.flush();

                            if (response[0] != ACK && response[0] != EOT) {
                                continue;
                            }

                            do {
                                count = PinpadManager.receive(response, 9900);
                            } while (count == 0);

                            if (count < 0) {
                                Log.e(TAG, "accept::count [" + count + "]");
                                continue;
                            }

                            onSend(response, count);

                            output.write(response, 0, count);
                            output.flush();
                        }
                    }
                } catch (Exception exception) {
                    close(exception);
                }
            }
        }.start();
    }

    private void close(Exception exception) {
        Log.d(TAG, "close");

        mSemaphore[0].acquireUninterruptibly();

        try {
            if (mClientSocket != null) {
                mClientSocket.close();
            }

            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (Exception warning) {
            Log.w(TAG, Log.getStackTraceString(warning));
        }

        if (mWifiLock != null) {
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
        }

        mSemaphore[0].release();

        if (exception != null) {
            mServerCallback.onFailure(exception);
        }
    }

    private void onRecv(byte[] input, int length) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                Log.d(TAG, "onRecv");

                mSemaphore[1].acquireUninterruptibly();

                mServerCallback.onRecv(input, length);

                mSemaphore[1].release();
            }
        }.start();
    }

    private void onSend(byte[] input, int length) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                Log.d(TAG, "onSend");

                mSemaphore[1].acquireUninterruptibly();

                mServerCallback.onSend(input, length);

                mSemaphore[1].release();
            }
        }.start();
    }

    public PinpadServer(@NotNull PinpadServer.Callback serverCallback, IServiceCallback pinpadCallback) {
        Log.d(TAG, "PinpadServer");

        mSemaphore[0].acquireUninterruptibly();

        mServerCallback = serverCallback;
        mServiceCallback = pinpadCallback;

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    mWifiManager = (WifiManager) Application.getPackageContext()
                            .getApplicationContext().getSystemService(WIFI_SERVICE);

                    InetAddress inetAddress = getInetAddress();

                    mServerSocket = new ServerSocket(8080, 1, inetAddress);

                    mServerSocket.setSoTimeout(200);

                    if (mWifiLock == null) {
                        mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);
                    }

                    mWifiLock.acquire();

                    accept(inetAddress);
                } catch (Exception exception) {
                    mSemaphore[0].release();

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
