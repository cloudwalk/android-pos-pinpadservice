package io.cloudwalk.pos.pinpadservice.presentation;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import br.com.setis.sunmi.ppcomp.PINplug;
import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.utilitieslibrary.Application;

public class PinCaptureActivity extends AppCompatActivity {
    private static final String
            TAG = PinCaptureActivity.class.getSimpleName();

    private static final Semaphore
            sLifeCycleSemaphore = new Semaphore(1, true);

    private static AppCompatActivity
            sActivity = null;

    private static String
            sApplicationId = null;

    private String buildJSONLayoutInfo() {
        // Log.d(TAG, "buildJSONLayoutInfo");

        int[] locationCAN   = new int[2];
        int[] locationNUM   = new int[2];
        int[] width         = new int[2];
        int[] height        = new int[2];

        do {
            View[] btn = {
                    findViewById(R.id.default_keyboard_custom_pos03),
                    findViewById(R.id.default_keyboard_custom_pos00)
            };

            btn[0].getLocationOnScreen(locationCAN);
            btn[1].getLocationOnScreen(locationNUM);

            width[0]  = btn[0].getWidth();
            width[1]  = btn[1].getWidth();

            height[0] = btn[0].getHeight();
            height[1] = btn[1].getHeight();
        } while (locationCAN[0] == 0 && locationCAN[1] == 0
              && locationNUM[0] == 0 && locationNUM[1] == 0);

        int numX = locationNUM[0];
        int numY = locationNUM[1];

        int numW = width[1];
        int numH = height[1];
        int linW = 0;
        int canX = locationCAN[0];
        int canY = locationCAN[1];
        int canW = width[0];
        int canH = height[0];
        int rows = 4;
        int cols = 4;
        byte[] keyMap = buildKeyMap(rows, cols);

        JSONObject obj = new JSONObject();

        try {
            obj.put("layout",   R.layout.default_activity_pin_capture);

            obj.put("numX",     numX);
            obj.put("numY",     numY);
            obj.put("numW",     numW);
            obj.put("numH",     numH);
            obj.put("lineW",    linW);
            obj.put("cancelX",  canX);
            obj.put("cancelY",  canY);
            obj.put("cancelW",  canW);
            obj.put("cancelH",  canH);
            obj.put("rows",     rows);
            obj.put("clos",     cols);

            for (int i = 0; i < rows * cols ; i++) {
                String txt = String.format (Locale.US,"keymap%d", i);
                obj.put (txt, keyMap[i]);
            }

            return obj.toString();
        } catch (JSONException exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        return "";
    }

    private byte[] buildKeyMap(int rows, int cols) {
        // Log.d(TAG, "buildKeyMap");

        byte[] keyMap   = new byte[rows * cols];

        //   0   1   2   3  |  1   2   3   C
        //   4   5   6   7  |  4   5   6   L
        //   8   9  10  11  |  7   8   9   E
        //  12  13  14  15  |      0       E

        keyMap[ 0] = '1';
        keyMap[ 1] = '2';
        keyMap[ 2] = '3';

        keyMap[ 4] = '4';
        keyMap[ 5] = '5';
        keyMap[ 6] = '6';

        keyMap[ 8] = '7';
        keyMap[ 9] = '8';
        keyMap[10] = '9';

        keyMap[12] = ' ';
        keyMap[13] = '0';
        keyMap[14] = ' ';

        keyMap[ 3] = 0x1B; // C
        keyMap[ 7] = 0x0C; // L
        keyMap[11] = 0x0D; // E
        keyMap[15] = 0x0D; // E

        return keyMap;
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

        sActivity = this;

        RelativeLayout relativeLayout = findViewById(R.id.default_rl_pin_capture);

        relativeLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        String JSONLayoutInfo = buildJSONLayoutInfo();

                        relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        new Thread() {
                            @Override
                            public void run() {
                                super.run();

                                PINplug.setPinpadType(1, JSONLayoutInfo);

                                sLifeCycleSemaphore.release();
                            }
                        } .start();
                    }
                });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        try {
            if (!sLifeCycleSemaphore.tryAcquire(0, TimeUnit.MILLISECONDS)) {
                if (sActivity != null) {
                    ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                            .moveTaskToFront(sActivity.getTaskId(), 0);
                }
            } else {
                sLifeCycleSemaphore.release();
            }

            super.onPause();

            overridePendingTransition(0, 0);
        } catch (Exception exception) {
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

        if (sActivity != null) {
            ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                    .moveTaskToFront(sActivity.getTaskId(), 0);
        }
    }

    public static void finishActivity() {
        Log.d(TAG, "finishActivity");

        if (sActivity != null) {
            sActivity.finishAndRemoveTask();

            sActivity = null;
            sApplicationId = null;

            sLifeCycleSemaphore.release();
        }
    }

    public static void moveActivityToFront(boolean status) {
        Log.d(TAG, "moveActivityToFront::status [" + status + "]");

        if (sActivity != null) {
            Semaphore semaphore = new Semaphore(0, true);

            sActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sActivity.findViewById(R.id.default_rl_pin_capture).setVisibility((status) ? VISIBLE : INVISIBLE);

                    if (status) {
                        ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                                .moveTaskToFront(sActivity.getTaskId(), 0);
                    }

                    semaphore.release();
                }
            });

            semaphore.acquireUninterruptibly();
        }
    }

    public static void startActivity(@NotNull String applicationId) {
        Log.d(TAG, "startActivity::applicationId [" + applicationId + "]");

        PinCaptureActivity.finishActivity();

        sLifeCycleSemaphore.acquireUninterruptibly();

        sApplicationId = applicationId;

        Context context = Application.getPackageContext();

        context.startActivity(new Intent(context, PinCaptureActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        sLifeCycleSemaphore.acquireUninterruptibly();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");

        return super.onTouchEvent(event);
    }
}
