package io.cloudwalk.pos.demo.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.demo.databinding.ActivityMainBinding;
import io.cloudwalk.pos.pinpadlibrary.managers.PinpadManager;
import io.cloudwalk.pos.utilitieslibrary.utilities.DataUtility;

import static java.nio.charset.StandardCharsets.UTF_8;

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

                byte[] cmd = "OPN".getBytes(UTF_8);

                byte[] pkt = new byte[2044 + 4];

                pkt[0] = 0x16; /* PKTSTART */

                int length = Math.min(cmd.length, 2044 + 4);

                int j = 1;

                for (int i = 0; i < length; i++) {
                    switch (cmd[i]) {
                        case 0x13: /* DC3 */
                            pkt[j++] = 0x13;
                            pkt[j++] = 0x33;
                            break;

                        case 0x16: /* SYN */
                            pkt[j++] = 0x13;
                            pkt[j++] = 0x36;
                            break;

                        case 0x17: /* ETB */
                            pkt[j++] = 0x13;
                            pkt[j++] = 0x37;
                            break;

                        default:
                            pkt[j++] = cmd[i];
                            break;
                    }
                }

                pkt[j] = 0x17; /* PKTSTOP */

                byte[] crc = new byte[cmd.length + 1];

                System.arraycopy(cmd, 0, crc, 0, cmd.length);

                crc[cmd.length] = pkt[j];

                crc = DataUtility.CRC16_XMODEM(crc);

                System.arraycopy(crc, 0, pkt, j + 1, crc.length);

                pkt = DataUtility.trimByteArray(pkt);

                byte[] response = PinpadManager.request(pkt);
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
