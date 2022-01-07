package io.cloudwalk.pos.pinpadservice.presentation;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;

import io.cloudwalk.loglibrary.Log;
import io.cloudwalk.pos.pinpadservice.R;
import io.cloudwalk.utilitieslibrary.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String
            TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        AlertDialog alertDialog = new MainAlertDialog(MainActivity.this);

        alertDialog.setOnDismissListener(dialog -> finish());

        alertDialog.show();
    }
}
