package io.cloudwalk.pos.loglibrary;

import static java.util.Locale.US;

public class Log {
    private static final String
            TAG =  Log.class.getSimpleName();

    public static final int
            ASSERT = Log.ASSERT;

    public static final int
            DEBUG = Log.DEBUG;

    public static final int
            ERROR = Log.ERROR;

    public static final int
            INFO = Log.INFO;

    public static final int
            VERBOSE = Log.VERBOSE;

    public static final int
            WARN = Log.WARN;

    public static int d(String tag, String msg, Throwable tr) {
        return android.util.Log.d(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return android.util.Log.d(tag, msg);
    }

    public static int e(String tag, String msg) {
        return android.util.Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return android.util.Log.e(tag, msg, tr);
    }

    public static String getByteTraceString(byte[] input, int length) {
        StringBuilder[] msg = { new StringBuilder(), new StringBuilder(), new StringBuilder() };

        for (int i = 0; i < length; i++) {
            byte b = input[i];

            msg[0].append(String.format(US, "%02X ", b));

            msg[1].append((b > 0x20 && b < 0x7F) ? (char) b : '.');

            if ((msg[1].length() % 16) != 0 && (i + 1) >= length) {
                int ceil;

                ceil = 48 - (msg[0].length() % 48);

                for (int j = 0; j < ceil; j += 3) {
                    msg[0].append(".. ");
                }

                ceil = 16 - (msg[1].length() % 16);

                for (int j = 0; j < ceil; j++) {
                    msg[1].append(".");
                }
            }

            if ((i > 0 && msg[1].length() % 16 == 0) || (i + 1) >= length) {
                msg[0].append(msg[1]);

                msg[2].append("\r\n").append(msg[0]);

                msg[0].delete(0, msg[0].length());

                msg[1].delete(0, msg[1].length());
            }
        }

        return msg[2].toString();
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static void h(String tag, byte[] input, int length) {
        String[] msg = getByteTraceString(input, length).split("\r\n");

        for (String slice : msg) {
            android.util.Log.d(tag, slice);
        }
    }

    public static int i(String tag, String msg, Throwable tr) {
        return android.util.Log.i(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return android.util.Log.i(tag, msg);
    }

    public static boolean isLoggable(String tag, int level) {
        return android.util.Log.isLoggable(tag, level);
    }

    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }

    public static int v(String tag, String msg) {
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return android.util.Log.v(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return android.util.Log.w(tag, tr);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return android.util.Log.w(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return android.util.Log.w(tag, msg);
    }

    public static int wtf(String tag, String msg) {
        return android.util.Log.wtf(tag, msg);
    }

    public static int wtf(String tag, Throwable tr) {
        return android.util.Log.wtf(tag, tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return android.util.Log.wtf(tag, msg, tr);
    }
}
