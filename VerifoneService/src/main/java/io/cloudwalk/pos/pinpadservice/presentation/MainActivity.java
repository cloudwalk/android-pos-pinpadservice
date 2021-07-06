package io.cloudwalk.pos.pinpadservice.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;
import java.util.concurrent.Semaphore;

import br.com.verifone.bibliotecapinpad.TecladoPINVirtual;
import br.com.verifone.bibliotecapinpad.definicoes.IdentificacaoTeclaPIN;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.pos.pinpadservice.managers.PinpadManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_LOGCAT = MainActivity.class.getSimpleName();

    private static final
            Semaphore[] sSemaphore = {
                    new Semaphore(0, true),
                    new Semaphore(0, true)
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphore[1].acquireUninterruptibly();

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
}
