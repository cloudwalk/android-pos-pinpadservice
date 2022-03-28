package io.cloudwalk.pos.pinpadservice.presentation;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.cloudwalk.pos.pinpadservice.utilities.VerifoneUtility.sAcessoDiretoPinpad;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import br.com.verifone.bibliotecapinpad.TecladoPINVirtual;
import br.com.verifone.bibliotecapinpad.definicoes.IdentificacaoTeclaPIN;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.utilitieslibrary.Application;

public class PinCaptureActivity extends AppCompatActivity {
    private static final String
            TAG = PinCaptureActivity.class.getSimpleName();

    private static final Semaphore
            sCreationSemaphore  = new Semaphore(0, true);

    private static final Semaphore
            sLifecycleSemaphore = new Semaphore(1, true);

    private static AppCompatActivity
            sActivity = null;

    private static String
            sApplicationId = null;

    private Map<IdentificacaoTeclaPIN, Integer> getPINViewMap() {
        // Log.d(TAG, "getPINViewMap");

        Map<IdentificacaoTeclaPIN, Integer> map = new HashMap<>();

        map.put(IdentificacaoTeclaPIN.TECLA_0,          R.id.default_keyboard_custom_pos12);
        map.put(IdentificacaoTeclaPIN.TECLA_1,          R.id.default_keyboard_custom_pos00);
        map.put(IdentificacaoTeclaPIN.TECLA_2,          R.id.default_keyboard_custom_pos01);
        map.put(IdentificacaoTeclaPIN.TECLA_3,          R.id.default_keyboard_custom_pos02);
        map.put(IdentificacaoTeclaPIN.TECLA_4,          R.id.default_keyboard_custom_pos04);
        map.put(IdentificacaoTeclaPIN.TECLA_5,          R.id.default_keyboard_custom_pos05);
        map.put(IdentificacaoTeclaPIN.TECLA_6,          R.id.default_keyboard_custom_pos06);
        map.put(IdentificacaoTeclaPIN.TECLA_7,          R.id.default_keyboard_custom_pos08);
        map.put(IdentificacaoTeclaPIN.TECLA_8,          R.id.default_keyboard_custom_pos09);
        map.put(IdentificacaoTeclaPIN.TECLA_9,          R.id.default_keyboard_custom_pos10);
        map.put(IdentificacaoTeclaPIN.TECLA_CONFIRMA,   R.id.default_keyboard_custom_pos11);
        map.put(IdentificacaoTeclaPIN.TECLA_CANCELA,    R.id.default_keyboard_custom_pos03);
        map.put(IdentificacaoTeclaPIN.TECLA_VOLTA,      R.id.default_keyboard_custom_pos07);

        return map;
    }

    private TecladoPINVirtual getTecladoPINVirtual() {
        // Log.d(TAG, "getTecladoPINVirtual");

        Semaphore semaphore = new Semaphore(0, true);
        View[]    keyboard  = { null };

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                keyboard[0] = findViewById(R.id.cl_keyboard);

                semaphore.release();
            }
        });

        semaphore.acquireUninterruptibly();

        return new TecladoPINVirtual(keyboard[0], getPINViewMap()) {
            @Override
            public View ObtemView() {
                return keyboard[0];
            }

            @Override
            public Map<IdentificacaoTeclaPIN, Integer> ObtemIdentificacaoTeclasPorId() {
                return getPINViewMap();
            }
        };
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.default_activity_pin_capture);

        int versionCode = -1;

        try {
            versionCode = getPackageManager().getPackageInfo(sApplicationId, 0).versionCode;

            Log.d(TAG, "onCreate::sApplicationId [" + sApplicationId + "] versionCode [" + versionCode + "]");
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        switch (sApplicationId) {
            case "io.cloudwalk.appclientcw":
                if (versionCode < 203) {
                    int background = Color.parseColor("#E5E5E5");

                    findViewById(R.id.fl_keyboard).setBackgroundColor(background);
                }
                break;

            case "io.cloudwalk.pos.poc2104301453.demo":
                // TODO: add blurred gradient background?!
                break;

            default:
                /* Nothing to do */
                break;
        }

        sLifecycleSemaphore.acquireUninterruptibly();

        sActivity = this;

        sLifecycleSemaphore.release();

        RelativeLayout relativeLayout = findViewById(R.id.default_rl_pin_capture);

        relativeLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        sAcessoDiretoPinpad.InformaTecladoPINVirtual(getTecladoPINVirtual());

                        relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        new Thread() {
                            @Override
                            public void run() {
                                super.run();

                                sCreationSemaphore.release();
                            }
                        } .start();
                    }
                });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        try {
            if (!sCreationSemaphore.tryAcquire(0, TimeUnit.MILLISECONDS)) {
                try {
                    sLifecycleSemaphore.acquireUninterruptibly();

                    if (sActivity != null) {
                        ((ActivityManager) (Application.getContext().getSystemService(ACTIVITY_SERVICE)))
                                .moveTaskToFront(sActivity.getTaskId(), 0);
                    }
                } finally {
                    sLifecycleSemaphore.release();
                }
            } else {
                sCreationSemaphore.release();
            }

            super.onPause();

            overridePendingTransition(0, 0);
        } catch (InterruptedException exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        overridePendingTransition(0, 0);
    }

    public static void resumeActivity() {
        Log.d(TAG, "resumeActivity");

        try {
            sLifecycleSemaphore.acquireUninterruptibly();

            if (sActivity != null) {
                ((ActivityManager) (Application.getContext().getSystemService(ACTIVITY_SERVICE)))
                        .moveTaskToFront(sActivity.getTaskId(), 0);
            }
        } finally {
            sLifecycleSemaphore.release();
        }
    }

    public static void finishActivity() {
        Log.d(TAG, "finishActivity");

        try {
            sLifecycleSemaphore.acquireUninterruptibly();

            if (sActivity != null) {
                sActivity.finishAndRemoveTask();

                sActivity = null;
                sApplicationId = null;
            }
        } finally {
            sLifecycleSemaphore.release();
        }
    }

    public static void moveActivity(boolean front) {
        Log.d(TAG, "moveActivity::front [" + front + "]");

        try {
            sLifecycleSemaphore.acquireUninterruptibly();

            if (sActivity != null) {
                Semaphore semaphore = new Semaphore(0, true);

                sActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sActivity.findViewById(R.id.default_rl_pin_capture).setVisibility((front) ? VISIBLE : INVISIBLE);

                        if (front) {
                            ((ActivityManager) (Application.getContext().getSystemService(ACTIVITY_SERVICE)))
                                    .moveTaskToFront(sActivity.getTaskId(), 0);
                        }

                        semaphore.release();
                    }
                });

                semaphore.acquireUninterruptibly();
            }
        } finally {
            sLifecycleSemaphore.release();
        }
    }

    public static void startActivity(@NotNull String applicationId) {
        Log.d(TAG, "startActivity::applicationId [" + applicationId + "]");

        PinCaptureActivity.finishActivity();

        sApplicationId = applicationId;

        Context context = Application.getContext();

        context.startActivity(new Intent(context, PinCaptureActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        sCreationSemaphore.acquireUninterruptibly();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");

        return super.onTouchEvent(event);
    }
}
