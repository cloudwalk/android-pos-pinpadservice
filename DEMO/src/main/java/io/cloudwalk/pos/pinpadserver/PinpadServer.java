package io.cloudwalk.pos.pinpadserver;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.utilitieslibrary.Application;

import static android.content.Context.WIFI_SERVICE;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.jetbrains.annotations.NotNull;

public class PinpadServer {
    private static final String
            TAG = PinpadServer.class.getSimpleName();

    private final Semaphore
            mExchangeSemaphore = new Semaphore(1, true);

    private IServiceCallback
            mServiceCallback = null;

    private PinpadServer.Callback
            mServerCallback = null;

    private Semaphore
            mSemaphore = new Semaphore(1, true);

    private ServerSocket
            mServerSocket = null;

    private Socket
            mClientSocket = null;

    private WifiManager
            mWifiManager = null;

    private WifiManager.WifiLock
            mWifiLock = null;

    public static interface Callback {
        void onClientConnection(String address);

        int  onPinpadCallback (Bundle bundle);

        // TODO: void onPinpadReceive(byte[] trace, int length);

        // TODO: void onPinpadSend(byte[] trace, int length);

        void onServerFailure(Exception exception);

        void onServerReceive(byte[] trace, int length);

        void onServerSend(byte[] trace, int length);

        void onServerSuccess(String address);
    }

    private void close(Exception exception) {
        // Log.d(TAG, "close");

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

        if (exception != null) {
            mServerCallback.onServerFailure(exception);
        }
    }

    private InetAddress getWiFiInetAddress()
            throws Exception {
        // Log.d(TAG, "getWiFiInetAddress");

        int address = mWifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            address = Integer.reverseBytes(address);
        }

        return InetAddress.getByAddress(BigInteger.valueOf(address).toByteArray());
    }

    private void onServerReceive(byte[] input, int length) {
        Log.d(TAG, "onServerReceive");

        mSemaphore.acquireUninterruptibly();

        mServerCallback.onServerReceive(input, length);

        mSemaphore.release();
    }

    private void onServerSend(byte[] input, int length) {
        Log.d(TAG, "onServerSend");

        mSemaphore.acquireUninterruptibly();

        mServerCallback.onServerSend(input, length);

        mSemaphore.release();
    }

    public PinpadServer(@NotNull PinpadServer.Callback callback) {
        Log.d(TAG, "PinpadServer");

        mServerCallback  = callback;

        mServiceCallback = new IServiceCallback.Stub() {
            @Override
            public int onServiceCallback(Bundle bundle) throws RemoteException {
                return mServerCallback.onPinpadCallback(bundle);
            }
        };
    }

    public void close() {
        Log.d(TAG, "close");

        try {
            mSemaphore.acquireUninterruptibly();

            close(null);
        } finally {
            mSemaphore.release();
        }
    }

    @SuppressLint("WifiManagerLeak")
    public void raise()
            throws Exception {
        Log.d(TAG, "raise");

        mWifiManager = (WifiManager) Application.getContext().getSystemService(WIFI_SERVICE);

        raise(getWiFiInetAddress(), 8080);

        try {
            mSemaphore.acquireUninterruptibly();

            if (mWifiLock == null) {
                mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);
            }

            mWifiLock.acquire();
        } finally {
            mSemaphore.release();
        }
    }

    public void raise(InetAddress host, int port)
            throws Exception {
        Log.d(TAG, "raise");

        try {
            mSemaphore.acquireUninterruptibly();

            if (mServerSocket != null) {
                mServerSocket.close();
            }

            mServerSocket = new ServerSocket(port, 1, host);

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    try {
                        String address = mServerSocket.getLocalSocketAddress().toString();

                        mServerCallback.onServerSuccess(address);

                        while (true) {
                            mClientSocket = mServerSocket.accept();

                            mServerCallback.onClientConnection(mClientSocket.getInetAddress().getHostAddress());

                            byte[] stream = new byte[2048];
                            int    count;

                            while ((count = mClientSocket.getInputStream().read(stream)) >= 0) {
                                if (count == 0) { continue; }

                                mExchangeSemaphore.acquireUninterruptibly();

                                try {
                                    onServerReceive(stream, count);

                                    count = PinpadManager.send(stream, count, mServiceCallback);

                                    if (count  < 0) { continue; }

                                    count = PinpadManager.receive(stream, 2000);

                                    if (count == 0) { continue; }

                                    mClientSocket.getOutputStream().write(stream, 0, count);
                                    mClientSocket.getOutputStream().flush();

                                    onServerSend(stream, count);
                                } finally {
                                    mExchangeSemaphore.release();
                                }

                                if (stream[0] == 0x06) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            super.run();

                                            try {
                                                Socket socket = mClientSocket;

                                                byte[] stream = new byte[2048];
                                                int    count;

                                                do {
                                                    // TODO: only interrupt over a <<CAN>> control byte

                                                    if (!mExchangeSemaphore.tryAcquire(0, SECONDS)) {
                                                        count = -1;
                                                    } else {
                                                        try { count = PinpadManager.receive(stream, 0); } finally { mExchangeSemaphore.release(); }
                                                    }
                                                } while (count == 0);

                                                if (count < 0) { return; }

                                                onServerSend(stream, count);

                                                socket.getOutputStream().write(stream, 0, count);
                                                socket.getOutputStream().flush();
                                            } catch (Exception exception) {
                                                Log.e(TAG, Log.getStackTraceString(exception));
                                            }
                                        }
                                    }.start();
                                }
                            }
                        }
                    } catch (Exception exception) {
                        mServerCallback.onServerFailure(exception);

                        close(exception);
                    }
                }
            }.start();
        } finally {
            mSemaphore.release();
        }
    }
}
