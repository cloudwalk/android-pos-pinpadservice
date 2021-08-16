package io.cloudwalk.pos.demo;

import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.loglibrary.Log;

public class AppCompatActivity extends androidx.appcompat.app.AppCompatActivity {
    private static final String
            TAG = AppCompatActivity.class.getSimpleName();

    private Semaphore
            mSemaphore = new Semaphore(1, true);

    private boolean
            mPauseStatus = false;

    private boolean
            mStopStatus = false;

    protected boolean wasPaused() {
        Log.d(TAG, "wasPaused");

        boolean value;

        mSemaphore.acquireUninterruptibly();

        value = mPauseStatus;

        mSemaphore.release();

        return value;
    }

    protected boolean wasStopped() {
        Log.d(TAG, "wasStopped");

        boolean value;

        mSemaphore.acquireUninterruptibly();

        value = mStopStatus;

        mSemaphore.release();

        return value;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        mSemaphore.acquireUninterruptibly();

        mPauseStatus = true;

        mSemaphore.release();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        mSemaphore.acquireUninterruptibly();

        mPauseStatus = false;
        mStopStatus  = false;

        mSemaphore.release();

        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();

        mSemaphore.acquireUninterruptibly();

        mStopStatus = true;

        mSemaphore.release();
    }
}
