package io.cloudwalk.pos.pinpadservice.presentation;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    private static final Semaphore[]
            sSemaphore = {
                    new Semaphore(1, true), /* activity sync. control */
                    new Semaphore(1, true)  /* public methods */
            };

    private static AppCompatActivity
            sActivity = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "startActivity");

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

                                sSemaphore[0].release();
                            }
                        } .start();
                    }
                });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        overridePendingTransition(0, 0);
    }

    public static void setVisibility(boolean status) {
        Log.d(TAG, "setVisibility::status [" + status + "]");

        sSemaphore[1].acquireUninterruptibly();

        if (sActivity != null) {
            if (!status) {
                sActivity.finishAndRemoveTask();

                sSemaphore[0].release();
            }
        }

        sSemaphore[1].release();
    }

    public static void startActivity(@NotNull String application) {
        Log.d(TAG, "startActivity::application [" + application + "]");

        sSemaphore[1].acquireUninterruptibly();
        sSemaphore[0].acquireUninterruptibly();

        Context context = Application.getPackageContext();

        Intent intent = new Intent(context, PinCaptureActivity.class);

        context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

        sSemaphore[0].acquireUninterruptibly();
        sSemaphore[1].release();
    }
}
