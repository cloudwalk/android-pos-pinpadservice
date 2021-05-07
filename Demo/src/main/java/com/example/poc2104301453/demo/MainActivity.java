package com.example.poc2104301453.demo;

import android.os.Bundle;

import com.example.poc2104301453.R;
import com.example.poc2104301453.library.ABECS;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.poc2104301453.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_LOGCAT = MainActivity.class.getSimpleName();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private void initialDraft() {
        ABECS.init(getApplicationContext());

        ABECS.Callback.Kernel kernel =
                new ABECS.Callback.Kernel() {
                    /* TODO */
                };

        ABECS.Callback.Status status =
                new ABECS.Callback.Status() {
                    @Override
                    public void onFailure(Bundle output) {
                        output.get(null);

                        Log.d(TAG_LOGCAT, "initialDraft::onFailure::output [" + output.toString() + "]");
                    }

                    @Override
                    public void onSuccess(Bundle output) {
                        output.get(null);

                        Log.d(TAG_LOGCAT, "initialDraft::onSuccess::output [" + output.toString() + "]");
                    }
                };

        Bundle input = new Bundle();

        input.putString("request", ABECS.VALUE_REQUEST_OPN);

        /* 2021-05-07: TEST 001 (async. call w/o registering a callback) */

        Log.d(TAG_LOGCAT, "initialDraft::TEST 0001");

        Bundle output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "initialDraft::output [" + ((output != null) ? output.toString() : "null") + "]");

        /* 2021-05-07: TEST 002 (async. call w/ a registered a callback) */

        ABECS.init(getApplicationContext(), new ABECS.Callback(kernel, status));

        Log.d(TAG_LOGCAT, "initialDraft::TEST 0002");

        output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "initialDraft::output [" + ((output != null) ? output.toString() : "null") + "]");

        new Thread() {
            @Override
            public void run() {
                super.run();

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        Bundle input = new Bundle();

                        /* 2021-05-07: TEST 003 (sync. call w/ a registered a callback) */

                        input.putString("request", ABECS.VALUE_REQUEST_OPN);

                        input.putBoolean("synchronous_operation", true);

                        Log.d(TAG_LOGCAT, "initialDraft::TEST 0003");

                        Bundle output = ABECS.run(input);

                        Log.d(TAG_LOGCAT, "initialDraft::output [" + ((output != null) ? output.toString() : "null") + "]");

                        /* 2021-05-07: TEST 004 (sync. call w/o registering a callback) */

                        ABECS.init(getApplicationContext());

                        Log.d(TAG_LOGCAT, "initialDraft::TEST 0004");

                        output = ABECS.run(input);

                        Log.d(TAG_LOGCAT, "initialDraft::output [" + ((output != null) ? output.toString() : "null") + "]");
                    }
                }.start();
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initialDraft();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
