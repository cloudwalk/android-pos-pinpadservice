package io.cloudwalk.pos.demo.presentation;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.demo.PinpadServer;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.adapters.MainAdapter;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class MainActivity extends AppCompatActivity {
    private static final String
            TAG = MainActivity.class.getSimpleName();

    private static final Semaphore[]
            SEMAPHORE = {
                    new Semaphore(1, true), /* mAutoScroll, mStopStatus and mServerTraceOn */
                    new Semaphore(1, true), /* mPinpadServer */
            };

    private static final int
            MAIN_ADAPTER_CONTENT_LIMIT = 2000;

    private MainAdapter
            mMainAdapter   = null;

    private PinpadServer
            mPinpadServer  = null;

    private RecyclerView
            mRecyclerView  = null;

    private boolean
            mAutoScroll    = true;

    private boolean
            mServerTraceOn = false;

    private boolean
            mStopStatus    = false;

    private SpannableString getBullet(@ColorInt int color) {
        Log.d(TAG, "getBullet::color [" + color + "]");

        SpannableString output = new SpannableString("  ");

        output.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return output;
    }

    private boolean getAutoScroll() {
        Log.d(TAG, "getAutoScroll");

        boolean autoScroll;

        acquire(0);

        autoScroll = mAutoScroll;

        release(0);

        return autoScroll;
    }

    private boolean getServerTraceStatus() {
        Log.d(TAG, "getServerTraceStatus");

        boolean serverTraceStatus;

        acquire(0);

        serverTraceStatus = mServerTraceOn;

        release(0);

        return serverTraceStatus;
    }

    private boolean getStopStatus() {
        Log.d(TAG, "getStopStatus");

        boolean stopStatus;

        acquire(0);

        stopStatus = mStopStatus;

        release(0);

        return stopStatus;
    }

    private void acquire(int index) {
        Log.d(TAG, "acquire::index [" + index + "]");

        SEMAPHORE[index].acquireUninterruptibly();
    }

    private void processLocalRequests() {
        Log.d(TAG, "processLocalRequests");

        updateStatus(2, getString(R.string.warning_local_processing));

        Bundle request = new Bundle();

        List<Bundle> requestList = new ArrayList<>(0);

        request.putString(ABECS.CMD_ID, ABECS.GIX);
        request.putString(ABECS.SPE_IDLIST, "800180028003800480058006800780088009800A80108011801280138014801580168032803380358036910A920B9300");

        requestList.add(request);

        request = new Bundle();

        request.putString(ABECS.CMD_ID, ABECS.OPN);
        request.putString(ABECS.OPN_OPMODE, "0");
        request.putString(ABECS.OPN_MOD, "A82A660B3C49226EFCDABA7FC68066B83D23D0560EDA3A12B63E9132F299FBF340A5AEBC4CD5DC1F14873F83A80BA9A88D3FEABBAB41DFFC1944BBBAA89F26AF9CC28FF31C497EB91D82F8613E7463C47529FBD1925FD3326A8DC027704DA68860E68BD0A1CEA8DE6EC75604CD3D9A6AF38822DE45AAA0C9FBF2BD4783B0F9A81F6350C0188156F908FAB1F559CFCE1F91A393431E8BF2CD78C04BD530DB441091CDFFB400DAC08B1450DB65C00E2D4AF4E9A85A1A19B61F550F0C289B14BD63DF8A1539A8CF629F98F88EA944D9056675000F95BFD0FEFC56F9D9D66E2701BDBD71933191AE9928F5D623FE8B99ECC777444FFAA83DE456F5C8D3C83EC511AF");
        request.putString(ABECS.OPN_EXP, "0D");

        // requestList.add(request);

        request = new Bundle();

        request.putString(ABECS.CMD_ID, ABECS.CLX);

        // requestList.add(request);

        for (Bundle TX : requestList) {
            try {
                updateContentScrolling(null, "\"TX\": " + DataUtility.bundleToJSON(TX).toString(4));

                Bundle RX = PinpadManager.request(TX);

                updateContentScrolling(null, "\"RX\": " + DataUtility.bundleToJSON(RX).toString(4));

                if (getStopStatus()) {
                    return;
                }
            } catch (Exception exception) {
                updateContentScrolling(null, Log.getStackTraceString(exception));
            }
        }
    }

    private void release(int index) {
        Log.d(TAG, "release::index [" + index + "]");

        SEMAPHORE[index].release();
    }

    private void setAutoScroll(boolean autoScroll) {
        Log.d(TAG, "setAutoScroll::autoScroll [" + autoScroll + "]");

        acquire(0);

        mAutoScroll = autoScroll;

        release(0);
    }

    private void setServerTraceStatus(boolean serverTraceStatus) {
        Log.d(TAG, "setServerTraceStatus::serverTraceStatus [" + serverTraceStatus + "]");

        acquire(0);

        mServerTraceOn = serverTraceStatus;

        release(0);
    }

    private void setStopStatus(boolean stopStatus) {
        Log.d(TAG, "setStopStatus::stopStatus [" + stopStatus + "]");

        acquire(0);

        mStopStatus = stopStatus;

        release(0);
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

        binding.fab.setEnabled(false);

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

                        binding.fab.setEnabled(false);

                        Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_pause_24);

                        binding.fab.setImageDrawable(drawable);
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

                binding.fab.setEnabled(true);

                Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_play_arrow_24);

                binding.fab.setImageDrawable(drawable);

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
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        setStopStatus(true);

        finish();

        /* 'onPause' runs on the UI thread, hence the new thread not to block it */
        new Thread() {
            @Override
            public void run() {
                super.run();

                acquire(1);

                if (mPinpadServer != null) {
                    mPinpadServer.close();
                }

                release(1);
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        super.onBackPressed();

        onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        overridePendingTransition(0, 0);

        super.onResume();

        /* 'onResume' runs on the UI thread, hence the new thread not to block it */
        new Thread() {
            @Override
            public void run() {
                super.run();

                processLocalRequests();

                PinpadServer.Callback callback = new PinpadServer.Callback() {
                    @Override
                    public void onFailure(Exception exception) {
                        Log.d(TAG, "onFailure");

                        updateStatus(1, "Server offline\r\n  " + exception.getMessage());

                        // TODO: reuse 'fab'?
                    }

                    @Override
                    public void onRecv(byte[] trace, int length) {
                        Log.d(TAG, "onRecv");

                        boolean serverTraceOn;

                        if (!(serverTraceOn = getServerTraceStatus())) {
                            mMainAdapter.clear(0, mMainAdapter.getItemCount());
                        }

                        updateContentScrolling(null, ((serverTraceOn) ? "  \r\n" : "") + "RX\r\n" + Log.getByteTraceString(trace, length));

                        setServerTraceStatus(true);
                    }

                    @Override
                    public void onSend(byte[] trace, int length) {
                        Log.d(TAG, "onSend");

                        updateContentScrolling(null, "  \r\nTX\r\n" + Log.getByteTraceString(trace, length));
                    }

                    @Override
                    public void onSuccess(String localSocket) {
                        Log.d(TAG, "onSuccess");

                        updateStatus(0, "Server up and running " + localSocket);
                    }
                };

                acquire(1);

                if (!getStopStatus()) {
                    updateStatus(2, "Bringing up server...");

                    mPinpadServer = new PinpadServer(callback);
                }

                release(1);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
