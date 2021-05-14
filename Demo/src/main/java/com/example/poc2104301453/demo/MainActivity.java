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

import static com.example.poc2104301453.library.ABECS.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_LOGCAT = MainActivity.class.getSimpleName();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    /**
     * 2021-05-07: async. call w/o registering a callback.
     */
    private void T001() {
        ABECS.register(getApplicationContext());

        Bundle input = new Bundle();

        input.putString(KEY_REQUEST, ABECS.VALUE_REQUEST_OPN);

        Bundle output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T001::output [" + ((output != null) ? output.toString() : "null") + "]");
    }

    /**
     * 2021-05-07: async. call w/ a registered callback.
     *
     * @param callback {@link ABECS.Callback}
     */
    private void T002(ABECS.Callback callback) {
        ABECS.register(getApplicationContext(), callback);

        Bundle input = new Bundle();

        input.putString(KEY_REQUEST, ABECS.VALUE_REQUEST_OPN);

        Bundle output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T002::output [" + ((output != null) ? output.toString() : "null") + "]");
    }

    /**
     * 2021-05-07: overlapped async. calls w/ a registered callback.
     *
     * @param callback {@link ABECS.Callback}
     */
    private void T003(ABECS.Callback callback) {
        ABECS.register(getApplicationContext(), callback);

        Bundle input = new Bundle();

        input.putString(KEY_REQUEST, ABECS.VALUE_REQUEST_OPN);

        Bundle output;

        output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T003::output [" + ((output != null) ? output.toString() : "null") + "]");

        output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T003::output [" + ((output != null) ? output.toString() : "null") + "]");
    }

    /**
     * 2021-05-07: sync. call w/o registering a callback.
     */
    private void T004() {
        ABECS.register(getApplicationContext());

        Bundle input = new Bundle();

        input.putString(KEY_REQUEST, ABECS.VALUE_REQUEST_OPN);

        input.putBoolean(KEY_SYNCHRONOUS_OPERATION, true);

        Bundle output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T004::output [" + ((output != null) ? output.toString() : "null") + "]");
    }

    /**
     * 2021-05-07: sync. call w/ a registered callback.
     */
    private void T005(ABECS.Callback callback) {
        ABECS.register(getApplicationContext(), callback);

        Bundle input = new Bundle();

        input.putString(KEY_REQUEST, ABECS.VALUE_REQUEST_OPN);

        input.putBoolean(KEY_SYNCHRONOUS_OPERATION, true);

        Bundle output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T005::output [" + ((output != null) ? output.toString() : "null") + "]");
    }

    /**
     * 2021-05-07: overlapped sync. calls w/ a registered callback.
     */
    private void T006(ABECS.Callback callback) {
        ABECS.register(getApplicationContext(), callback);

        Bundle input = new Bundle();

        input.putString(KEY_REQUEST, ABECS.VALUE_REQUEST_OPN);

        input.putBoolean(KEY_SYNCHRONOUS_OPERATION, true);

        Bundle output;

        output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T006::output [" + ((output != null) ? output.toString() : "null") + "]");

        output = ABECS.run(input);

        Log.d(TAG_LOGCAT, "T006::output [" + ((output != null) ? output.toString() : "null") + "]");
    }

    private void runTests() {
        ABECS.Callback.Kernel kernel =
                new ABECS.Callback.Kernel() {
                    /* TODO */
                };

        ABECS.Callback.Status status =
                new ABECS.Callback.Status() {
                    @Override
                    public void onFailure(Bundle output) {
                        Log.d(TAG_LOGCAT, "onFailure::output [" + output.toString() + "]");
                    }

                    @Override
                    public void onSuccess(Bundle output) {
                        Log.d(TAG_LOGCAT, "onSuccess::output [" + output.toString() + "]");
                    }
                };

        new Thread() { /* 2021-05-14: tests do overlap async. calls. If that happens in the main
                        * thread (as described in {@link ABECS#run(Bundle)), it may freeze the
                        * application. */
            @Override
            public void run() {
                super.run();

                ABECS.Callback callback = new ABECS.Callback(kernel, status);

                T001();
                T002(callback);
                T003(callback);
                T004();
                T005(callback);
                T006(callback);
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

        runTests();
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
