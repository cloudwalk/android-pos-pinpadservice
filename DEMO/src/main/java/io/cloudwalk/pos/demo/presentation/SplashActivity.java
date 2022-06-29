package io.cloudwalk.pos.demo.presentation;

import static io.cloudwalk.pos.Application.sPinpadServer;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import org.json.JSONObject;

import java.util.concurrent.Semaphore;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.databinding.ActivitySplashBinding;
import io.cloudwalk.pos.pinpadlibrary.PinpadService;
import io.cloudwalk.pos.pinpadserver.PinpadServer;
import io.cloudwalk.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.utilitieslibrary.utilities.ByteUtility;
import io.cloudwalk.utilitieslibrary.utilities.ServiceUtility;

public class SplashActivity extends AppCompatActivity {
    private static final String
            TAG = SplashActivity.class.getSimpleName();

    private AlertDialog
            mAboutAlertDialog = null;

    private Menu
            mMenu = null;

    private Semaphore[]
            mSemaphore = {
                    new Semaphore(-1, true),
                    new Semaphore( 0, true)
            };

    private Menu _getMenu() {
        Log.d(TAG, "_getMenu");

        Menu menu;

        mSemaphore[1].acquireUninterruptibly();

        menu = mMenu;

        mSemaphore[1].release();

        return menu;
    }

    private SpannableString _getBullet(@ColorInt int color) {
        Log.d(TAG, "_getBullet::color [" + color + "]");

        SpannableString response = new SpannableString("  ");

        response.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return response;
    }

    private void _loadApplication() {
        Log.d(TAG, "_loadApplication");

        _loadDependencies();

        PinpadServer server = sPinpadServer.get();

        if (server != null) {
            server.close();
        }

        sPinpadServer.set(null);

        /* The 'acquire' call serves the purpose of blocking the application till all required
         * dependencies are ready. */
        mSemaphore[0].acquireUninterruptibly();

        mSemaphore[0] = new Semaphore(-1, true);

        if (!wasPaused()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

            overridePendingTransition(0, 0);
        }
    }

    private void _loadDependencies() {
        Log.d(TAG, "_loadDependencies");

        long timestamp = SystemClock.elapsedRealtime();

        PinpadService.unregister();

        String string = null;

        try {
            JSONObject json = new JSONObject();

            switch (Build.BRAND) {
                case "SUNMI":
                    byte[] keymap = new byte[] { 0x31, 0x30, 0x30, 0x33, 0x31, 0x30, 0x0D, 0x0A,
                                                 0x31, 0x31, 0x31, 0x33, 0x31, 0x31 };

                    json.put("keymap.dat", ByteUtility.getHexString(keymap, keymap.length));
                    break;

                case "Verifone":
                    byte[] duklink = new byte[] { 0x03, 0x0A, 0x03, 0x05, 0x0B, 0x04 };

                    json.put("DUKLINK.dat", ByteUtility.getHexString(duklink, duklink.length));
                    break;

                default:
                    json = null;
                    break;
            }

            string = (json != null) ? json.toString() : null;
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        PinpadService.register(string, new ServiceUtility.Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess");

                mSemaphore[0].release();
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "onFailure");

                /* A reconnection strategy is recommended in here. */

                _updateContentScrolling(1, "PinpadService was either disconnected, not found or its bind failed due to missing permissions");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Inflate the menu; this adds items to the action bar if it is present.
                        getMenuInflater().inflate(R.menu.menu_main, _getMenu());
                    }
                });
            }
        });

        timestamp = SystemClock.elapsedRealtime() - timestamp;

        /* Ensures the SplashActivity will be shown for a minimum amount of time */
        if (timestamp < 750) {
            SystemClock.sleep(750 - timestamp);
        }

        mSemaphore[0].release();
    }

    private void _updateContentScrolling(int status, String message) {
        Log.d(TAG, "_updateContentScrolling");

        SpannableStringBuilder[] content = { new SpannableStringBuilder() };

        switch (status) {
            case 0: /* SUCCESS */
                content[0].append(_getBullet(Color.GREEN));
                break;

            case 1: /* FAILURE */
                content[0].append(_getBullet(Color.RED));
                break;

            case 2: /* PROCESSING */
                content[0].append(_getBullet(Color.BLUE));
                break;

            default:
                content[0].append(_getBullet(Color.GRAY));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());

        setContentView     (binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        mAboutAlertDialog = new AboutAlertDialog(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        _updateContentScrolling(2, getString(R.string.warning_application_starting));

        /* 'onCreate' shouldn't be blocked by potentially demanding routines, hence the thread */
        new Thread() {
            @Override
            public void run() {
                super.run();

                _loadApplication();
            }
        }.start();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        mAboutAlertDialog.dismiss();

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        // Not inflating the menu in here 'cause the SplashActivity should have
        // a limited lifecycle...
        // (but saving it for later, if anything goes wrong)
        mMenu = menu;

        mSemaphore[1].release();

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
            mAboutAlertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
