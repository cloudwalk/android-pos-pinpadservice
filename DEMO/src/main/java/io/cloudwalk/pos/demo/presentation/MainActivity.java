package io.cloudwalk.pos.demo.presentation;

import static java.util.Locale.US;

import static io.cloudwalk.pos.Application.sPinpadServer;

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
import io.cloudwalk.pos.demo.DEMO_aline;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.internals.utilities.PinpadUtility;
import io.cloudwalk.pos.pinpadserver.PinpadServer;
import io.cloudwalk.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.adapters.MainAdapter;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.IServiceCallback;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.utilitieslibrary.utilities.DataUtility;

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

    private PinpadServer
            mPinpadServer = null;

    private RecyclerView
            mRecyclerView = null;

    private boolean
            mAutoScroll = true;

    private SpannableString getBullet(@ColorInt int color) {
        // Log.d(TAG, "getBullet::color [" + color + "]");

        SpannableString output = new SpannableString("  ");

        output.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return output;
    }

    private boolean getAutoScroll() {
        // Log.d(TAG, "getAutoScroll");

        boolean autoScroll;

        acquire();

        autoScroll = mAutoScroll;

        release();

        return autoScroll;
    }

    private void acquire() {
        // Log.d(TAG, "acquire");

        sSemaphore.acquireUninterruptibly();
    }

    private void release() {
        // Log.d(TAG, "release");

        sSemaphore.release();
    }

    private void setAutoScroll(boolean autoScroll) {
        // Log.d(TAG, "setAutoScroll::autoScroll [" + autoScroll + "]");

        acquire();

        mAutoScroll = autoScroll;

        release();
    }

    private void updateContentScrolling(String delim, String message) {
        // Log.d(TAG, "updateContentScrolling::delim [" + delim + "]");

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

    private void updatePinpadContent(String message) {
        // Log.d(TAG, "updatePinpadContent");

        Semaphore[] semaphore = { new Semaphore(0, true) };

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ((TextView) findViewById(R.id.tv_pinpad_content)).setText(message);
                } finally {
                    semaphore[0].release();
                }
            }
        });

        semaphore[0].acquireUninterruptibly();
    }

    private void updateStatus(int status, String message) {
        // Log.d(TAG, "updateStatus::status [" + status + "]");

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

        setContentView     (binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

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

        ((TextView) findViewById(R.id.tv_pinpad_content)).setText(getString(R.string.warning_wait).toUpperCase(US));

        String label = getString(R.string.app_name_alternative).substring(0, 14).toUpperCase(US);

        new Thread() { // TODO: new method!?
            @Override
            public void run() {
                super.run();

                IServiceCallback serviceCallback = new IServiceCallback.Stub() {
                    @Override
                    public int onServiceCallback(Bundle bundle) {
                        Log.d(TAG, "onServiceCallback");

                        try {
                            String trace = DataUtility.getJSONObjectFromBundle(bundle, true).toString();

                            Log.d(TAG, "onServiceCallback::" + trace);
                        } catch (Exception exception) {
                            Log.e(TAG, Log.getStackTraceString(exception));
                        }

                        String pin  = "                ";
                               pin += bundle.getString(NTF_PIN, "");

                        String msg  = bundle.getString(NTF_MSG, "");

                        if (!msg.isEmpty()) {
                            while (msg.charAt(0) == '\n') msg = msg.substring(1);
                        }

                        msg += "\n" + pin.substring(pin.length() - 16);

                        updatePinpadContent(msg);

                        switch (bundle.getInt(NTF_TYPE, -99)) {
                        //  case...
                        //  case...
                            case NTF_SELECT: return 1;
                            default:         return 0;
                        }
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
                requestList.add(DEMO_aline.GCX());
                // requestList.add(DEMO.GED());
                // requestList.add(DEMO.GOX());
                requestList.add(DEMO_aline.GOX());
                // requestList.add(DEMO.FCX());
                requestList.add(DEMO_aline.FCX());
                // requestList.add(DEMO.MNU());
                // requestList.add(DEMO.GCD());
                // requestList.add(DEMO.CHP());

                PinpadManager.abort();

                for (Bundle TX : requestList) {
                    try {
                        updateContentScrolling(null, "\"TX\": " + DataUtility.getJSONObjectFromBundle(TX).toString(4));

                        Bundle RX = PinpadManager.request(TX, serviceCallback);

                        updateContentScrolling(null, "\"RX\": " + DataUtility.getJSONObjectFromBundle(RX).toString(4));

                        if (wasStopped()) {
                            return;
                        }

                        switch (RX.getString(ABECS.RSP_ID)) {
                            case ABECS.TLI:
                            case ABECS.TLR:
                                /* Nothing to do */
                                break;

                            default:
                                updatePinpadContent(label);
                                break;
                        }
                    } catch (Exception exception) {
                        updateContentScrolling(null, Log.getStackTraceString(exception));
                    }
                }

                // TODO: replace hardcoded strings by values @string.xml

                updateStatus(0, "Finished processing local requests");

                PinpadServer.Callback serverCallback = new PinpadServer.Callback() {
                    @Override
                    public int onPinpadCallback(Bundle bundle) {
                        try {
                            return serviceCallback.onServiceCallback(bundle);
                        } catch (Exception exception) { return -1; }
                    }

                    @Override
                    public void onClientConnection(String address) {
                        // TODO: UX
                    }

                    @Override
                    public void onServerFailure(Exception exception) {
                        // TODO: triple vibration and beep

                        updateStatus(1, "Server failure: " + exception.getMessage());

                        updatePinpadContent(label);
                    }

                    @Override
                    public void onServerReceive(byte[] trace, int length) {
                        try {
                            Bundle TX = PinpadUtility.parseRequestDataPacket(trace, length);

                            updateContentScrolling(null, "\"TX\": " + DataUtility.getJSONObjectFromBundle(TX).toString(4));
                        } catch (Exception exception) {
                            if (length <= 0) { return; }

                            updateContentScrolling("\n", Log.getByteTraceString(trace, length));
                        }
                    }

                    @Override
                    public void onServerSend(byte[] trace, int length) {
                        try {
                            Bundle RX = PinpadUtility.parseResponseDataPacket(trace, length);

                            updateContentScrolling(null, "\"RX\": " + DataUtility.getJSONObjectFromBundle(RX).toString(4));

                            switch (RX.getString(ABECS.RSP_ID)) {
                                case ABECS.TLI:
                                case ABECS.TLR:
                                    /* Nothing to do */
                                    break;

                                default:
                                    updatePinpadContent(label);
                                    break;
                            }
                        } catch (Exception exception) {
                            if (length <= 0) { return; }

                            updateContentScrolling("\n", Log.getByteTraceString(trace, length));
                        }
                    }

                    @Override
                    public void onServerSuccess(String address) {
                        // TODO: vibration and beep

                        updateStatus(0, "Server up an running " + address);
                    }
                };

                updateStatus(2, "Raising server...");

                try {
                    sPinpadServer.set(new PinpadServer(serverCallback));

                    sPinpadServer.get().raise();
                } catch (Exception exception) {
                    serverCallback.onServerFailure(exception);
                }
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

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onOptionsItemSelected");

        // super.onBackPressed();

        moveTaskToBack(true);
    }
}
