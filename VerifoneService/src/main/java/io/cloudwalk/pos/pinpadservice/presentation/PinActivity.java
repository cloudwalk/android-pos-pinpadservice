package io.cloudwalk.pos.pinpadservice.presentation;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.TecladoPINVirtual;
import br.com.verifone.bibliotecapinpad.definicoes.IdentificacaoTeclaPIN;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;

public class PinActivity extends AppCompatActivity {
    private static final String TAG = PinActivity.class.getSimpleName();

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
                        PinKeyboard pinKeyboard = findViewById(R.id.cl_default_keyboard_pin);

                        TecladoPINVirtual tecladoPINVirtual = new TecladoPINVirtual(pinKeyboard, PinKeyboard.getPINViewMap()) {
                            @Override
                            public View ObtemView() {
                                return pinKeyboard;
                            }

                            @Override
                            public Map<IdentificacaoTeclaPIN, Integer> ObtemIdentificacaoTeclasPorId() {
                                return pinKeyboard.getPINViewMap();
                            }
                        };

                        PinpadManager.getInstance().getPinpad().InformaTecladoPINVirtual(tecladoPINVirtual);

                        sSemaphore[3].acquireUninterruptibly();

                        sTimestamp = SystemClock.elapsedRealtime();

                        Log.d(TAG, "onCreate::sTimestamp [" + sTimestamp + "]");

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
                    message.append(sMessage);
                    message.append("\r\n");

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
                            findViewById(R.id.tv_default_activity_pin_progress).setVisibility(View.INVISIBLE);

                            findViewById(R.id.cl_default_keyboard_pin).setVisibility(View.VISIBLE);

                            ((TextView) findViewById(R.id.tv_default_activity_pin_main)).setText(message);
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
        Log.d(TAG, "acquireUninterruptibly");

        sSemaphore[0].acquireUninterruptibly();
    }

    public static void release() {
        Log.d(TAG, "release");

        sSemaphore[3].acquireUninterruptibly();

        long timestamp = SystemClock.elapsedRealtime() - sTimestamp;

        Log.d(TAG, "release::timestamp [" + timestamp + "]");

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
