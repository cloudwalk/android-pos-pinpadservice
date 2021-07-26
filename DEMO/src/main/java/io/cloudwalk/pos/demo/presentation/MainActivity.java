package io.cloudwalk.pos.demo.presentation;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadlibrary.ABECS;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* 'onCreate' shouldn't be blocked by potentially demanding routines, hence the thread */
        new Thread() {
            @Override
            public void run() {
                super.run();

                Bundle request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.OPN);
                request.putLong  (ABECS.OPN_OPMODE, 0);
                request.putString(ABECS.OPN_MOD, "A82A660B3C49226EFCDABA7FC68066B83D23D0560EDA3A12B63E9132F299FBF340A5AEBC4CD5DC1F14873F83A80BA9A88D3FEABBAB41DFFC1944BBBAA89F26AF9CC28FF31C497EB91D82F8613E7463C47529FBD1925FD3326A8DC027704DA68860E68BD0A1CEA8DE6EC75604CD3D9A6AF38822DE45AAA0C9FBF2BD4783B0F9A81F6350C0188156F908FAB1F559CFCE1F91A393431E8BF2CD78C04BD530DB441091CDFFB400DAC08B1450DB65C00E2D4AF4E9A85A1A19B61F550F0C289B14BD63DF8A1539A8CF629F98F88EA944D9056675000F95BFD0FEFC56F9D9D66E2701BDBD71933191AE9928F5D623FE8B99ECC777444FFAA83DE456F5C8D3C83EC511AF");
                request.putString(ABECS.OPN_EXP, "0D");

                Bundle response = PinpadManager.request(request);

                String[] content = { null };

                try {
                    content[0] = DataUtility.bundleToJSON(response).toString(4);
                } catch (Exception exception) {
                    content[0] = Log.getStackTraceString(exception);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_main_content_scrolling)).setText(content[0]);
                    }
                });

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.CLX);
                request.putString(ABECS.SPE_DSPMSG, " HAVE FAITH...  ");
                request.putString(ABECS.SPE_MFNAME, "FAITH000");

                response = PinpadManager.request(request);

                try {
                    content[0] += "\r\n" + DataUtility.bundleToJSON(response).toString(4);
                } catch (Exception exception) {
                    content[0] += "\r\n" + Log.getStackTraceString(exception);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_main_content_scrolling)).setText(content[0]);
                    }
                });

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GIN);
                request.putLong  (ABECS.GIN_ACQIDX, 0);

                response = PinpadManager.request(request);

                try {
                    content[0] += "\r\n" + DataUtility.bundleToJSON(response).toString(4);
                } catch (Exception exception) {
                    content[0] += "\r\n" + Log.getStackTraceString(exception);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_main_content_scrolling)).setText(content[0]);
                    }
                });

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GIN);
                request.putLong  (ABECS.GIN_ACQIDX, 2);

                response = PinpadManager.request(request);

                try {
                    content[0] += "\r\n" + DataUtility.bundleToJSON(response).toString(4);
                } catch (Exception exception) {
                    content[0] += "\r\n" + Log.getStackTraceString(exception);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_main_content_scrolling)).setText(content[0]);
                    }
                });

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GIN);
                request.putLong  (ABECS.GIN_ACQIDX, 3);

                response = PinpadManager.request(request);

                try {
                    content[0] += "\r\n" + DataUtility.bundleToJSON(response).toString(4);
                } catch (Exception exception) {
                    content[0] += "\r\n" + Log.getStackTraceString(exception);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_main_content_scrolling)).setText(content[0]);
                    }
                });

                request = new Bundle();

                request.putString(ABECS.CMD_ID, ABECS.GIN);
                request.putLong  (ABECS.GIN_ACQIDX, 4);

                response = PinpadManager.request(request);

                try {
                    content[0] += "\r\n" + DataUtility.bundleToJSON(response).toString(4);
                } catch (Exception exception) {
                    content[0] += "\r\n" + Log.getStackTraceString(exception);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_main_content_scrolling)).setText(content[0]);
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

        finish();
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
