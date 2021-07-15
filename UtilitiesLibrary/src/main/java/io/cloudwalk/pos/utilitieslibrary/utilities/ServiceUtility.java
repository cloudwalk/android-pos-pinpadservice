package io.cloudwalk.pos.utilitieslibrary.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import io.cloudwalk.pos.utilitieslibrary.models.ServiceModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ServiceUtility {
    private static final String TAG = ServiceUtility.class.getSimpleName();

    private static final List<ServiceModel> mServiceList = new ArrayList<>(0);

    private static final Semaphore sSemaphore = new Semaphore(1, true);

    /**
     * Connection callback.
     */
    public static interface Callback {
        /**
         * Self-describing.
         */
        public void onSuccess();

        /**
         * Indicates a service was disconnected, isn't found or its bind failed due to missing
         * permissions.<br>
         * A reconnection strategy is recommended in here.
         */
        public void onFailure();
    }

    /**
     * Constructor.
     */
    private ServiceUtility() {
        Log.d(TAG, "ServiceUtility");

        /* Nothing to do */
    }

    /**
     * See {@link ServiceUtility#retrieve(String, String)} notes.
     */
    private static IBinder getService(@NotNull String pkg, @NotNull String cls) {
        Log.d(TAG, "getService");

        IBinder service = null;

        long timeout = 2750;
        long timestamp = SystemClock.elapsedRealtime();

        do {
            sSemaphore.acquireUninterruptibly();

            int index = search(cls);

            if (index >= 0) {
                service = mServiceList.get(index).getService();
            }

            sSemaphore.release();

            if (index < 0 || service != null) {
                break;
            } else {
                SystemClock.sleep(timeout / 10);
            }
        } while ((timestamp + timeout) >= SystemClock.elapsedRealtime());

        return service;
    }

    /**
     * Searches for a previously bounded service.
     *
     * @param cls service full class name
     * @return index of the service in the list or -1
     */
    private static int search(String cls) {
        Log.d(TAG, "search");

        for (int i = 0; i < mServiceList.size(); i++) {
            if (mServiceList.get(i).getComponentName().getClassName().equals(cls)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @param service {@link IBinder}
     */
    private static void setService(ComponentName name, IBinder service, ServiceConnection serviceConnection) {
        Log.d(TAG, "setService");

        sSemaphore.acquireUninterruptibly();

        int index = search(name.getClassName());

        if (index >= 0) {
            mServiceList.get(index).setComponentName(name);

            mServiceList.get(index).setService(service);

            mServiceList.get(index).setServiceConnection(serviceConnection);
        }

        sSemaphore.release();
    }

    /**
     * Retrieves a valid instance of {@link IBinder} according to given {@code pkg} and
     * {@code cls}.
     *
     * @param pkg service package name
     * @param cls service class name
     * @return {@link IBinder}
     */
    public static IBinder retrieve(@NotNull String pkg, @NotNull String cls) {
        Log.d(TAG, "retrieve");

        return getService(pkg, cls);
    }

    /**
     * Starts a new thread and calls {@link Runnable#run()} from given {@link Runnable}.<br>
     * Intended as a helper for UI thread calls.<br>
     * <code>
     *     <pre>
     * ServiceUtility.execute(new Runnable() {
     *    {@literal @}Override
     *     public void execute() {
     *         // code you shouldn't run on the main thread goes here
     *     }
     * });
     *     </pre>
     * </code>
     *
     * @param runnable {@link Runnable}
     */
    public static void execute(@NotNull Runnable runnable) {
        Log.d(TAG, "execute");

        new Thread() {
            @Override
            public void run() {
                super.run();

                runnable.run();
            }
        }.start();
    }

    /**
     * Binds a service according to given {@code pkg} and {@code cls}.<br>
     * Ensures the binding will be undone in the event of a service disconnection.
     *
     * @param pkg service package name
     * @param cls service class name
     */
    public static void register(@NotNull String pkg, @NotNull String cls, @NotNull Callback callback) {
        Log.d(TAG, "register");

        new Thread() {
            @Override
            public void run() {
                super.run();

                sSemaphore.acquireUninterruptibly();

                try {
                    if (search(cls) >= 0) {
                        return;
                    }

                    Log.d(TAG, "cls [" + cls + "]");
                    Log.d(TAG, "pkg [" + pkg + "]");

                    Context context = DataUtility.getPackageContext();

                    ServiceConnection serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            Log.d(TAG, "onServiceConnected::name [" + name.getClassName() + "]");

                            setService(name, service, this);

                            callback.onSuccess();
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            Log.e(TAG, "onServiceDisconnected::name [" + name.getClassName() + "]");

                            unregister(pkg, cls);

                            callback.onFailure();
                        }

                        @Override
                        public void onBindingDied(ComponentName name) {
                            Log.e(TAG, "onBindingDied::name [" + name.getClassName() + "]");

                            unregister(pkg, cls);

                            callback.onFailure();
                        }
                    };

                    int count = 0;

                    mServiceList.add(new ServiceModel(new ComponentName(pkg, cls), null));

                    do {
                        Intent intent = new Intent();

                        if (count == 0) {
                            intent.setClassName(pkg, cls);
                        }

                        if (count == 1 || count == 3) {
                            break;
                        }

                        if (count == 2) {
                            intent.setAction(cls);

                            intent.setPackage(pkg);
                        }

                        if (count >= 4) {
                            mServiceList.remove(search(cls));

                            Log.e(TAG, "Failed to bind to " + intent.getAction() + " (either not found or missing permission).");

                            callback.onFailure();

                            break;
                        }

                        count += (context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) ? 1 : 2;
                    } while (true);
                } finally {
                    sSemaphore.release();
                }
            }
        }.start();
    }

    /**
     * Unbinds a service according to given {@code pkg} and {@code cls}.
     *
     * @param pkg service package name
     * @param cls service class name
     */
    public static void unregister(@NotNull String pkg, @NotNull String cls) {
        Log.d(TAG, "unregister");

        sSemaphore.acquireUninterruptibly();

        Context context = DataUtility.getPackageContext();

        int index = search(cls);

        if (index >= 0) {
            ServiceConnection serviceConnection = mServiceList.get(index).getServiceConnection();

            if (serviceConnection != null) {
                context.unbindService(serviceConnection);
            }

            mServiceList.remove(index);
        }

        sSemaphore.release();
    }
}
