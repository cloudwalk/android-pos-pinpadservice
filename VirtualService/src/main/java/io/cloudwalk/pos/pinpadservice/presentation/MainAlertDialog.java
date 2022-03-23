package io.cloudwalk.pos.pinpadservice.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.BuildConfig;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.pos.pinpadservice.utilities.SharedPreferencesUtility;
import io.cloudwalk.utilitieslibrary.Application;

public class MainAlertDialog extends AlertDialog {
    private static final String
            TAG = MainAlertDialog.class.getSimpleName();

    private static final String
            IPV4_PATTERN = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(:(\\d{1,5})?)?)?)?)?)?)?)?";

    private CharSequence onFilterBlock(EditText editText) {
        Log.d(TAG, "onFilterBlock");

        editText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

        return null;
    }

    private void onFilterEntry(EditText editText) {
        Log.d(TAG, "onFilterEntry");

        getButton(BUTTON_POSITIVE).setEnabled(false);

        editText.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
    }

    /**
     * Constructor.
     */
    private MainAlertDialog(Context context) {
        super(context);

        Log.d(TAG, "AboutAlertDialog");
    }

    /**
     * Constructor.
     */
    private MainAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

        Log.d(TAG, "AboutAlertDialog");
    }

    /**
     * Constructor.
     */
    private MainAlertDialog(Context context, int themeResId) {
        super(context, themeResId);

        Log.d(TAG, "AboutAlertDialog");
    }

    /**
     * Constructor.
     */
    protected MainAlertDialog(Activity activity) {
        super(activity);

        Log.d(TAG, "AboutAlertDialog");

        setIcon(R.mipmap.ic_pinpadservice);

        setTitle("Virtual " + activity.getString(R.string.app_name));

        View view = getLayoutInflater().inflate(R.layout.alert_dialog_main, null);

        setView(view);

        Context context = Application.getContext();

        String componentList = "\n";
        String versionName   = "";

        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        componentList += "\n\t\u2022 LogLibrary v"
                + io.cloudwalk.loglibrary.BuildConfig.VERSION_NAME;

        componentList += "\n\t\u2022 PinpadLibrary v"
                + io.cloudwalk.pos.pinpadlibrary.BuildConfig.VERSION_NAME;

        componentList += "\n\t\u2022 UtilitiesLibrary v"
                + io.cloudwalk.utilitieslibrary.BuildConfig.VERSION_NAME;

        String contentView = "(P)roof (O)f (C)oncept v" + versionName + " @" + BuildConfig.BUILD_DATE
                + componentList + "\n\n"
                + context.getString(R.string.content_about);

        ((TextView) view.findViewById(R.id.tv_alert_dialog)).setText(contentView);

        EditText editText = view.findViewById(R.id.et_alert_dialog);

        editText.setText(SharedPreferencesUtility.readIPv4());

        editText.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

        // editText.setKeyListener(DigitsKeyListener.getInstance("0123456789.:"));

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                onFilterEntry(editText);

                String s  = dest  .subSequence(0, dstart).toString();
                       s += source.subSequence(start, end);
                       s += dest  .subSequence(dend, dest.length()).toString();

                String p = IPV4_PATTERN;

                if (s.matches(p)) {
                    String[] splits = s.split("[.:]");

                    for (int i = 0; i < splits.length && i < 4; i++) {
                        if (Integer.parseInt(splits[i]) > 255) {
                            return onFilterBlock(editText);
                        }
                    }

                    getButton(BUTTON_POSITIVE).setEnabled(!(splits.length != 5));

                    return null;
                } else {
                    return onFilterBlock(editText);
                }
            }
        };

        editText.setFilters(new InputFilter[] { filter });

        setButton(BUTTON_POSITIVE, activity.getString(R.string.action_save),
                (OnClickListener) (dialog, which) -> {
            SharedPreferencesUtility.writeIPv4(editText.getText().toString());

            dialog.dismiss();
        });

        setButton(BUTTON_NEGATIVE, activity.getString(R.string.action_exit),
                (OnClickListener) (dialog, which) -> {
            dialog.dismiss();
        });

        setCanceledOnTouchOutside(false);
    }
}
