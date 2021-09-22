package io.cloudwalk.pos.pinpadservice.presentation;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

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

import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.pos.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.pos.utilitieslibrary.Application;

public class PinCaptureActivity extends AppCompatActivity {
    private static final String
            TAG = PinCaptureActivity.class.getSimpleName();

    private static final Semaphore
            sLifeCycleSemaphore = new Semaphore(1, true);

    private static AppCompatActivity
            sActivity = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");

        return super.onTouchEvent(event);
    }

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

        int availablePermits = sLifeCycleSemaphore.availablePermits();

        Log.d(TAG, "onPause::availablePermits [" + availablePermits + "]");

        if (availablePermits <= 0) {
            ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                    .moveTaskToFront(getTaskId(), 0);
        }

        super.onPause();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        overridePendingTransition(0, 0);
    }

    public static void finishActivity() {
        Log.d(TAG, "finishActivity");

        if (sActivity != null) {
            sActivity.finishAndRemoveTask();

            sActivity = null;

            sLifeCycleSemaphore.release();
        }
    }

    public static void setVisibility(boolean status) {
        Log.d(TAG, "setVisibility::status [" + status + "]");

        if (!status) {
            finishActivity();
        }
    }

    public static void startActivity(@NotNull String application) {
        Log.d(TAG, "startActivity::application [" + application + "]");

        sLifeCycleSemaphore.acquireUninterruptibly();

        Context context = Application.getPackageContext();

        Intent intent = new Intent(context, PinCaptureActivity.class);

        context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

        sLifeCycleSemaphore.acquireUninterruptibly();
    }
}
