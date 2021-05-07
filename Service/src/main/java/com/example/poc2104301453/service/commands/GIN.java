package com.example.poc2104301453.service.commands;

import android.os.Bundle;

import com.example.poc2104301453.library.exceptions.PendingDevelopmentException;

public class GIN {
    private static final String TAG_LOGCAT = GIN.class.getSimpleName();

    public static Bundle gin(Bundle input)
            throws Exception {
        throw new PendingDevelopmentException(TAG_LOGCAT + " pending development");
    }
}
