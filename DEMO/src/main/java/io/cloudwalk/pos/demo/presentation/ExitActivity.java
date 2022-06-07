package io.cloudwalk.pos.demo.presentation;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.utilitieslibrary.Application;

public class ExitActivity extends Activity {
    private static final String
            TAG = ExitActivity.class.getSimpleName();

    private static Context _getContext() {
        Log.d(TAG, "_getContext");

        return Application.getContext();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        finishAndRemoveTask();
    }

    public static void run() {
        Log.d(TAG, "run");

        Intent intent = new Intent(_getContext(), ExitActivity.class);

        intent.addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | FLAG_ACTIVITY_CLEAR_TASK
                      | FLAG_ACTIVITY_NEW_TASK             | FLAG_ACTIVITY_NO_ANIMATION);

        _getContext().startActivity(intent);
    }
}
