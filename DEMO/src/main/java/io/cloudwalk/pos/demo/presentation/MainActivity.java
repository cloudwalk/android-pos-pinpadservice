package io.cloudwalk.pos.demo.presentation;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.demo.DEMO;
import io.cloudwalk.pos.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.adapters.MainAdapter;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class MainActivity extends AppCompatActivity {
    private static final String
            TAG = MainActivity.class.getSimpleName();

    private static final int
            MAIN_ADAPTER_CONTENT_LIMIT = 2000;

    private static final Semaphore
            sSemaphore = new Semaphore(1, true);

    private AlertDialog
            mAboutAlertDialog = null;

    private MainAdapter
            mMainAdapter = null;

    private RecyclerView
            mRecyclerView = null;

    private boolean
            mAutoScroll = true;

    private SpannableString getBullet(@ColorInt int color) {
        Log.d(TAG, "getBullet::color [" + color + "]");

        SpannableString output = new SpannableString("  ");

        output.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return output;
    }

    private boolean getAutoScroll() {
        Log.d(TAG, "getAutoScroll");

        boolean autoScroll;

        acquire();

        autoScroll = mAutoScroll;

        release();

        return autoScroll;
    }

    private void acquire() {
        Log.d(TAG, "acquire");

        sSemaphore.acquireUninterruptibly();
    }

    private void release() {
        Log.d(TAG, "release");

        sSemaphore.release();

        Log.d(TAG, "release::semaphore.availablePermits() [" + sSemaphore.availablePermits() + "]");
    }

    private void setAutoScroll(boolean autoScroll) {
        Log.d(TAG, "setAutoScroll::autoScroll [" + autoScroll + "]");

        acquire();

        mAutoScroll = autoScroll;

        release();
    }

    private void updateContentScrolling(String delim, String message) {
        Log.d(TAG, "updateContentScrolling:: delim [" + delim + "]");

        String[] trace = null;

        if (delim != null && !delim.isEmpty()) {
            trace = message.split(delim);
        } else {
            trace = new String[] { message };
        }

        Semaphore[] semaphore = { new Semaphore(0, true) };

        for (String chunk : trace) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int limit = MAIN_ADAPTER_CONTENT_LIMIT;

                        mMainAdapter.push(chunk);

                        int count = mMainAdapter.getItemCount();

                        if (!getAutoScroll()) {
                            return;
                        }

                        if (count >= limit) {
                            mMainAdapter.clear(0, count - (limit / 2));

                            count = mMainAdapter.getItemCount();
                        }

                        mRecyclerView.scrollToPosition(count - 1);
                    } finally {
                        semaphore[0].release();
                    }
                }
            });

            semaphore[0].acquireUninterruptibly();
        }
    }

    private void updateStatus(int status, String message) {
        Log.d(TAG, "updateStatus::status [" + status + "]");

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
                ((TextView) findViewById(R.id.tv_app_status)).setText(content[0]);

                semaphore[0].release();
            }
        });

        semaphore[0].acquireUninterruptibly();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAutoScroll(true);

                        int limit = MAIN_ADAPTER_CONTENT_LIMIT;

                        int count = mMainAdapter.getItemCount();

                        if (count >= limit) {
                            mMainAdapter.clear(0, count - (limit / 2));

                            count = mMainAdapter.getItemCount();
                        }

                        mRecyclerView.scrollToPosition(count - 1);

                        binding.fab.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        mRecyclerView = findViewById(R.id.rv_main_content);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setScrollContainer(true);
        mRecyclerView.setItemAnimator(null);

        mMainAdapter = new MainAdapter();

        mRecyclerView.setAdapter(mMainAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                Log.d(TAG, "onInterceptTouchEvent");

                setAutoScroll(false);

                binding.fab.setVisibility(View.VISIBLE);

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                Log.d(TAG, "onTouchEvent");
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                Log.d(TAG, "onRequestDisallowInterceptTouchEvent");
            }
        });

        mAboutAlertDialog = new AboutAlertDialog(this);

        new Thread() { // TODO: new method!?
            @Override
            public void run() {
                super.run();

                IServiceCallback serviceCallback = new IServiceCallback.Stub() {
                    @Override
                    public int onSelectionRequired(Bundle output) {
                        Log.d(TAG, "onSelectionRequired");

                        try {
                            String trace = DataUtility.getJSONObjectFromBundle(output, true).toString();

                            Log.d(TAG, "onSelectionRequired::" + trace);
                        } catch (Exception exception) {
                            Log.e(TAG, Log.getStackTraceString(exception));
                        }

                        // TODO: AlertDialog!?

                        return 1;
                    }

                    @Override
                    public void onNotificationThrow(Bundle output, int type) {
                        Log.d(TAG, "onNotificationThrow");

                        try {
                            String trace = DataUtility.getJSONObjectFromBundle(output, true).toString();

                            Log.d(TAG, "onNotificationThrow::type [" + type + "] trace " + trace + "");
                        } catch (Exception exception) {
                            Log.e(TAG, Log.getStackTraceString(exception));
                        }

                        switch (type) {
                            case NTF_UPDATING:
                                /* 2021-09-23: disposable considering the purposes of this application */
                                return;

                            default:
                                /* Nothing to do */
                                break;
                        }

                        // TODO: AlertDialog!?
                    }
                };

                updateStatus(2, getString(R.string.warning_local_processing));

                Bundle request;

                List<Bundle> requestList = new ArrayList<>(0);

                // requestList.add(DEMO.CLX());
                   requestList.add(DEMO.GIX());
                // requestList.add(DEMO.OPN());
                // requestList.add(DEMO.TLI());

                // for (Bundle TLR : DEMO.TLR()) requestList.add(TLR);

                // requestList.add(DEMO.TLE());
                // requestList.add(DEMO.CEX());
                // requestList.add(DEMO.GTK());
                // requestList.add(DEMO.RMC());
                // requestList.add(DEMO.EBX());
                // requestList.add(DEMO.GPN());
                // requestList.add(DEMO.GCX());
                // requestList.add(DEMO.GED());
                // requestList.add(DEMO.GOX());
                // requestList.add(DEMO.FCX());
                // requestList.add(DEMO.MNU());
                // requestList.add(DEMO.GCD());
                // requestList.add(DEMO.CHP());

                PinpadManager.abort();

                for (Bundle TX : requestList) {
                    try {
                        updateContentScrolling(null, "\"TX\": " + DataUtility.getJSONObjectFromBundle(TX).toString(4));

                        Bundle RX = PinpadManager.request(serviceCallback, TX);

                        updateContentScrolling(null, "\"RX\": " + DataUtility.getJSONObjectFromBundle(RX).toString(4));

                        if (wasStopped()) {
                            return;
                        }
                    } catch (Exception exception) {
                        updateContentScrolling(null, Log.getStackTraceString(exception));
                    }
                }

                updateStatus(0, "Finished processing local requests");
            }
        }.start();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();

        mAboutAlertDialog.dismiss();

        finish();

        new Thread() {
            @Override
            public void run() {
                super.run();

                PinpadManager.abort();
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
