package com.example.poc2104301453.demo.presentation;

import android.os.Bundle;

import com.example.poc2104301453.demo.R;
import com.example.poc2104301453.pinpadlibrary.managers.PinpadManager;
import com.example.poc2104301453.pinpadlibrary.utilities.DataUtility;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import com.example.poc2104301453.demo.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import static com.example.poc2104301453.pinpadlibrary.ABECS.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_LOGCAT = MainActivity.class.getSimpleName();

    private static final Semaphore sSemaphore = new Semaphore(1, true);

    private void updateContentScrolling(String msg) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphore.acquireUninterruptibly();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_content_scrolling)).setText(msg);

                        sSemaphore.release();
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        binding.fab.setEnabled(false);

        PinpadManager.register();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG_LOGCAT, "onPause");

        finish();

        PinpadManager.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateContentScrolling(getString(R.string.warning_reading));

        new Thread() {
            @Override
            public void run() {
                try {
                    String contentScrolling = "Triggered at "
                            + Calendar.getInstance().getTime()
                            + "\r\n\r\n";

                    List<Bundle> requestList = new ArrayList<>(0);

                    Bundle input;

                    input = new Bundle();

                    input.putString(CMD_ID, OPN);

                    requestList.add(input);

                    input = new Bundle();

                    input.putString(CMD_ID, GIN);

                    input.putInt   (GIN_ACQIDX, 0);

                    requestList.add(input);

                    input = new Bundle();

                    input.putString(CMD_ID, CLO);

                    requestList.add(input);

                    input = new Bundle();

                    input.putString(CMD_ID, OPN);

                    requestList.add(input);

                    input = new Bundle();

                    input.putString(CMD_ID, CKE);

                    input.putInt   (CKE_KEY,  1);
                    input.putInt   (CKE_MAG,  1);
                    input.putInt   (CKE_ICC,  1);
                    input.putInt   (CKE_CTLS, 1);

                    requestList.add(input);

                    input = new Bundle();

                    input.putString(CMD_ID, GCR);

                    String date = (new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()))
                            .format(new Date());

                    input.putInt   (GCR_ACQIDXREQ, 0);
                    input.putInt   (GCR_APPTYPREQ, 99);
                    input.putLong  (GCR_AMOUNT, 0);
                    input.putString(GCR_DATE, date.substring(0, 6));
                    input.putString(GCR_TIME, date.substring(6));
                    input.putString(GCR_TABVER, "2106202112");
                    input.putInt   (GCR_QTDAPP, 0);

                    requestList.add(input);

                    for (Bundle request : requestList) {
                        contentScrolling += "\n";

                        contentScrolling += DataUtility.toJSON(PinpadManager.request(request), true).toString(4);

                        updateContentScrolling(contentScrolling);
                    }
                } catch (Exception exception) {
                    updateContentScrolling(Log.getStackTraceString(exception));
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
