package io.cloudwalk.pos.pinpadservice.presentation;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.pinpadservice.R;

public class PinActivity extends AppCompatActivity {
    private static final String TAG_LOGCAT = PinActivity.class.getSimpleName();

    private static final
            Semaphore[] sSemaphore = {
                    new Semaphore(0, true), /* onCreate */
                    new Semaphore(0, true), /* onFinish */
                    new Semaphore(0, true), /* onUpdate */
                    new Semaphore(1, true)
            };

    private static String   sMessage;

    private static boolean  sRunning;

    private static int      sDigits;

    private static long     sTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_activity_pin);

        new Thread() {
            @Override
            public void run() {
                super.run();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sSemaphore[3].acquireUninterruptibly();

                        sTimestamp = SystemClock.elapsedRealtime();

                        Log.d(TAG_LOGCAT, "onCreate::sTimestamp [" + sTimestamp + "]");

                        sSemaphore[3].release();

                        sSemaphore[0].release();
                    }
                });
            }
        }.start();

        sRunning = true;

        new Thread() {
            @Override
            public void run() {
                super.run();

                while (true) {
                    sSemaphore[2].acquireUninterruptibly();

                    if (!sRunning) {
                        return;
                    }

                    sSemaphore[3].acquireUninterruptibly();

                    StringBuilder message = new StringBuilder();
                    int digits = sDigits;

                    message.append("\r\n");

                    for (int i = 0; i < sMessage.length(); i += 16) {
                        if ((i + 16) < sMessage.length()) {
                            message.append(sMessage.substring(i, i + 16));
                        } else {
                            message.append(sMessage.substring(i));

                            for (int j = sMessage.length(); j <= (i + 16); j++) {
                                message.append(" ");
                            }
                        }
                        message.append("\r\n");
                    }

                    sSemaphore[3].release();

                    if (digits >= 0) {
                        for (int i = 0; i < 16; i++) {
                            if (i < 16 - digits) {
                                message.append(" ");
                            } else {
                                message.append("*");
                            }
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.tv_default_activity_pin_progress)).setText(message);
                        }
                    });
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphore[1].acquireUninterruptibly();

                sRunning = false;

                sSemaphore[2].release();

                finish();
            }
        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public static void acquireUninterruptibly() {
        Log.d(TAG_LOGCAT, "acquireUninterruptibly");

        sSemaphore[0].acquireUninterruptibly();
    }

    public static void release() {
        Log.d(TAG_LOGCAT, "release");

        sSemaphore[3].acquireUninterruptibly();

        long timestamp = SystemClock.elapsedRealtime() - sTimestamp;

        Log.d(TAG_LOGCAT, "release::timestamp [" + timestamp + "]");

        sSemaphore[3].release();

        if (timestamp < 1000) {
            SystemClock.sleep(1000 - timestamp);
        }

        sSemaphore[1].release();
    }

    public static void update(String message, int digits) {
        sSemaphore[3].acquireUninterruptibly();

        sMessage = message;

        sDigits  = digits;

        sSemaphore[3].release();

        sSemaphore[2].release();
    }
}
