package io.cloudwalk.pos.pinpadservice.presentation;

import static io.cloudwalk.pos.pinpadlibrary.IServiceCallback.*;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.Semaphore;

import br.com.setis.sunmi.ppcomp.PINplug;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.pos.utilitieslibrary.AppCompatActivity;
import io.cloudwalk.pos.utilitieslibrary.Application;

public class PinCaptureActivity extends AppCompatActivity {
    private static final String
            TAG = PinCaptureActivity.class.getSimpleName();

    private static final Semaphore
            sSemaphore = new Semaphore(0, true);

    @SuppressLint("StaticFieldLeak")
    private static AppCompatActivity
            mActivity = null;

    private String buildJSONLayoutInfo() {
        Log.d(TAG, "buildJSONLayoutInfo");

        int[] locationCAN = new int[2];
        int[] locationNUM = new int[2];
        int[] width       = new int[2];
        int[] height      = new int[2];

        do {
            View btnC = findViewById(R.id.keyboard_custom_pos00);

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
            obj.put("layout",   R.layout.activity_pin_capture);

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
        Log.d(TAG, "buildKeyMap");

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

    private void release(int permits) {
        Log.d(TAG, "release::permits [" + permits + "]");

        sSemaphore.release(permits);
    }

    private static void setVisibility(boolean status) {
        Log.d(TAG, "setVisibility::status [" + status + "]");

        AppCompatActivity activity = mActivity; // TODO: getActivity(); for thread-safeness

        Semaphore semaphore = new Semaphore(0, true);

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.findViewById(R.id.rl_pin_capture).setVisibility((status) ? View.VISIBLE : View.INVISIBLE);

                    semaphore.release();
                }
            });
        }

        semaphore.acquireUninterruptibly();

        Log.d(TAG, "setVisiblity::R.id.rl_pin_capture [" + status + "]");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_capture);

        mActivity = this; // TODO: setActivity(); for thread-safeness

        RelativeLayout relativeLayout = findViewById(R.id.rl_pin_capture);

        relativeLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                PINplug.setPinpadType(1, buildJSONLayoutInfo());

                onNotificationThrow(-1);

                relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                release(2);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        overridePendingTransition(0, 0);
    }

    public static void acquire() {
        Log.d(TAG, "acquire");

        sSemaphore.acquireUninterruptibly();
    }

    public static void onNotificationThrow(int type) {
        Log.d(TAG, "onNotificationThrow");

        AppCompatActivity activity = mActivity; // TODO: getActivity(); for thread-safeness

        Log.d(TAG, "onNotificationThrow::activity [" + activity + "]");

        if (activity == null) {
            return;
        }

        Log.d(TAG, "onNotificationThrow::type [" + type + "]");

        switch (type) {
            case NTF_PIN_START:
                ((ActivityManager) (Application.getPackageContext().getSystemService(Context.ACTIVITY_SERVICE)))
                        .moveTaskToFront(activity.getTaskId(), 0);

                setVisibility(true);
                return;

            case NTF_PIN_ENTRY:
                /* Nothing to do */
                return;

            case NTF_PIN_FINISH:
            default:
                setVisibility(false);
                break;
        }

        activity.moveTaskToBack(true);
    }

    public static void release() {
        Log.d(TAG, "release");

        int availablePermits = sSemaphore.availablePermits();

        Log.d(TAG, "release::availablePermits [" + availablePermits + "]");

        if (availablePermits <= 0) {
            sSemaphore.release();
        }
    }
}
