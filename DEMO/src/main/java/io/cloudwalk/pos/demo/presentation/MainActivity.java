package io.cloudwalk.pos.demo.presentation;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.demo.PinpadServer;
import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Semaphore sOnBackPressedSemaphore = new Semaphore(1, true);

    private PinpadServer sPinpadServer = null;

    private boolean sOnBackPressed = false;

    private SpannableString getBullet(@ColorInt int color) {
        SpannableString output = new SpannableString("  ");

        output.setSpan(new BulletSpan(7, color), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return output;
    }

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

    private void updateStatus(int status, String message) {
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

        binding.fab.setVisibility(View.GONE);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* 'onCreate' shouldn't be blocked by potentially demanding routines, hence the thread */
        new Thread() {
            @Override
            public void run() {
                super.run();

                updateStatus(2, getString(R.string.warning_local_processing));

                Bundle request = new Bundle();

                List<Bundle> requestList = new ArrayList<>(0);

                request.putString(ABECS.CMD_ID,     ABECS.OPN);
                request.putString(ABECS.OPN_OPMODE, "0");
                request.putString(ABECS.OPN_MOD,    "A82A660B3C49226EFCDABA7FC68066B83D23D0560EDA3A12B63E9132F299FBF340A5AEBC4CD5DC1F14873F83A80BA9A88D3FEABBAB41DFFC1944BBBAA89F26AF9CC28FF31C497EB91D82F8613E7463C47529FBD1925FD3326A8DC027704DA68860E68BD0A1CEA8DE6EC75604CD3D9A6AF38822DE45AAA0C9FBF2BD4783B0F9A81F6350C0188156F908FAB1F559CFCE1F91A393431E8BF2CD78C04BD530DB441091CDFFB400DAC08B1450DB65C00E2D4AF4E9A85A1A19B61F550F0C289B14BD63DF8A1539A8CF629F98F88EA944D9056675000F95BFD0FEFC56F9D9D66E2701BDBD71933191AE9928F5D623FE8B99ECC777444FFAA83DE456F5C8D3C83EC511AF");
                request.putString(ABECS.OPN_EXP,    "0D");

                // requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID,     ABECS.CLX);
                request.putString(ABECS.SPE_DSPMSG, " HAVE FAITH...  ");
                request.putString(ABECS.SPE_MFNAME, "FAITH000");

                // requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID,     ABECS.GIX);

                // requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID,     ABECS.GIX);
                request.putString(ABECS.SPE_IDLIST, "800180048035910A910B920A920B930093049363");

                // requestList.add(request);

                request = new Bundle();

                request.putString(ABECS.CMD_ID,     ABECS.GIX);
                request.putString(ABECS.SPE_IDLIST, "800180028003800480058006800780088009800A80108011801280138014801580168032803380358036910A920B9300");

                requestList.add(request);

                Semaphore semaphore = new Semaphore(0, true);

                // TODO: auto-scrolling?

                String[] contentScrolling = { "" };

                for (Bundle item : requestList) {
                    Bundle response = PinpadManager.request(item);

                    contentScrolling[0] += ((contentScrolling[0].isEmpty()) ? "" : "\r\n");

                    try {
                        contentScrolling[0] += DataUtility.bundleToJSON(response).toString(4) + "\r\n";
                    } catch (Exception exception) {
                        contentScrolling[0] += Log.getStackTraceString(exception);
                    }

                    // String[] trace = contentScrolling.split("\n");

                    // for (String slice : trace) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tvMainContentScrolling = findViewById(R.id.tv_main_content_scrolling);

                                tvMainContentScrolling.setText(contentScrolling[0]);

                                // tvMainContentScrolling.append("\r\n" + slice);

                                // ((NestedScrollView) findViewById(R.id.nsv_main_content_scrolling)).smoothScrollTo(0, tvMainContentScrolling.getBottom());

                                semaphore.release();
                            }
                        });

                        semaphore.acquireUninterruptibly();
                    // }

                    if (getOnBackPressed()) {
                        /* Ensures not to go any further if the user has decided to abort */
                        break;
                    }
                }

                // TODO: start server according to 'fab' press?

                updateStatus(2, "Bringing up server...");

                sPinpadServer = new PinpadServer(new PinpadServer.Callback() {
                    @Override
                    public void onFailure(Exception exception) {
                        Log.d(TAG, "onFailure");

                        updateStatus(1, "Server offline\r\n  " + exception.getMessage());
                    }

                    @Override
                    public void onRecv(byte[] trace, int length) {
                        Log.d(TAG, "onRecv");

                        Log.h(TAG, trace, length);
                    }

                    @Override
                    public void onSend(byte[] trace, int length) {
                        Log.d(TAG, "onSend");

                        Log.h(TAG, trace, length);
                    }

                    @Override
                    public void onSuccess(String localSocket) {
                        Log.d(TAG, "onSuccess");

                        updateStatus(0, "Server up and running " + localSocket);
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        super.onBackPressed();

        setOnBackPressed(true);

        finish();

        sPinpadServer.close();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        overridePendingTransition(0, 0);

        super.onResume();
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
