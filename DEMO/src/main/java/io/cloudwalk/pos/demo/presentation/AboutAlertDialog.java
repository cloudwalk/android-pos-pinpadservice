package io.cloudwalk.pos.demo.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.widget.TextView;

import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.loglibrary.Log;
import io.cloudwalk.pos.utilitieslibrary.Application;

public class AboutAlertDialog extends AlertDialog {
    private static final String
            TAG = AboutAlertDialog.class.getSimpleName();

    /**
     * Constructor.
     */
    private AboutAlertDialog(Context context) {
        super(context);

        Log.d(TAG, "AboutAlertDialog");
    }

    /**
     * Constructor.
     */
    private AboutAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

        Log.d(TAG, "AboutAlertDialog");
    }

    /**
     * Constructor.
     */
    private AboutAlertDialog(Context context, int themeResId) {
        super(context, themeResId);

        Log.d(TAG, "AboutAlertDialog");
    }

    /**
     * Constructor.
     */
    protected AboutAlertDialog(Activity activity) {
        super(activity);

        Log.d(TAG, "AboutAlertDialog");

        setIcon(R.mipmap.ic_pinpadservice);

        setTitle(R.string.app_name);

        View view = getLayoutInflater().inflate(R.layout.alert_dialog_about, null);

        setView(view);

        Context context = Application.getPackageContext();

        String componentList = "\n";
        String versionName   = "";

        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }

        componentList += "\n\t\u2022 LogLibrary v"
                + io.cloudwalk.pos.loglibrary.BuildConfig.VERSION_NAME;

        componentList += "\n\t\u2022 PinpadLibrary v"
                + io.cloudwalk.pos.pinpadlibrary.BuildConfig.VERSION_NAME;

        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo("io.cloudwalk.pos.pinpadservice", 0);

            componentList += "\n\t\u2022 PinpadService v" + packageInfo.versionName;
        } catch (Exception exception) {
            componentList += "\n\t\u2022 PinpadService (not found!)";
        }

        componentList += "\n\t\u2022 UtilitiesLibrary v"
                + io.cloudwalk.pos.utilitieslibrary.BuildConfig.VERSION_NAME;

        String trade = context.getString(R.string.app_name).substring(4, 14);

        String contentView = "Proof Of Concept " + trade + ((!versionName.isEmpty()) ? (" v" + versionName) : versionName)
                + componentList
                + "\n\n"
                + context.getString(R.string.content_about);

        ((TextView) view.findViewById(R.id.tv_about)).setText(contentView);
    }
}
