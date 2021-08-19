package io.cloudwalk.pos.demo.presentation;

import androidx.annotation.ColorInt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.databinding.ActivitySplashBinding;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.ServiceUtility;

public class SplashActivity extends AppCompatActivity {
    private static final String
            TAG = SplashActivity.class.getSimpleName();

    private Menu
            mMenu = null;

    private Semaphore
            mSemaphore = new Semaphore(-1, true);

    private SpannableString getBullet(@ColorInt int color) {
        SpannableString output = new SpannableString("  ");

        output.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return output;
    }

    private void updateContentScrolling(int status, String message) {
        SpannableStringBuilder[] content = { new SpannableStringBuilder() };

        switch (status) {
            case 0: /* SUCCESS */
                content[0].append(getBullet(Color.GREEN));
                break;

            case 1: /* FAILURE */
                content[0].append(getBullet(Color.RED));
                break;

            case 2: /* PROCESSING */
                content[0].append(getBullet(Color.BLUE));
                break;

            default:
                content[0].append(getBullet(Color.GRAY));
                break;
        }

        content[0].append(message);

        Semaphore[] semaphore = { new Semaphore(0, true) };

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.tv_splash_content_scrolling)).setText(content[0]);

                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();
    }

    private void loadApplication() {
        Log.d(TAG, "loadApplication");

        loadDependencies();

        /* The 'acquire' call serves the purpose of blocking the application till all required
         * dependencies are ready. For that, the semaphore instantiation must take into account the
         * right amount of permits: (number of dependencies * -1)
         * See SplashActivity#loadDependencies() for further insight on the number of permits */
        mSemaphore.acquireUninterruptibly();

        if (!wasPaused()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

            overridePendingTransition(0, 0);
        }

        finish();
    }

    private void loadDependencies() {
        Log.d(TAG, "loadDependencies");

        long timestamp = SystemClock.elapsedRealtime();

        PinpadManager.register(new ServiceUtility.Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess");

                mSemaphore.release();
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "onFailure");

                /* A reconnection strategy is recommended in here. */

                updateContentScrolling(1, "PinpadService was either disconnected, not found or its bind failed due to missing permissions");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Inflate the menu; this adds items to the action bar if it is present.
                        getMenuInflater().inflate(R.menu.menu_main, mMenu);
                    }
                });
            }
        });

        timestamp = SystemClock.elapsedRealtime() - timestamp;

        /* Ensures the SplashActivity will be shown for a minimum amount of time */
        if (timestamp < 750) {
            SystemClock.sleep(750 - timestamp);
        }

        mSemaphore.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        updateContentScrolling(2, getString(R.string.warning_application_starting));

        /* 'onCreate' shouldn't be blocked by potentially demanding routines, hence the thread */
        new Thread() {
            @Override
            public void run() {
                super.run();

                loadApplication();
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        // Not inflating the menu in here 'cause the SplashActivity should have
        // a limited lifecycle...
        // (but saving it for later, if anything goes wrong)
        mMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement

        if (id == R.id.action_about) {
            (new AboutAlertDialog(this)).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
