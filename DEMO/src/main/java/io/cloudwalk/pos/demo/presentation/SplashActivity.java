package io.cloudwalk.pos.demo.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.demo.databinding.ActivitySplashBinding;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final Semaphore sOnBackPressedSemaphore = new Semaphore(1, true);

    private static final Semaphore sStartSemaphore = new Semaphore(-1, true);

    private static boolean sOnBackPressed = false;

    private boolean getOnBackPressed() {
        boolean onBackPressed;

        sOnBackPressedSemaphore.acquireUninterruptibly();

        onBackPressed = sOnBackPressed;

        sOnBackPressedSemaphore.release();

        return onBackPressed;
    }

    private void setOnBackPressed(boolean onBackPressed) {
        sOnBackPressedSemaphore.acquireUninterruptibly();

        sOnBackPressed = onBackPressed;

        sOnBackPressedSemaphore.release();
    }

    private void startApplication() {
        Log.d(TAG, "startApplication");

        startDependencies();

        /* The 'acquire' call serves the purpose of blocking the application till all required
         * dependencies are ready. For that, the semaphore instantiation must take into account the
         * right amount of permits: (number of dependencies * -1)
         * See SplashActivity#startDependencies() for further insight on the number of permits */
        sStartSemaphore.acquireUninterruptibly();

        if (getOnBackPressed()) {
            /* Ensures not to go any further if the user has decided to abort */
            return;
        }

        /* Effectively start the application */
        startActivity(new Intent(getApplicationContext(), MainActivity.class));

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    private void startDependencies() {
        Log.d(TAG, "startDependencies");

        long timestamp = SystemClock.elapsedRealtime();

        PinpadManager.register(new ServiceUtility.Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess");

                sStartSemaphore.release();
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "onFailure");
            }
        });

        timestamp = SystemClock.elapsedRealtime() - timestamp;

        /* Ensures the SplashActivity will be shown for a minimum amount of time */
        if (timestamp < 1500) {
            SystemClock.sleep(1500 - timestamp);
        }

        sStartSemaphore.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        /* 'onCreate' shouldn't be blocked by potentially demanding routines, hence the thread */
        new Thread() {
            @Override
            public void run() {
                super.run();

                startApplication();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        setOnBackPressed(false);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        super.onBackPressed();

        setOnBackPressed(true);

        finish();
    }
}
