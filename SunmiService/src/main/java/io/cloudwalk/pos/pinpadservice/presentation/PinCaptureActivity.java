package io.cloudwalk.pos.pinpadservice.presentation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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

    private static Bundle
            sExtras = null;

    private static String
            sApplicationId = null;

    private String buildJSONLayoutInfo() {
        // Log.d(TAG, "buildJSONLayoutInfo");

        int[] locationCAN   = new int[2];
        int[] locationNUM   = new int[2];
        int[] width         = new int[2];
        int[] height        = new int[2];

        do {
            View btnC = findViewById(sExtras.getInt("keyboard_custom_pos00"));

            btnC.getLocationOnScreen(locationCAN);
            btnC.getLocationOnScreen(locationNUM);

            width[0]  = btnC.getWidth();
            width[1]  = btnC.getWidth();

            height[0] = btnC.getHeight();
            height[1] = btnC.getHeight();
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
        int rows = 5;
        int cols = 6;
        byte[] keyMap = buildKeyMap(rows, cols);

        JSONObject obj = new JSONObject();

        try {
            obj.put("layout",   sExtras.getInt("activity_pin_capture"));

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

        String keyList  = "123456789";
        byte[] keyMap   = new byte[rows * cols];

        //   0   1   2   3   4   5 |  C  C  C  E  E  E
        //   6   7   8   9  10  11 |  1  1  2  2  3  3
        //  12  13  14  15  16  17 |  4  4  5  5  6  6
        //  18  19  20  21  22  23 |  7  7  8  8  9  9
        //  24  25  26  27  28  29 |        0  0  L  L

        keyMap[ 0] = keyMap[ 1] = keyMap[2] = 0x1B; // C
        keyMap[ 3] = keyMap[ 4] = keyMap[5] = 0x0D; // E
        keyMap[24] = keyMap[25] = ' ';
        keyMap[26] = keyMap[27] = '0';
        keyMap[28] = keyMap[29] = 0x0C;             // L

        for (int i = 6, j = 0; i < 23; i += 2, j++) {
            keyMap[i]       = (byte) keyList.charAt(j);
            keyMap[i + 1]   = keyMap [i];
        }

        return keyMap;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        sExtras = getIntent().getExtras();

        setContentView(sExtras.getInt("activity_pin_capture"));

        sActivity = this;

        RelativeLayout relativeLayout = findViewById(sExtras.getInt("rl_pin_capture"));

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

        int availablePermits = sLifeCycleSemaphore.availablePermits();

        Log.d(TAG, "onPause::availablePermits [" + availablePermits + "]");

        if (availablePermits <= 0) {
            ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                    .moveTaskToFront(getTaskId(), 0);
        }

        super.onPause();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        overridePendingTransition(0, 0);
    }

    public static void finishActivity() {
        Log.d(TAG, "finishActivity");

        if (sActivity != null) {
            sActivity.finishAndRemoveTask();

            sActivity = null;
            sApplicationId = null;
            sExtras = null;

            sLifeCycleSemaphore.release();
        }
    }

    public static Bundle getKeyboardResID(String applicationId) {
        Log.d(TAG, "getKeyboardResID::applicationId [" + applicationId + "]");

        Bundle bundle = new Bundle();

        if (!applicationId.equals    ("io.cloudwalk.pos.poc2104301453.demo")
           & applicationId.startsWith("io.cloudwalk.")) {
            bundle.putInt("activity_pin_capture", R.layout.infinitepay_activity_pin_capture);
            bundle.putInt("rl_pin_capture", R.id.infinitepay_rl_pin_capture);
            bundle.putInt("keyboard_custom_pos00", R.id.infinitepay_keyboard_custom_pos00);
        } else {
            bundle.putInt("activity_pin_capture", R.layout.default_activity_pin_capture);
            bundle.putInt("rl_pin_capture", R.id.default_rl_pin_capture);
            bundle.putInt("keyboard_custom_pos00", R.id.default_keyboard_custom_pos00);
        }

        return bundle;
    }

    public static void setVisibility(boolean status) {
        Log.d(TAG, "setVisibility::status [" + status + "]");

        if (sActivity != null && sExtras != null) {
            Semaphore semaphore = new Semaphore(0, true);

            sActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status) {
                        sActivity.findViewById(sExtras.getInt("rl_pin_capture")).setVisibility(View.VISIBLE);

                        ((ActivityManager) (Application.getPackageContext().getSystemService(ACTIVITY_SERVICE)))
                                .moveTaskToFront(sActivity.getTaskId(), 0);
                    } else {
                        finishActivity();
                    }

                    semaphore.release();
                }
            });

            semaphore.acquireUninterruptibly();
        }
    }

    public static void startActivity(@NotNull String applicationId) {
        Log.d(TAG, "startActivity");

        sLifeCycleSemaphore.acquireUninterruptibly();

        sApplicationId = applicationId;

        Bundle keyboardResID = getKeyboardResID(sApplicationId);

        Context context = Application.getPackageContext();

        Intent intent = new Intent(context, PinCaptureActivity.class).putExtras(keyboardResID);

        context.startActivity(intent);

        sLifeCycleSemaphore.acquireUninterruptibly();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");

        return super.onTouchEvent(event);
    }
}
