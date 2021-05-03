package com.example.poc2104301453;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class COTS extends ICOTS.Stub {
    private static final String TAG_LOGCAT = COTS.class.getSimpleName();

    private static final COTS sMyself = new COTS();

    private static final Lock sLock = new ReentrantLock(true);

    private COTS() {
        Log.w(TAG_LOGCAT, "COTS");
    }

    public static COTS getInstance() {
        Log.w(TAG_LOGCAT, "getInstance");

        return sMyself;
    }

    @Override
    public void parse(Bundle input, IServiceCallback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                Bundle output = new Bundle();
                try {
                    sLock.lock();

                    JSONObject jsonObject = new JSONObject();
                    List<String> keySet = new ArrayList<>(0);

                    keySet.add("COTS");

                    for (String key : keySet) {
                        jsonObject.put(key, input.get(key));

                        if (!jsonObject.has(key)) {
                            throw new NoSuchElementException("Input missing mandatory key(s)");
                        }
                    }

                    output.putString("echo", jsonObject.toString());
                    output.putInt("status", 0);
                } catch (Exception exception) {
                    output.putSerializable("exception", (Serializable) exception);
                    output.putInt("status", 40);
                } finally {
                    try {
                        if (callback != null) {
                            if ((int) output.get("status") != 0) {
                                callback.onFailure(output);
                            } else {
                                callback.onSuccess(output);
                            }
                        }
                    } catch (RemoteException exception) {
                        Log.w(TAG_LOGCAT, exception);
                    }

                    sLock.unlock();
                }
            }
        }.start();
    }
}
