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
        Bundle input = new Bundle();
        input.putString("request", ABECS.VALUE_REQUEST_OPN);
        input.putBoolean("synchronous_operation", false);

        ABECS.Callback.Process process = new ABECS.Callback.Process() {
            /* TODO */
        };

        ABECS.Callback.Status status = new ABECS.Callback.Status() {
            @Override
            public void onFailure(Bundle output) {
                if (output != null) {
                    output.get(null); /* 2021-05-05: just to force the parcelable data printable */
                }

                Log.d(TAG_LOGCAT, "run::input [" + ((output != null) ? output.toString() : null) + "]");
            }

            @Override
            public void onSuccess(Bundle output) {
                if (output != null) {
                    output.get(null); /* 2021-05-05: just to force the parcelable data printable */
                }

                Log.d(TAG_LOGCAT, "run::input [" + ((output != null) ? output.toString() : null) + "]");
            }
        };

        ABECS.Callback callback = new ABECS.Callback(process, status);

        ABECS.run(getApplicationContext(), input);

        ABECS.run(getApplicationContext(), callback, input);

        new Thread() {
            @Override
            public void run() {
                super.run();

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        Bundle input = new Bundle();

                        input.putString("request", ABECS.VALUE_REQUEST_OPN);
                        input.putBoolean("synchronous_operation", true);

                        ABECS.Callback callback = new ABECS.Callback(process, status);

                        ABECS.run(getApplicationContext(), input);

                        ABECS.run(getApplicationContext(), callback, input);
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
