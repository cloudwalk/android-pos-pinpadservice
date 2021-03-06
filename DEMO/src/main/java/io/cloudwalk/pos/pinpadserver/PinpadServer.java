package io.cloudwalk.pos.pinpadserver;

import static android.content.Context.WIFI_SERVICE;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.utilitieslibrary.Application;

public class PinpadServer {
    private static final String
            TAG = PinpadServer.class.getSimpleName();

    private final Semaphore
            mExchangeSemaphore = new Semaphore(1, true);

    private PinpadManager.Callback
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
        int  onPinpadCallback(String string);

        // TODO: int  onServerCallback(String string);

        void onServerFailure(Exception exception);

        void onServerRecv(byte[] trace, int length);

        void onServerSend(byte[] trace, int length);

        void onServerSuccess(String address);
    }

    private void _close(Exception exception) {
        // Log.d(TAG, "_close");

        try {
            if (mClientSocket != null) {
                mClientSocket.close();
            }

            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (Exception ignored) { }

        if (mWifiLock != null) {
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
        }

        if (exception != null) {
            mServerCallback.onServerFailure(exception);
        }
    }

    private InetAddress _getWiFiInetAddress()
            throws Exception {
        // Log.d(TAG, "_getWiFiInetAddress");

        int address = mWifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            address = Integer.reverseBytes(address);
        }

        return InetAddress.getByAddress(BigInteger.valueOf(address).toByteArray());
    }

    private void _onServerRecv(byte[] array, int length) {
        Log.d(TAG, "_onServerRecv");

        mSemaphore.acquireUninterruptibly();

        mServerCallback.onServerRecv(array, length);

        mSemaphore.release();
    }

    private void _onServerSend(byte[] array, int length) {
        Log.d(TAG, "_onServerSend");

        mSemaphore.acquireUninterruptibly();

        mServerCallback.onServerSend(array, length);

        mSemaphore.release();
    }

    public PinpadServer(@NotNull PinpadServer.Callback callback) {
        Log.d(TAG, "PinpadServer");

        mServerCallback  = callback;

        mServiceCallback = new PinpadManager.Callback() {
            @Override
            public int onServiceCallback(String string) {
                return mServerCallback.onPinpadCallback(string);
            }
        };
    }

    public void close() {
        Log.d(TAG, "close");

        try {
            mSemaphore.acquireUninterruptibly();

            _close(null);
        } finally {
            mSemaphore.release();
        }
    }

    @SuppressLint("WifiManagerLeak")
    public void raise()
            throws Exception {
        Log.d(TAG, "raise");

        mWifiManager = (WifiManager) Application.getContext().getSystemService(WIFI_SERVICE);

        raise(_getWiFiInetAddress(), 8080);

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

                            byte[] stream = new byte[2048];
                            int    count;

                            while ((count = mClientSocket.getInputStream().read(stream)) >= 0) {
                                if (count == 0) { continue; }

                                mExchangeSemaphore.acquireUninterruptibly();

                                try {
                                    _onServerRecv(stream, count);

                                    count = PinpadManager.send(stream, count, mServiceCallback);

                                    if (count  < 0) { continue; }

                                    count = PinpadManager.recv(stream, 2000);

                                    if (count == 0) { continue; }

                                    mClientSocket.getOutputStream().write(stream, 0, count);
                                    mClientSocket.getOutputStream().flush();

                                    _onServerSend(stream, count);
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
                                                        try { count = PinpadManager.recv(stream, 0); } finally { mExchangeSemaphore.release(); }
                                                    }
                                                } while (count == 0);

                                                if (count < 0) { return; }

                                                _onServerSend(stream, count);

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

                        _close(exception);
                    }
                }
            }.start();
        } finally {
            mSemaphore.release();
        }
    }
}
