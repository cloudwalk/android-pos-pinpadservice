package io.cloudwalk.pos.pinpadservice.presentation;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_activity_pin);

        PinKeyboard pinKeyboard = findViewById(R.id.keyboard_pin);

        TecladoPINVirtual tecladoPINVirtual = new TecladoPINVirtual(pinKeyboard, pinKeyboard.getPINViewMap()) {
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
                            ((TextView) findViewById(R.id.tv_main)).setText(message);
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

        sSemaphore[0].release();
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
