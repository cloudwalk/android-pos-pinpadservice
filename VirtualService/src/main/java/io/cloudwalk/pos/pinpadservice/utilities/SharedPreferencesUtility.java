package io.cloudwalk.pos.pinpadservice.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import io.cloudwalk.loglibrary.Application;
import io.cloudwalk.loglibrary.Log;

public class SharedPreferencesUtility {
    private static final String
            TAG = SharedPreferencesUtility.class.getSimpleName();

    private static final SharedPreferences
            sSharedPreferences = Application.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

    private static SharedPreferences.Editor
            sEditor = sSharedPreferences.edit();

    private SharedPreferencesUtility() {
        Log.d(TAG, "SharedPreferencesUtility");

        /* Nothing to do */
    }

    public static String readIPv4() {
        Log.d(TAG, "readIPv4");

        return sSharedPreferences.getString("address", "127.0.0.1:8080");
    }

    public static boolean writeIPv4(String address) {
        Log.d(TAG, "writeIPv4");

        sEditor.putString("address", address);

        return sEditor.commit();
    }
}
