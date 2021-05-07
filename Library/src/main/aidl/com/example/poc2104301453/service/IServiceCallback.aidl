package com.example.poc2104301453.service;

import com.example.poc2104301453.service.IServiceCallback;

interface IServiceCallback {
    /**
     * @see com.example.library.ABECS.Callback.Status#onFailure(Bundle)
     */
     void onFailure(inout Bundle output);

    /**
     * @see com.example.library.ABECS.Callback.Status#onSuccess(Bundle)
     */
    void onSuccess(inout Bundle output);
}
