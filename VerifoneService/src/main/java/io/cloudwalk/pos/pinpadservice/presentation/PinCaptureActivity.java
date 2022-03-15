package io.cloudwalk.pos.pinpadservice.presentation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.utilitieslibrary.Application;

public class PinCaptureActivity extends AppCompatActivity {
    private static final String
            TAG = PinCaptureActivity.class.getSimpleName();

    private static final Semaphore
            sLifeCycleSemaphore = new Semaphore(1, true);

    private static AppCompatActivity
            sActivity = null;

    private static String
            sApplicationId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity);

        sActivity = this;

        RelativeLayout relativeLayout = findViewById(R.id.relative_layout);

        relativeLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        new Thread() {
                            @Override
                            public void run() {
                                super.run();

                                sLifeCycleSemaphore.release();
                            }
                        } .start();
                    }
                });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        try {
            if (!sLifeCycleSemaphore.tryAcquire(0, TimeUnit.MILLISECONDS)) {
                if (sActivity != null) {
                    ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                            .moveTaskToFront(sActivity.getTaskId(), 0);
                }
            } else {
                sLifeCycleSemaphore.release();
            }

            super.onPause();

            overridePendingTransition(0, 0);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        overridePendingTransition(0, 0);
    }

    public static void resumeActivity() {
        Log.d(TAG, "resumeActivity");

        if (sActivity != null) {
            ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                    .moveTaskToFront(sActivity.getTaskId(), 0);
        }
    }

    public static void finishActivity() {
        Log.d(TAG, "finishActivity");

        if (sActivity != null) {
            sActivity.finishAndRemoveTask();

            sActivity = null;
            sApplicationId = null;

            sLifeCycleSemaphore.release();
        }
    }

    public static void moveActivityToFront(boolean status) {
        Log.d(TAG, "moveActivityToFront::status [" + status + "]");
    }

    public static void startActivity(@NotNull String applicationId) {
        Log.d(TAG, "startActivity::applicationId [" + applicationId + "]");

        sLifeCycleSemaphore.acquireUninterruptibly();

        sApplicationId = applicationId;

        Context context = Application.getPackageContext();

        context.startActivity(new Intent(context, PinCaptureActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        sLifeCycleSemaphore.acquireUninterruptibly();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");

        return super.onTouchEvent(event);
    }
}
