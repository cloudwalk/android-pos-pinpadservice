package com.example.poc2104301453.service.commands;

import android.os.Bundle;

import com.example.poc2104301453.service.exceptions.PendingDevelopmentException;

public class OPN {
    private static final String TAG_LOGCAT = OPN.class.getSimpleName();

    public static Bundle opn(Bundle input)
            throws Exception {
        throw new PendingDevelopmentException(TAG_LOGCAT + "pending development");
    }
}
