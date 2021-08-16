package io.cloudwalk.pos.utilitieslibrary.models;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceModel {
    private static final String
            TAG = ServiceModel.class.getSimpleName();

    private ComponentName
            mComponentName;

    private IBinder
            mService;

    private ServiceConnection
            mServiceConnection;

    public ServiceModel(ComponentName name, IBinder service) {
        this(name, service, null);
    }

    public ServiceModel(ComponentName name, IBinder service, ServiceConnection serviceConnection) {
        mComponentName = name;

        mService = service;

        mServiceConnection = serviceConnection;
    }

    /**
     * @return {@link ComponentName}
     */
    public ComponentName getComponentName() {
        return mComponentName;
    }

    /**
     * @return {@link IBinder}
     */
    public IBinder getService() {
        return mService;
    }

    /**
     * @return {@link ServiceConnection}
     */
    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    /**
     * @param componentName {@link ComponentName}
     */
    public void setComponentName(ComponentName componentName) {
        mComponentName = componentName;
    }

    /**
     * @param service {@link IBinder}
     */
    public void setService(IBinder service) {
        mService = service;
    }

    /**
     * @param serviceConnection {@link ServiceConnection}
     */
    public void setServiceConnection(ServiceConnection serviceConnection) {
        mServiceConnection = serviceConnection;
    }
}
